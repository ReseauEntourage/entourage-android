
package social.entourage.android.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_onboarding_main.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.tools.Logger
import social.entourage.android.tools.Utils.checkPhoneNumberFormat
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable
import social.entourage.android.view.CustomProgressDialog
import java.io.File
import java.util.*

/**
 * Created by Jr on 04/05/2020.
 */

class OnboardingMainActivity : AppCompatActivity(),OnboardingCallback {

    val LOGIN_ERROR_UNAUTHORIZED = -1
    val LOGIN_ERROR_INVALID_PHONE_FORMAT = -2
    val LOGIN_ERROR_UNKNOWN = -9998
    val LOGIN_ERROR_NETWORK = -9999

    private var currentFragmentPosition = 1
    private val numberOfSteps = 7

    private var temporaryUser = User()
    private var temporaryCountrycode:String? = null
    private var temporaryPhone:String? = null
    private var temporaryPasscode:String? = null
    private var temporaryEmail:String? = null
    private var temporaryImageUri:Uri? = null

    private var userTypeSelected = UserTypeSelection.NONE

    private var temporaryPlaceAddress:User.Address? = null

    lateinit var alertDialog: CustomProgressDialog

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding_main)

        alertDialog = CustomProgressDialog(this)
        temporaryUser = User()

        if (savedInstanceState == null) {
            changeFragment()
        }

        setupViews()
    }



    override fun onBackPressed() {
        if (currentFragmentPosition == PositionType.Names.pos || currentFragmentPosition == PositionType.Type.pos) {
//            startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
//            finish()
            return
        }
        currentFragmentPosition = currentFragmentPosition - 1
        changeFragment()
    }

    private fun setupViews() {
        ui_bt_next?.setOnClickListener {
            goNext()
        }

        ui_bt_previous?.setOnClickListener {
            goPrevious()
        }

        ui_bt_back?.setOnClickListener {
            startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
            finish()
        }

        ui_bt_pass?.setOnClickListener {
            action_pass()
        }

        ui_bt_previous?.visibility = View.INVISIBLE
        ui_bt_next?.disable()
    }

    //**********//**********//**********
    // Network
    //**********//**********//**********

    fun callSignup() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        OnboardingAPI.getInstance(get()).createUser(temporaryUser) { isOK, error ->
            alertDialog.dismiss()
            if (isOK) {
                showSmsAndGo(R.string.registration_smscode_sent)
            }
            else {
                if (error != null) {
                    if (error.contains("PHONE_ALREADY_EXIST")) {
                        showSmsAndGo(R.string.registration_number_error_already_registered)
                    }
                    else if (error.contains("INVALID_PHONE_FORMAT")) {
                        displayToast(R.string.login_text_invalid_format)
                    }
                    else {
                        displayToast(R.string.login_error_network)
                    }
                    return@createUser
                }
                displayToast(R.string.login_error)
            }
        }
    }

    private fun showSmsAndGo(textId:Int) {
        displayToast(textId)
        temporaryUser.phone = temporaryPhone
        goNextStep()
    }

    fun sendPasscode() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        val phoneNumber = checkPhoneNumberFormat(null, temporaryUser.phone)
        OnboardingAPI.getInstance(get()).login(phoneNumber,temporaryPasscode!!) { isOK, loginResponse, error ->
            if (isOK) {
                val authController = get().entourageComponent.authenticationController
                Logger("Inside login, auth controller : $authController")
                Logger("Inside login, auth controller : ${authController.isAuthenticated}")
                loginResponse?.let { authController.saveUser(loginResponse.user)
                    Logger("Inside login, auth controller after : ${authController.isAuthenticated}")
                }
                authController.saveUserPhoneAndCode(phoneNumber, temporaryPasscode)
                authController.saveUserToursOnly(false)

                //set the tutorial as done
                val sharedPreferences = get().sharedPreferences
                val loggedNumbers = sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>?
                loggedNumbers!!.add(phoneNumber!!)
                sharedPreferences.edit().putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers).apply()
                alertDialog.dismiss()
                goNextStep()
            }
            else {
                alertDialog.dismiss()
                if (error != null) {
                    if (error.contains("INVALID_PHONE_FORMAT")) {
                        showLoginFail(LOGIN_ERROR_INVALID_PHONE_FORMAT)
                        return@login
                    }
                    else if (error.contains("UNAUTHORIZED")) {
                        showLoginFail(LOGIN_ERROR_UNAUTHORIZED)
                        return@login
                    }
                    showLoginFail(LOGIN_ERROR_UNKNOWN)
                    return@login
                }
                showLoginFail(LOGIN_ERROR_NETWORK)
            }
        }
    }

    fun showLoginFail(errorCode: Int) {
        @StringRes val errorMessage: Int
        when (errorCode) {
            LOGIN_ERROR_INVALID_PHONE_FORMAT -> {
                errorMessage = R.string.login_error_invalid_phone_format
            }
            LOGIN_ERROR_UNAUTHORIZED -> {
                errorMessage = R.string.login_error_invalid_credentials
            }
            LOGIN_ERROR_NETWORK -> {
                errorMessage = R.string.login_error_network
            }
            else -> {
                errorMessage = R.string.login_error
            }
        }
        if (!isFinishing) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.login_error_title)
                    .setMessage(errorMessage)
                    .setPositiveButton(R.string.login_retry_label) { dialog, which -> }
                    .create()
                    .show()
        }
    }

    fun resendCode() {
        OnboardingAPI.getInstance(get()).resendCode(temporaryPhone!!) { isOK, loginResponse, error ->
            if (isOK) {
                displayToast(R.string.registration_smscode_sent)
            }
            else {
                if (error != null){
                    displayToast(R.string.login_text_lost_code_ko)
                }
                else {
                    displayToast(R.string.login_text_lost_code_ko)
                }
            }
        }
    }

    fun sendAddress() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        OnboardingAPI.getInstance(get()).updateAddress(temporaryPlaceAddress!!) { isOK, userResponse ->
            if (isOK) {
                val authenticationController = get().entourageComponent.authenticationController
                val me = authenticationController.user
                if (me != null && userResponse != null) {
                    me.address = userResponse.address
                    authenticationController.saveUser(me)
                }
                displayToast(R.string.user_action_zone_send_ok)
                alertDialog.dismiss()
                goNextStep()
            }
            else {
                alertDialog.dismiss()
                displayToast(R.string.user_action_zone_send_failed)
            }
        }
    }

    fun updateUserEmailPwd() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        OnboardingAPI.getInstance(get()).updateUser(temporaryEmail) { isOK, userResponse ->
            Logger("Return update useremail ?")
            if (isOK && userResponse != null) {
                val authenticationController = get().entourageComponent.authenticationController
                authenticationController.saveUser(userResponse.user)
            }
            alertDialog.dismiss()
            goNextStep()
        }
    }

    fun updateUserPhoto() {
        Logger("Send upload Photo Prépare")
        alertDialog.show(R.string.user_photo_uploading)
        OnboardingAPI.getInstance(get()).prepareUploadPhoto { avatarKey, presignedUrl, error ->
            Logger("Send upload Photo Return")
            if (!avatarKey.isNullOrEmpty() && !presignedUrl.isNullOrEmpty()) {
                val path: String? = temporaryImageUri?.getPath()
                if (path == null) return@prepareUploadPhoto
                val file = File(path)
                Logger("Send upload Photo file")
                OnboardingAPI.getInstance(get()).uploadPhotoFile(presignedUrl,file) { isOk ->
                    if (isOk) {
                        updateUserPhoto(avatarKey)
                    }
                    alertDialog.dismiss()
                    goMain()
                }
            }
            else {
                alertDialog.dismiss()
                showErrorUpload()
            }
        }
    }

    fun updateUserPhoto(avatarKey:String) {
        OnboardingAPI.getInstance(get()).updateUserPhoto(avatarKey) { isOK, userResponse ->
            if (isOK && userResponse != null) {
                val authenticationController = get().entourageComponent.authenticationController
                if (authenticationController.isAuthenticated) {
                    authenticationController.saveUser(userResponse.user)
                }
            }
        }
    }

    fun showErrorUpload() {
        Toast.makeText(this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
    }

    //**********//**********//**********
    // Navigation
    //**********//**********//**********

    private fun changeFragment() {
        ui_bt_next?.disable()
        var fragment = Fragment()

        when(currentFragmentPosition) {
            1 -> fragment = OnboardingNamesFragment.newInstance(temporaryUser.firstName,temporaryUser.lastName)
            2 -> fragment = OnboardingPhoneFragment.newInstance(temporaryUser.firstName, temporaryCountrycode, temporaryPhone)
            3 -> fragment = OnboardingPasscodeFragment.newInstance(temporaryPhone)
            4 -> fragment = OnboardingTypeFragment.newInstance(temporaryUser.firstName, userTypeSelected)
            5 -> fragment = OnboardingPlaceFragment.newInstance(temporaryPlaceAddress)
            6 -> fragment = OnboardingEmailPwdFragment.newInstance(temporaryEmail)
            7 -> fragment = OnboardingPhotoFragment.newInstance(temporaryUser.firstName)
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.ui_container, fragment)
                .commit()

        updateButtons()

        val percent = currentFragmentPosition.toFloat() / numberOfSteps.toFloat() * 100
        ui_view_progress?.updatePercent(percent)
    }

    fun goNext() {
        Logger("Call button next pos : $currentFragmentPosition")
        when(currentFragmentPosition) {
            PositionType.Phone.pos -> {callSignup(); return}
            PositionType.Place.pos -> {sendAddress(); return}
            PositionType.EmailPwd.pos -> {updateUserEmailPwd(); return}
            PositionType.Photo.pos -> {updateUserPhoto(); return}
            PositionType.Passcode.pos -> {
                if (temporaryPasscode?.length ?: 0 == 6) {
                    sendPasscode()
                }
                else {
                    displayToast(R.string.onboard_sms_no_pwd)
                }
                return
            }
            else -> {
                currentFragmentPosition = currentFragmentPosition + 1
                if (currentFragmentPosition > numberOfSteps) currentFragmentPosition = numberOfSteps
                changeFragment()
            }
        }
    }

    fun goPrevious() {
        currentFragmentPosition = currentFragmentPosition - 1
        if (currentFragmentPosition < 1) currentFragmentPosition = 1

        changeFragment()
    }

    fun goNextStep() {
        currentFragmentPosition = currentFragmentPosition + 1
        changeFragment()
    }

    fun action_pass() {
        if (currentFragmentPosition == PositionType.Type.pos) {
            userTypeSelected = UserTypeSelection.NONE
        }
        if (currentFragmentPosition == PositionType.Photo.pos) {
            goMain()
            return
        }

        goNext()
    }

    fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun updateButtons() {
        ui_bt_pass?.visibility = View.INVISIBLE
        when(currentFragmentPosition) {
            PositionType.Names.pos, PositionType.Type.pos -> ui_bt_previous?.visibility = View.INVISIBLE
            else -> ui_bt_previous.visibility = View.VISIBLE
        }

        if (currentFragmentPosition == PositionType.Type.pos || currentFragmentPosition == PositionType.Photo.pos ) {
            ui_bt_pass?.visibility = View.VISIBLE
        }
    }

    fun displayToast(messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
    }

    //**********//**********//**********
    // Callbacks
    //**********//**********//**********

    override fun validateNames(firstname: String?, lastname: String?,isValidate:Boolean) {
        temporaryUser.firstName = firstname ?: ""
        temporaryUser.lastName = lastname ?: ""
        Logger("Validarte name : $firstname -- $lastname -- Validate : $isValidate")
        Logger("Validate name from temp : ${temporaryUser.firstName} -- ${temporaryUser.lastName}  - tempuser: $temporaryUser")
        if (isValidate) goNext()
    }

    override fun validatePhoneNumber(prefix: String?, phoneNumber: String?) {
        if (phoneNumber != null) {
            val phoneWithCode = checkPhoneNumberFormat(prefix, phoneNumber)

            if (phoneWithCode != null) {
                temporaryPhone = phoneNumber
                temporaryCountrycode = prefix
                temporaryUser.phone = phoneWithCode
            } else {
                temporaryCountrycode = "+33"
                temporaryPhone = ""
            }
        }
    }

    override fun validatePasscode(password: String) {
        temporaryPasscode = password
    }

    override fun requestNewCode() {
        resendCode()
    }

    override fun updateUsertype(userTypeSelected: UserTypeSelection) {
        this.userTypeSelected = userTypeSelected
    }

    override fun updateAddress(placeAddress: User.Address?) {
        temporaryPlaceAddress = placeAddress
        if (temporaryPlaceAddress != null) {
            upadteButtonNext(true)
        }
        else {
            upadteButtonNext(false)
        }
    }

    override fun updateEmailPwd(email: String?, pwd: String?, pwdConfirm: String?) {
        temporaryEmail = email
        temporaryUser.email = temporaryEmail
    }

    override fun updateUserPhoto(imageUri: Uri?) {
        temporaryImageUri = imageUri
        if (temporaryImageUri != null) {
            upadteButtonNext(true)
        }
        else {
            upadteButtonNext(false)
        }
    }

    override fun upadteButtonNext(isValid:Boolean) {
        if (isValid) {
            ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)
        }
        else {
            ui_bt_next?.disable()
        }
    }
}

//**********//**********//**********
// Enums
//**********//**********//**********

enum class PositionType(val pos:Int) {
    Names(1),
    Phone(2),
    Passcode(3),
    Type(4),
    Place(5),
    EmailPwd(6),
    Photo(7)
}

enum class UserTypeSelection {
    NEIGHBOUR,
    ALONE,
    ASSOS,
    NONE
}

//**********//**********//**********
// Interface
//**********//**********//**********

interface OnboardingCallback {
    fun validateNames(firstname:String?,lastname:String?,isValidate:Boolean)
    fun validatePhoneNumber(prefix: String?, phoneNumber: String?)
    fun validatePasscode(password:String)
    fun requestNewCode()
    fun updateUsertype(userTypeSelected:UserTypeSelection)
    fun updateAddress(placeAddress:User.Address?)
    fun updateEmailPwd(email:String?,pwd:String?,pwdConfirm:String?)
    fun updateUserPhoto(imageUri:Uri?)
    fun upadteButtonNext(isValid:Boolean)
}