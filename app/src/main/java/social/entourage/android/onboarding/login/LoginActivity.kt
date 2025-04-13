package social.entourage.android.onboarding.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageApplication.Companion.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLoginBinding
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.view.CustomProgressDialog
import java.util.Locale

class LoginActivity : BaseActivity() {

    lateinit var authenticationController: AuthenticationController
    lateinit var binding:ActivityLoginBinding

    private var countDownTimer: CountDownTimer? = null
    private var timeOut = TIME_BEFORE_CALL
    var isLoading = false

    private lateinit var alertDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        authenticationController = EntourageApplication.get().authenticationController
        alertDialog = CustomProgressDialog(this)
        setupViews()
        setContentView(binding.root)

        updatePaddingTopForEdgeToEdge(binding.layout)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_LOGIN_LOGIN)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }

    private fun setEditTextAlignmentBasedOnLocale() {
        val locale = Locale.getDefault()

        // Définir l'alignement pour chaque EditText concerné
        setEditTextGravity(binding.uiLoginPhoneEtPhone, locale)
        setEditTextGravity(binding.uiLoginEtCode, locale)
    }

    private fun setEditTextGravity(editText: EditText, locale: Locale) {
        if (locale.language == "ar") {
            editText.gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.END
            editText.textAlignment = android.view.View.TEXT_ALIGNMENT_VIEW_END
        } else {
            editText.gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.START
            editText.textAlignment = android.view.View.TEXT_ALIGNMENT_VIEW_START
        }
    }

    fun setupViews() {
        setEditTextAlignmentBasedOnLocale()
        binding.onboardLoginMainlayout.setOnTouchListener { view, _ ->
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
                val message =
                    String.format(getString(R.string.error_login_phone_length),
                        MINIMUM_PHONE_CHARACTERS
                    )
                showError(R.string.attention_pop_title, message, R.string.close)
            }
        }

        binding.uiLoginButtonSignup.setOnClickListener {
            validateInputsAndLogin()
        }

        binding.uiLoginButtonChangePhone.setOnClickListener {
            val intent = Intent(this, LoginChangePhoneActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        val text = getString(R.string.terms_and_conditions_html)
        binding.tvConditionGenerales.text = Html.fromHtml(text)
        binding.tvConditionGenerales.movementMethod = LinkMovementMethod.getInstance()
    }

    /*fun changeLocale(activity: Activity, locale: Locale) {
        val resources = activity.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.applicationContext.createConfigurationContext(configuration)
        }
    }*/

    /********************************
     * Methods
     ********************************/

    private fun activateTimer() {
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

    private fun goBack() {
        startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
        finish()
    }

    private fun goMain() {
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, true).apply()
        sharedPreferences.edit().putBoolean(EntourageApplication.KEY_MIGRATION_V7_OK,true).apply()
        goRealMain()
    }

    private fun goRealMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        goBack()
    }

    /********************************
     * Methods Valide inputs
     ********************************/

    private fun validateInputsAndLogin():Boolean {
        val countryCode = binding.uiLoginPhoneCcpCode.selectedCountryCodeWithPlus
        val phoneNumber = binding.uiLoginPhoneEtPhone.text.toString()
        val codePwd = binding.uiLoginEtCode.text.toString()

        var isValidate = true
        var message = ""

        if (phoneNumber.length < MINIMUM_PHONE_CHARACTERS) {
            isValidate = false
            message =
                String.format(getString(R.string.error_login_phone_length),
                    MINIMUM_PHONE_CHARACTERS
                )
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

        if (phoneWithCode == null) {
            showError(
                R.string.attention_pop_title,
                getString(R.string.login_error_invalid_phone_format),
                R.string.close
            )
            return false
        }
        if (!isLoading) {
            isLoading = true
            login(phoneWithCode, codePwd)
        }
        return true
    }

    private fun checkAndResendCode() {
        val countryCode = binding.uiLoginPhoneCcpCode.selectedCountryCodeWithPlus
        val phoneNumber = binding.uiLoginPhoneEtPhone.text.toString()

        if (phoneNumber.length <= MINIMUM_PHONE_CHARACTERS) {
            val message =
                String.format(getString(R.string.error_login_phone_length),
                    MINIMUM_PHONE_CHARACTERS
                )
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
            } else {
                resendCode(phoneWithCode)
            }
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
                    val mutableLoggedNumbers = loggedNumbers.toHashSet()
                    mutableLoggedNumbers.add(phone)
                    sharedPreferences.edit {
                        putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, mutableLoggedNumbers)
                    }
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

    private fun resendCode(phone: String) {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_SMS)
        OnboardingAPI.getInstance().requestNewCode(phone) { isOK, _, error ->
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

    companion object {
        private const val TIME_BEFORE_CALL = 60
        private const val MINIMUM_PHONE_CHARACTERS = 9
    }
}
