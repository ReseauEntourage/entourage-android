package social.entourage.android.authentication.login

import android.Manifest.permission
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.login_email.*
import kotlinx.android.synthetic.main.login_lost_code.*
import kotlinx.android.synthetic.main.login_name.*
import kotlinx.android.synthetic.main.login_notifications_permission.*
import kotlinx.android.synthetic.main.login_signin.*
import kotlinx.android.synthetic.main.login_startup.*
import kotlinx.android.synthetic.main.login_verify_code.*
import social.entourage.android.*
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.User
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.api.tape.Events.OnShowURLEvent
import social.entourage.android.authentication.login.register.OnRegisterUserListener
import social.entourage.android.authentication.login.register.RegisterNumberFragment
import social.entourage.android.authentication.login.register.RegisterSMSCodeFragment
import social.entourage.android.authentication.login.register.RegisterWelcomeFragment
import social.entourage.android.configuration.Configuration
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.Utils.checkPhoneNumberFormat
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.edit.UserEditActionZoneFragment.FragmentListener
import social.entourage.android.user.edit.UserEditActionZoneFragmentCompat
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragmentCompat
import social.entourage.android.user.edit.photo.PhotoEditFragment
import social.entourage.android.view.EntourageSnackbar
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Activity providing the login steps
 */
class LoginActivity : EntourageActivity(), OnRegisterUserListener, PhotoChooseInterface, FragmentListener, AvatarUploadView {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var loggedPhoneNumber: String? = null
    //private val previousView: View? = null

    @JvmField
    @Inject
    var loginPresenter: LoginPresenter? = null

    @JvmField
    @Inject
    var avatarUploadPresenter: AvatarUploadPresenter? = null
    private var onboardingUser: User? = null
    private var goToNextActionAfterActionZone = false
    private var isFromChoice = false

