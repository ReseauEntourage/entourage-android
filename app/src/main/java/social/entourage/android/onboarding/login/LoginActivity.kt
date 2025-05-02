package social.entourage.android.onboarding.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageApplication.Companion.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLoginBinding
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.CustomProgressDialog
import java.util.Locale

class LoginActivity : BaseActivity() {

    lateinit var authenticationController: AuthenticationController
    lateinit var binding:ActivityLoginBinding

    private val minimumPhoneCharacters = 9
    private val TIME_BEFORE_CALL = 60

    private var countDownTimer: CountDownTimer? = null
    private var timeOut = TIME_BEFORE_CALL
    var isLoading = false



    lateinit var alertDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        authenticationController = EntourageApplication.get().authenticationController

        alertDialog = CustomProgressDialog(this)
        setupViews()
        setContentView(binding.root)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_LOGIN_LOGIN)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }

    fun setupViews() {

        binding.onboardLoginMainlayout.setOnTouchListener { view, motionEvent ->
            view.hideKeyboard()
            view.performClick()
            true
        }

       binding.iconBack.setOnClickListener {
            goBack()
        }

        binding.uiLoginButtonResendCode.setOnClickListener {
            if(binding.uiLoginPhoneEtPhone.text.toString().isNotEmpty()) {
                CustomAlertDialog.showWithCancelFirst(
                    this,
                    getString(R.string.login_button_resend_code),
                    String.format(
                        getString(R.string.login_button_resend_code_text),
                        binding.uiLoginPhoneEtPhone.text.toString()
                    ),
                    getString(R.string.login_button_resend_code_action)
                ) {
                    checkAndResendCode()
                }
            } else {
                Toast.makeText(this, R.string.login_text_invalid_format, Toast.LENGTH_LONG).show()
            }
        }

        binding.uiLoginButtonSignup.setOnClickListener {
            validateInputsAndLogin()
        }

        binding.uiLoginButtonChangePhone.setOnClickListener {
            val intent = Intent(this, LoginChangePhoneActivity::class.java)
            startActivity(intent)
        }
        val text = getString(R.string.terms_and_conditions_html)
        binding.tvConditionGenerales.text = Html.fromHtml(text)
        binding.tvConditionGenerales.movementMethod = LinkMovementMethod.getInstance()


    }

    fun changeLocale(activity: Activity, locale: Locale) {
        val resources = activity.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.applicationContext.createConfigurationContext(configuration)
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
        sharedPreferences.edit().putBoolean(EntourageApplication.KEY_MIGRATION_V7_OK,true).apply()
        goRealMain()
    }

    fun goRealMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        goBack()
    }

    /********************************
     * Methods Valide inputs
     ********************************/

    fun validateInputsAndLogin():Boolean {
        val countryCode = binding.uiLoginPhoneCcpCode.selectedCountryCodeWithPlus
        val phoneNumber = binding.uiLoginPhoneEtPhone.text.toString()
        val codePwd = binding.uiLoginEtCode.text.toString()

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
            return false
        }

        val phoneWithCode = Utils.checkPhoneNumberFormat(countryCode, phoneNumber)

        if (phoneWithCode != null) {
            if (!isLoading) {
                isLoading = true
                login(phoneWithCode, codePwd)
                return true
            }
        } else {
            showError(
                R.string.attention_pop_title,
                getString(R.string.login_error_invalid_phone_format),
                R.string.close
            )
            return false
        }
        return true
    }

    private fun checkAndResendCode() {
        val countryCode = binding.uiLoginPhoneCcpCode?.selectedCountryCodeWithPlus
        val phoneNumber = binding.uiLoginPhoneEtPhone?.text.toString()

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
