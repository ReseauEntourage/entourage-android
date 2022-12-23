package social.entourage.android.onboarding.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageApplication.Companion.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.BaseActivity
import social.entourage.android.new_v8.utils.CustomAlertDialog
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.CustomProgressDialog

class LoginActivity : BaseActivity() {

    lateinit var authenticationController: AuthenticationController

    private val minimumPhoneCharacters = 9
    private val TIME_BEFORE_CALL = 60

    private var countDownTimer: CountDownTimer? = null
    private var timeOut = TIME_BEFORE_CALL
    var isLoading = false

    lateinit var alertDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticationController = EntourageApplication.get().authenticationController
        setContentView(R.layout.activity_login)

        alertDialog = CustomProgressDialog(this)
        setupViews()

        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_LOGIN_LOGIN)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }

    fun setupViews() {

        onboard_login_mainlayout?.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            view.performClick()
            true
        }

       icon_back?.setOnClickListener {
            goBack()
        }

        ui_login_button_resend_code?.setOnClickListener {
            if(ui_login_phone_et_phone?.text.toString().isNotEmpty()) {
                CustomAlertDialog.showWithCancelFirst(
                    this,
                    getString(R.string.login_button_resend_code),
                    String.format(
                        getString(R.string.login_button_resend_code_text),
                        ui_login_phone_et_phone?.text.toString()
                    ),
                    getString(R.string.login_button_resend_code_action)
                ) {
                    checkAndResendCode()
                }
            } else {
                Toast.makeText(this, R.string.login_text_invalid_format, Toast.LENGTH_LONG).show()
            }
        }

        ui_login_button_signup?.setOnClickListener {
            validateInputsAndLogin()
        }

        ui_login_button_change_phone?.setOnClickListener {
            val intent = Intent(this, LoginChangePhoneActivity::class.java)
            startActivity(intent)
        }
    }

    /********************************
     * Methods
     ********************************/

    fun activateTimer() {
        cancelTimer()
        timeOut = TIME_BEFORE_CALL
        countDownTimer = object : CountDownTimer(600000, 1000L) {
            override fun onFinish() {
                cancelTimer()
            }

            override fun onTick(p0: Long) {
                timeOut -= 1
            }
        }

        countDownTimer?.start()
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun goBack() {
        startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
        finish()
    }

    fun goMain() {
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, true).apply()

        goRealMain()
    }

    fun goRealMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        goBack()
    }

    /********************************
     * Methods Valide inputs
     ********************************/

    fun validateInputsAndLogin() {
        val countryCode = ui_login_phone_ccp_code?.selectedCountryCodeWithPlus
        val phoneNumber = ui_login_phone_et_phone?.text.toString()
        val codePwd = ui_login_et_code?.text.toString()

        var isValidate = true
        var message = ""
        if (phoneNumber.length < minimumPhoneCharacters) {
            isValidate = false
            message =
                String.format(getString(R.string.error_login_phone_length), minimumPhoneCharacters)
        }

        if (isValidate && codePwd.length != 6) {
            isValidate = false
            message = getString(R.string.error_login_code_lenght)
        }

        if (!isValidate) {
            showError(R.string.attention_pop_title, message, R.string.close)
            return
        }

        val phoneWithCode = Utils.checkPhoneNumberFormat(countryCode, phoneNumber)

        if (phoneWithCode != null) {
            if (!isLoading) {
                isLoading = true
                login(phoneWithCode, codePwd)
            }
        } else {
            showError(
                R.string.attention_pop_title,
                getString(R.string.login_error_invalid_phone_format),
                R.string.close
            )
        }
    }

    private fun checkAndResendCode() {
        val countryCode = ui_login_phone_ccp_code?.selectedCountryCodeWithPlus
        val phoneNumber = ui_login_phone_et_phone?.text.toString()

        if (phoneNumber.length <= minimumPhoneCharacters) {
            val message =
                String.format(getString(R.string.error_login_phone_length), minimumPhoneCharacters)
            showError(R.string.attention_pop_title, message, R.string.close)
            return
        }

        if (timeOut > 0 && timeOut != TIME_BEFORE_CALL) {
            val message = String.format(getString(R.string.onboard_sms_pop_alert), timeOut)
            showError(R.string.attention_pop_title, message, R.string.close)
        } else {
            val phoneWithCode = Utils.checkPhoneNumberFormat(countryCode, phoneNumber)
            if (phoneWithCode == null) {
                showError(
                    R.string.attention_pop_title,
                    getString(R.string.login_error_invalid_phone_format),
                    R.string.close
                )
                return
            }
            resendCode(phoneWithCode)
        }
    }

    /********************************
     * Network
     ********************************/

    fun login(phone: String, codePwd: String) {
        alertDialog.show(R.string.onboard_waiting_dialog)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_SUBMIT)
        OnboardingAPI.getInstance().login(phone, codePwd) { isOK, loginResponse, error ->
            isLoading = false
            if (isOK) {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_SUCCESS)
                loginResponse?.let {
                    authenticationController.saveUser(loginResponse.user)
                }
                authenticationController.saveUserPhoneAndCode(phone, codePwd)

                //set the tutorial as done
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                (sharedPreferences.getStringSet(
                    EntourageApplication.KEY_TUTORIAL_DONE,
                    HashSet()
                ) as HashSet<String>?)?.let { loggedNumbers ->
                    loggedNumbers.add(phone)
                    sharedPreferences.edit()
                        .putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers).apply()
                }
                alertDialog.dismiss()
                goMain()
            } else {
                alertDialog.dismiss()
                var errorId = R.string.login_error_network
                if (error != null) {
                    when {
                        error.contains("INVALID_PHONE_FORMAT") -> {
                            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_LOGIN_PHONE)
                            errorId = R.string.login_error_invalid_phone_format
                        }
                        error.contains("UNAUTHORIZED") -> {
                            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_LOGIN_FAIL)
                            errorId = R.string.login_error_invalid_credentials
                        }
                        else -> {
                            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_LOGIN_ERROR)
                            errorId = R.string.login_error
                        }
                    }
                }
                if (!isFinishing) {
                    showError(
                        R.string.login_error_title,
                        getString(errorId),
                        R.string.login_retry_label
                    )
                }
            }
        }
    }

    fun resendCode(phone: String) {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_SMS)
        OnboardingAPI.getInstance().requestNewCode(phone) { isOK, loginResponse, error ->
            if (isOK) {
                Toast.makeText(this, R.string.login_smscode_sent, Toast.LENGTH_LONG).show()
                activateTimer()
            } else {
                if (error != null && error.contains("USER_NOT_FOUND")) {
                    Toast.makeText(this, R.string.login_text_lost_code_ko, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, R.string.login_error_network, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /********************************
     * Helpers
     ********************************/

    private fun showError(titleId: Int, message: String, buttonTextId: Int) {
        CustomAlertDialog.showOnlyOneButton(
            this,
            getString(titleId),
            message,
            getString(buttonTextId)
        ) {}
    }
}