    //login_include_signin)   var loginSignin: View? = null
    //login_ccp)    var countryCodePicker: CountryCodePicker? = null
    //login_edit_phone)    var phoneEditText: EditText? = null
    //login_edit_code)  var passwordEditText: EditText? = null
    //login_button_signin)     var loginButton: Button? = null
    //login_text_lost_code)    var lostCodeText: TextView? = null
    //login_include_lost_code)    var loginLostCode: View? = null
    //login_lost_code_ccp)     var lostCodeCountryCodePicker: CountryCodePicker? = null
    //login_edit_phone_lost_code)     var lostCodePhone: EditText? = null
    //login_button_ask_code)    var receiveCodeButton: Button? = null
    //login_block_lost_code_start) var enterCodeBlock: View? = null
    //login_block_lost_code_confirmation)     var confirmationBlock: View? = null
    //login_text_confirmation)    var codeConfirmation: HtmlTextView? = null
    //login_button_home)    var homeButton: Button? = null
    //login_include_email) var loginEmail: View? = null
    //login_edit_email_profile)     var profileEmail: EditText? = null
    //login_user_photo)     var profilePhoto: ImageView? = null
    //login_button_go)     var goButton: FloatingActionButton? = null
    //login_include_name)    var loginNameView: View? = null
    //login_name_firstname)    var firstnameEditText: EditText? = null
    //login_name_lastname)    var lastnameEditText: EditText? = null
    //login_name_go_button)    var nameGoButton: FloatingActionButton? = null
    //login_include_startup)    var loginStartup: View? = null
    //login_include_verify_code)var loginVerifyCode: View? = null
    //login_button_verify_code)var verifyCodeButton: View? = null
    //login_verify_code_code var receivedCode: TextView? = null
    //login_include_notifications)  var loginNotificationsView: View? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //checkPermissions();
        setContentView(R.layout.activity_login)
        login_email_back_button?.setOnClickListener { onEmailBackClicked() }
        login_button_go?.setOnClickListener { saveEmail()}
        login_name_back_button?.setOnClickListener { onNameBackClicked() }
        login_name_go_button?.setOnClickListener { onNameGoClicked() }
        login_email_ignore_button?.setOnClickListener { ignoreEmail() }
        login_button_register?.setOnClickListener { showRegisterScreen() }
        login_code_sent_close?.setOnClickListener { verifyCodeClose()  }
        login_button_verify_code?.setOnClickListener { verifyCode() }
        login_verify_code_back?.setOnClickListener { showLostCodeScreen() }
        login_notifications_ignore_button?.setOnClickListener { onNotificationsIgnore() }
        login_notifications_accept?.setOnClickListener { onNotificationsAccept() }
        login_verify_code_description?.setOnClickListener { showResendByEmailView() }
        login_button_login?.setOnClickListener { onStartupLoginClicked() }
        login_back_button?.setOnClickListener { onLoginBackClick() }
        login_button_signin?.setOnClickListener { onLoginClick() }
        login_text_lost_code?.setOnClickListener { onLostCodeClick() }
        login_lost_code_close?.setOnClickListener { lostCodeClose()  }
        login_button_ask_code?.setOnClickListener { sendNewCode()}
        login_button_home?.setOnClickListener { returnHome()}
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_01)
        login_include_signin?.visibility = View.GONE
        login_include_lost_code?.visibility = View.GONE
        login_include_verify_code?.visibility = View.GONE
        login_include_email?.visibility = View.GONE
        login_include_name?.visibility = View.GONE
        login_include_notifications?.visibility = View.GONE
        login_edit_code?.apply {
            this.typeface = Typeface.DEFAULT
            this.transformationMethod = PasswordTransformationMethod()
            this.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onLoginClick()
                    return@setOnEditorActionListener true
                }
                false
            }
        }
        val ltw = LoginTextWatcher()
        login_name_firstname?.addTextChangedListener(ltw)
        login_name_lastname?.addTextChangedListener(ltw)
        loginPresenter?.authenticationController?.user?.let { user ->
            launchFillInProfileView(user.phone, user)
        }

        //Hack en attendant la nouvelle version de l'onboarding pour simuler les clicks sur signup / login
        val key = intent.getStringExtra("fromChoice")
        if (key != null && key.equals("login", ignoreCase = true)) {
            isFromChoice = true
            login_include_startup?.visibility = View.GONE
            onStartupLoginClicked()
        } else if (key != null && key.equals("signup", ignoreCase = true)) {
            isFromChoice = true
            login_include_startup?.visibility = View.GONE
            showRegisterScreen()
        }
    }

    override fun setupComponent(entourageComponent: EntourageComponent) {
        DaggerLoginComponent.builder()
                .entourageComponent(entourageComponent)
                .loginModule(LoginModule(this))
                .build()
                .inject(this)
    }

    override fun onBackPressed() {
        when {
            login_include_signin?.visibility == View.VISIBLE -> {
                //Hack en attendant la nouvelle version de l'onboarding (On retourne au choix login/signin)
                if (isFromChoice) {
                    startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
                    finish()
                    return
                }
                login_edit_phone?.setText("")
                login_edit_code?.setText("")
                hideKeyboard()
                login_include_signin?.visibility = View.GONE
                login_include_startup?.visibility = View.VISIBLE
            }
            login_include_lost_code?.visibility == View.VISIBLE -> {
                login_edit_phone_lost_code?.setText("")
                login_include_lost_code?.visibility = View.GONE
                login_include_signin?.visibility = View.VISIBLE
                showKeyboard(login_edit_phone)
            }
            login_include_email?.visibility == View.VISIBLE -> {
                login_include_email?.visibility = View.GONE
                login_include_name?.visibility = View.VISIBLE
            }
            login_include_name?.visibility == View.VISIBLE -> {
                hideKeyboard()
                login_include_name?.visibility = View.GONE
                login_include_signin?.visibility = View.VISIBLE
                showKeyboard(login_edit_phone)
            }
            login_include_verify_code?.visibility == View.VISIBLE -> {
                showLostCodeScreen()
            }
            else -> {
                //Hack en attendant la nouvelle version de l'onboarding (On retourne au choix login/signin)
                if (isFromChoice) {
                    startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
                    finish()
                    return
                }
                super.onBackPressed()
            }
        }
    }

    override fun onStart() {
        BusProvider.instance.register(this)
        super.onStart()
    }

    override fun onStop() {
        BusProvider.instance.unregister(this)
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (index in permissions.indices) {
                if (permissions[index].equals(permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                    BusProvider.instance.post(OnLocationPermissionGranted(grantResults[index] == PackageManager.PERMISSION_GRANTED))
                }
            }
            // We don't care if the user allowed/denied the location, just show the notifications view
            //TODO to do this in onResume
            hideActionZoneView()
            showNotificationPermissionView()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun startMapActivity() {
        stopLoader()
        hideKeyboard()
        EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_OK)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun loginFail(errorCode: Int) {
        stopLoader()
        @StringRes val errorMessage: Int
        when (errorCode) {
            LOGIN_ERROR_INVALID_PHONE_FORMAT -> {
                errorMessage = R.string.login_error_invalid_phone_format
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_FAILED)
            }
            LOGIN_ERROR_UNAUTHORIZED -> {
                errorMessage = R.string.login_error_invalid_credentials
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_FAILED)
            }
            LOGIN_ERROR_NETWORK -> {
                errorMessage = R.string.login_error_network
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_ERROR)
            }
            else -> {
                errorMessage = R.string.login_error
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_ERROR)
            }
        }
        if (!isFinishing) {
            try {
                AlertDialog.Builder(this)
                        .setTitle(R.string.login_error_title)
                        .setMessage(errorMessage)
                        .setPositiveButton(R.string.login_retry_label) { dialog: DialogInterface?, which: Int -> }
                        .create()
                        .show()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun displayToast(@StringRes messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
    }

    fun startLoader() {
        login_button_signin?.setText(R.string.button_loading)
        login_button_signin?.isEnabled = false
        login_button_ask_code?.setText(R.string.button_loading)
        login_button_ask_code?.isEnabled = false
        login_edit_phone_lost_code?.isEnabled = false
        login_button_verify_code?.isEnabled = false
        login_name_go_button?.isEnabled = false
    }

    fun stopLoader() {
        login_button_signin?.setText(R.string.login_button_signin)
        login_button_signin?.isEnabled = true
        login_button_ask_code?.setText(R.string.login_button_ask_code)
        login_button_ask_code?.isEnabled = true
        login_edit_phone_lost_code?.isEnabled = true
        login_button_verify_code?.isEnabled = true
        login_name_go_button?.isEnabled = true
    }

    fun launchFillInProfileView(phoneNumber: String?, user: User) {
        loggedPhoneNumber = phoneNumber
        if (onboardingUser != null) {
            user.isOnboardingUser = true
        }
        try {
            (supportFragmentManager.findFragmentByTag(RegisterSMSCodeFragment.TAG) as DialogFragment?)?.dismiss()
            (supportFragmentManager.findFragmentByTag(RegisterNumberFragment.TAG) as DialogFragment?)?.dismiss()
            (supportFragmentManager.findFragmentByTag(RegisterWelcomeFragment.TAG) as DialogFragment?)?.let {fragment->
                //Hack en attente du onboarding nouvelle version ( pour éviter de fermer l'activity sur le dismiss depuis le choix)
                if (fragment.javaClass == RegisterWelcomeFragment::class.java) {
                    (fragment as RegisterWelcomeFragment).isFromChoice = false
                }
                fragment.dismiss()
            }
            hideKeyboard()
            loginPresenter?.let { presenter ->
                login_include_startup?.visibility = View.GONE
                login_include_signin?.visibility = View.GONE
                login_include_verify_code?.visibility = View.GONE
                when {
                    presenter.shouldShowNameView(user) -> {
                        showNameView()
                    }
                    presenter.shouldShowEmailView(user) -> {
                        showEmailView()
                    }
                    presenter.shouldShowPhotoChooseView(user) -> {
                        showPhotoChooseSource()
                    }
                    presenter.shouldShowActionZoneView(user) -> {
                        showActionZoneView()
                    }
                    else -> {
                        showNotificationPermissionView()
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    // ----------------------------------
    // INTERFACES CALLBACKS
    // ----------------------------------
    override fun onPhotoBack() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_BACK)
        showActionZoneView()
    }

    override fun onPhotoIgnore() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_IGNORE)
        showActionZoneView()
    }

    override fun onPhotoChosen(photoURI: Uri?, photoSource: Int) {
        if (photoSource == PhotoChooseSourceFragmentCompat.TAKE_PHOTO_REQUEST) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_SUBMIT)
        }
        if (loginPresenter?.authenticationController?.user == null) {
            displayToast(R.string.login_error)
            val photoEditFragment = supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
            photoEditFragment?.onPhotoSent(false)
            return
        }

        //Upload the photo to Amazon S3
        showProgressDialog(R.string.user_photo_uploading)
        photoURI?.path?.let {
            val file = File(it)
            avatarUploadPresenter?.uploadPhoto(file)
        }
    }

    override fun onUploadError() {
        displayToast(R.string.user_photo_error_not_saved)
        dismissProgressDialog()
        val photoEditFragment = supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
        photoEditFragment?.onPhotoSent(false)
    }
    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------
    /************************
     * Signin View
     */
    private fun onLoginBackClick() {
        onBackPressed()
    }

    private fun onLoginClick() {
        loginPresenter?.login(
                login_ccp?.selectedCountryCodeWithPlus,
                login_edit_phone?.text.toString(),
                login_edit_code?.text.toString())
                ?: run {
            displayToast(R.string.login_error)
        }
    }

    private fun onLostCodeClick() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SMS_CODE_REQUEST)
        hideKeyboard()
        login_include_signin?.visibility = View.GONE
        login_block_lost_code_start?.visibility = View.VISIBLE
        login_include_lost_code?.visibility = View.VISIBLE
        login_block_lost_code_confirmation?.visibility = View.GONE
        login_edit_phone?.text.toString().let { text->
            login_edit_phone_lost_code?.let {
                it.setText(text)
                showKeyboard(it)
            }
        }
    }

    /************************
     * Lost Code View
     */
    private fun lostCodeClose() {
        onBackPressed()
    }

    private fun sendNewCode() {
        login_edit_phone_lost_code?.let { phone ->
            checkPhoneNumberFormat(login_lost_code_ccp?.selectedCountryCodeWithPlus, phone.text.toString())?.let { phoneNumber ->
                loginPresenter?.let {
                    startLoader()
                    it.sendNewCode(phoneNumber)
                    EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_SEND_NEW_CODE)
                } ?: run {
                    displayToast(R.string.login_error)
                }
            } ?: run {
                displayToast(R.string.login_text_invalid_format)
            }
        }
    }

    private fun returnHome() {
        login_edit_phone_lost_code?.setText("")
        login_block_lost_code_confirmation?.visibility = View.GONE
        login_block_lost_code_start?.visibility = View.VISIBLE
        login_include_lost_code?.visibility = View.GONE
        login_include_signin?.visibility = View.VISIBLE
        showKeyboard(login_edit_phone)
    }

    fun newCodeAsked(user: User?, isOnboarding: Boolean) {
        stopLoader()
        if (user != null) {
            if (isOnboarding) {
                //registerPhoneNumberSent(onboardingUser.getPhone(), true);
                displayToast(R.string.registration_smscode_sent)
            } else {
                if (login_include_lost_code?.visibility == View.VISIBLE) {
                    login_include_lost_code?.visibility = View.GONE
                    login_include_verify_code?.visibility = View.VISIBLE
                } else {
                    displayToast(R.string.login_text_lost_code_ok)
                }
            }
        } else {
            if (isOnboarding || Configuration.showLostCodeErrorToast()) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_03_2)
                displayToast(R.string.login_text_lost_code_ko)
            } else {
                if (login_include_lost_code?.visibility == View.VISIBLE) {
                    //codeConfirmation.setText(R.string.login_text_lost_code_ko);
                    login_text_confirmation?.setHtmlString(R.string.login_text_lost_code_ko_html)
                    login_block_lost_code_start?.visibility = View.GONE
                    login_block_lost_code_confirmation?.visibility = View.VISIBLE
                }
            }
        }
    }

    /************************
     * Email View
     */
    private fun showEmailView() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_4)
        login_include_email?.visibility = View.VISIBLE
        loginPresenter?.authenticationController?.user?.email?.let { email ->
            login_edit_email_profile?.setText(email)
        }
        login_edit_email_profile?.requestFocus()
    }

    private fun onEmailBackClicked() {
        login_edit_email_profile?.setText("")
        onBackPressed()
    }

    private fun saveEmail() {
        loginPresenter?.let {
            EntourageEvents.logEvent(EntourageEvents.EVENT_EMAIL_SUBMIT)
            it.updateUserEmail(login_edit_email_profile?.text.toString())
            it.updateUserToServer()
        } ?: run {
            displayToast(R.string.login_error)
        }
    }

    private fun ignoreEmail() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_EMAIL_IGNORE)
        loginPresenter?.updateUserToServer()
                ?: run  { displayToast(R.string.login_error) }
    }

    /************************
     * Enter Name View
     */
    private fun showNameView() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_5)
        login_include_name?.visibility = View.VISIBLE
        loginPresenter?.authenticationController?.user?.let { user ->
            if (user.firstName != null) {
                login_name_firstname?.setText(user.firstName)
            }
            if (user.lastName != null) {
                login_name_lastname?.setText(user.lastName)
            }
        }
        login_name_firstname?.requestFocus()
    }

    private fun onNameBackClicked() {
        onBackPressed()
    }

    private fun onNameGoClicked() {
        loginPresenter?.let {presenter ->
            EntourageEvents.logEvent(EntourageEvents.EVENT_NAME_SUBMIT)
            val firstname = login_name_firstname?.text.toString()
            val lastname = login_name_lastname?.text.toString()
            if (firstname.isBlank()) {
                displayToast(R.string.login_firstname_error)
                return
            }
            if (lastname.isBlank()) {
                displayToast(R.string.login_lastname_error)
                return
            }
            hideKeyboard()
            presenter.updateUserName(firstname, lastname)
            presenter.authenticationController?.user?.let {user ->
                if (presenter.shouldShowEmailView(user)) {
                    login_include_name?.visibility = View.GONE
                    showEmailView()
                    return
                }
            }
            presenter.updateUserToServer()
        } ?: run {
            displayToast(R.string.login_error)
        }
    }

    fun showPhotoChooseSource() {
        if (isFinishing) return
        try {
            if (loginPresenter?.authenticationController != null) {
                hideKeyboard()
                login_include_email?.visibility = View.GONE
                login_include_name?.visibility = View.GONE
                val avatarURL = loginPresenter?.authenticationController?.user?.avatarURL
                if (avatarURL == null || avatarURL.isEmpty()) {
                    val fragment = PhotoChooseSourceFragmentCompat()
                    fragment.show(supportFragmentManager, PhotoChooseSourceFragmentCompat.TAG)
                } else if (loginPresenter?.shouldShowActionZoneView() == true) {
                    showActionZoneView()
                } else {
                    showNotificationPermissionView()
                }
            } else {
                displayToast(R.string.login_error)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun onUserPhotoUpdated(updated: Boolean) {
        if (isFinishing) return
        try {
            dismissProgressDialog()
            val photoEditFragment = supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
            photoEditFragment?.onPhotoSent(updated)
            if (updated) {
                val fragment = supportFragmentManager.findFragmentByTag(PhotoChooseSourceFragmentCompat.TAG) as PhotoChooseSourceFragmentCompat?
                if (fragment != null && !fragment.isStopped) {
                    fragment.dismiss()
                }
                showActionZoneView()
            } else {
                displayToast(R.string.user_photo_error_not_saved)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    /************************
     * Private Methods
     */
    private fun finishTutorial() {
        //set the tutorial as done
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        (sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String?>?)?.apply {
            this.add(loggedPhoneNumber)
        }.also {
            sharedPreferences.edit().putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, it).apply()
        }
        startMapActivity()
    }

    /************************
     * Startup View
     */
    private fun onStartupLoginClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SPLASH_LOGIN)
        if (loginPresenter != null && loginPresenter!!.shouldShowTermsAndConditions()) {
            onboardingUser = null
            val registerWelcomeFragment = RegisterWelcomeFragment()
            registerWelcomeFragment.show(supportFragmentManager, RegisterWelcomeFragment.TAG)
        } else {
            showLoginScreen()
        }
    }

    private fun showLoginScreen() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_START)
        login_include_startup?.visibility = View.GONE
        login_include_signin?.visibility = View.VISIBLE
        showKeyboard(login_edit_phone)
        onboardingUser = null
    }

    private fun showRegisterScreen() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SPLASH_SIGNUP)
        onboardingUser = User()
        val registerWelcomeFragment = RegisterWelcomeFragment()
        registerWelcomeFragment.isFromChoice = isFromChoice
        registerWelcomeFragment.show(supportFragmentManager, RegisterWelcomeFragment.TAG)
    }

    /************************
     * Verify Code View
     */
    private fun verifyCodeClose() {
        onBackPressed()
    }

    private fun verifyCode() {
        loginPresenter?.login(
                login_lost_code_ccp?.selectedCountryCodeWithPlus,
                login_edit_phone_lost_code?.text.toString(),
                login_verify_code_code?.text.toString()
            ) ?: run {
            displayToast(R.string.login_error)
        }
    }

    private fun showLostCodeScreen() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_03_1)
        hideKeyboard()
        login_verify_code_code?.setText("")
        login_include_startup?.visibility = View.GONE
        login_include_lost_code?.visibility = View.VISIBLE
        showKeyboard(login_edit_phone_lost_code)
    }

    private fun showResendByEmailView() {
        login_verify_code_email?.visibility = View.VISIBLE
    }

    /************************
     * Register
     */
    // OnRegisterUserListener
    override fun registerShowSignIn() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_02_1)
        showLoginScreen()
    }

    //Hack en attendant la nouvelle version de l'onboarding
    override fun registerClosePop(isShowLogin: Boolean) {
        if (!isShowLogin) {
            onBackPressed()
        }
    }

    override fun registerStart(): Boolean {
        if (onboardingUser != null || loginPresenter?.shouldContinueWithRegistration() == true) return true
        showLoginScreen()
        return false
    }

    override fun registerSavePhoneNumber(phoneNumber: String) {
        if (loginPresenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PHONE_SUBMIT)
            loginPresenter!!.registerUserPhone(phoneNumber)
        } else {
            displayToast(R.string.login_error)
        }
    }

    override fun registerCheckCode(smsCode: String) {
        if (loginPresenter != null && onboardingUser != null) {
            loginPresenter?.login(null, onboardingUser!!.phone, smsCode)
        } else {
            displayToast(R.string.login_error)
        }
    }

    override fun registerResendCode() {
        if (loginPresenter != null && onboardingUser != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_03_1)
            loginPresenter?.sendNewCode(onboardingUser!!.phone, true)
        } else {
            displayToast(R.string.login_error)
        }
    }

    fun registerPhoneNumberSent(phoneNumber: String?, smsSent: Boolean) {
        if (isFinishing) return
        val numberFragment = supportFragmentManager.findFragmentByTag(RegisterNumberFragment.TAG) as RegisterNumberFragment?
        numberFragment?.savedPhoneNumber(smsSent)
        if (smsSent) {
            displayToast(R.string.registration_smscode_sent)
            onboardingUser?.phone = phoneNumber
            try {
                val fragment = RegisterSMSCodeFragment()
                fragment.show(supportFragmentManager, RegisterSMSCodeFragment.TAG)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    /************************
     * Notifications View
     */
    fun showNotificationPermissionView() {
        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            finishTutorial()
            return
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_04_3)
        login_include_notifications?.visibility = View.VISIBLE
    }

    private fun onNotificationsIgnore() {
        finishTutorial()
    }

    private fun onNotificationsAccept() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATIONS_ACCEPT)
        finishTutorial()
        val settingsIntent: Intent
        settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            val uri = Uri.fromParts("package", packageName, null)
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setData(uri)
        }
        try {
            startActivity(settingsIntent)
        } catch (ex: ActivityNotFoundException) {
            Timber.e(ex, "Failed to start the activity that shows the app notification settings")
        }
    }

    /************************
     * Action Zone View
     */
    private fun showActionZoneView() {
        if (isFinishing) return
        goToNextActionAfterActionZone = false
        val userPref = loginPresenter?.authenticationController?.userPreferences ?: return
        if (userPref.isIgnoringActionZone) {
            showNotificationPermissionView()
            return
        }
        val me = loginPresenter?.authenticationController?.user
        val actionZoneFragment = UserEditActionZoneFragmentCompat.newInstance(me?.address, false)
        actionZoneFragment.setFragmentListener(this)
        actionZoneFragment.setFromLogin(true)
        try {
            actionZoneFragment.show(supportFragmentManager, UserEditActionZoneFragmentCompat.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun hideActionZoneView() {
        if (isFinishing) return
        try {
            (supportFragmentManager.findFragmentByTag(UserEditActionZoneFragmentCompat.TAG) as UserEditActionZoneFragmentCompat?)?.dismiss()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun onUserEditActionZoneFragmentDismiss() {
        EntourageSnackbar.make(findViewById(R.id.activity_login), R.string.user_setting_ignore_hint, Snackbar.LENGTH_LONG)
                .show()
        if (!goToNextActionAfterActionZone) {
            showNameView()
        } else {
            showNotificationPermissionView()
        }
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        goToNextActionAfterActionZone(true)
    }

    override fun onUserEditActionZoneFragmentAddressSaved() {
        goToNextActionAfterActionZone(false)
    }

    private fun goToNextActionAfterActionZone(ignoreZone: Boolean) {
        loginPresenter?.authenticationController?.userPreferences?.isIgnoringActionZone = ignoreZone
        loginPresenter?.authenticationController?.saveUserPreferences()
        goToNextActionAfterActionZone = true
        hideActionZoneView()
        showNotificationPermissionView()
    }

    /************************
     * Bus Events
     */
    @Subscribe
    fun onShowURLRequested(event: OnShowURLEvent?) {
        if (event == null) return
        showWebView(event.url)
    }

    /************************
     * LoginTextWatcher Class
     */
    internal inner class LoginTextWatcher : TextWatcher {
        private var firstEvent = true
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (firstEvent) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_NAME_TYPE)
                firstEvent = false
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val PERMISSIONS_REQUEST_LOCATION = 1
        const val LOGIN_ERROR_UNAUTHORIZED = -1
        const val LOGIN_ERROR_INVALID_PHONE_FORMAT = -2
        const val LOGIN_ERROR_UNKNOWN = -9998
        const val LOGIN_ERROR_NETWORK = -9999
    }
}