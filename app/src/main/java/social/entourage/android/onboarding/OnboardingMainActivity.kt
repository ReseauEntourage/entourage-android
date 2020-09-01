
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
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.User
import social.entourage.android.onboarding.asso.AssoActivities
import social.entourage.android.onboarding.asso.OnboardingAssoActivitiesFragment
import social.entourage.android.onboarding.asso.OnboardingAssoFillFragment
import social.entourage.android.onboarding.asso.OnboardingAssoStartFragment
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.onboarding.sdf_neighbour.OnboardingSdfNeighbourActivitiesFragment
import social.entourage.android.onboarding.sdf_neighbour.SdfNeighbourActivities
import social.entourage.android.tools.Utils.checkPhoneNumberFormat
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable
import social.entourage.android.tools.view.CustomProgressDialog
import timber.log.Timber
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

    private var userTypeSelected:UserTypeSelection = UserTypeSelection.NONE
    private var currentPositionAsso = 0
    private var currentPositionAlone = 0
    private var currentPositionNeighbour = 0

    private var temporaryPlaceAddress:User.Address? = null
    private var temporaryAssoInfo:Partner? = null
    private var temporaryAssoActivities: AssoActivities? = null

    private var temporary2ndPlaceAddress:User.Address? = null
    private var temporarySdfActivities: SdfNeighbourActivities? = null
    private var temporaryNeighbourActivities: SdfNeighbourActivities? = null

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
        if (currentFragmentPosition == PositionType.Names.pos || (currentFragmentPosition == PositionType.Type.pos && userTypeSelected == UserTypeSelection.NONE)) {
            return
        }

        goPrevious()
    }

    private fun setupViews() {
        ui_bt_next?.setOnClickListener {
            if (currentPositionNeighbour == NeighbourPositionType.PLACE.pos || currentPositionAlone == AlonePositionType.PLACE.pos) {
                action_pass()
                return@setOnClickListener
            }

            goNext()
        }

        ui_bt_previous?.setOnClickListener {
            goPrevious()
        }

        ui_bt_back?.setOnClickListener {
            if (currentFragmentPosition >= PositionType.Type.pos) return@setOnClickListener
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
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_PHONE_SUBMIT)
        OnboardingAPI.getInstance(get()).createUser(temporaryUser) { isOK, error ->
            alertDialog.dismiss()
            if (isOK) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_PHONE_SUBMIT_SUCCESS)
                showSmsAndGo(R.string.registration_smscode_sent)
            }
            else {
                if (error != null) {
                    if (error.contains("PHONE_ALREADY_EXIST")) {
                        showPopAlreadySigned()
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_EXIST)
                    }
                    else if (error.contains("INVALID_PHONE_FORMAT")) {
                        displayToast(R.string.login_text_invalid_format)
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR)
                    }
                    else {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR)
                        displayToast(R.string.login_error_network)
                    }
                    return@createUser
                }
                EntourageEvents.logEvent(EntourageEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR)
                displayToast(R.string.login_error)
            }
        }
    }

    private fun showPopAlreadySigned() {
        AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(R.string.alreadyRegistereMessageGoBack)
                .setPositiveButton(R.string.button_OK) { dialog, which ->
                    dialog.dismiss()

                    val intent = Intent(this, PreOnboardingChoiceActivity::class.java)
                    intent.putExtra("isFromOnboarding",true)
                    startActivity(intent)
                    finish()
                }
                .create()
                .show()
    }

    private fun showSmsAndGo(textId:Int) {
        displayToast(textId)
        temporaryUser.phone = temporaryPhone
        goNextStep()
    }

    fun sendPasscode() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_SIGNUP_SUBMIT)
        val phoneNumber = checkPhoneNumberFormat(null, temporaryUser.phone ?:"") ?: run {
            showLoginFail(LOGIN_ERROR_INVALID_PHONE_FORMAT)
            return
        }
        OnboardingAPI.getInstance(get()).login(phoneNumber,temporaryPasscode ?: "") { isOK, loginResponse, error ->
            if (isOK) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_SIGNUP_SUCCESS)
                val authController = get().entourageComponent.authenticationController
                Timber.d("Inside login, auth controller : $authController")
                loginResponse?.let {
                    authController.saveUser(loginResponse.user)
                }
                authController.saveUserPhoneAndCode(phoneNumber, temporaryPasscode)
                authController.saveUserToursOnly(false)

                //set the tutorial as done
                val sharedPreferences = get().sharedPreferences
                (sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>?)?.let {loggedNumbers ->
                    loggedNumbers.add(phoneNumber)
                    sharedPreferences.edit().putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers).apply()
                }
                alertDialog.dismiss()
                goNextStep()
            }
            else {
                alertDialog.dismiss()
                EntourageEvents.logEvent(EntourageEvents.EVENT_ERROR_ONBOARDING_SINGUP_FAIL)
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
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_SMS)
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
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_ACTION_ZONE_SUBMIT)
        OnboardingAPI.getInstance(get()).updateAddress(temporaryPlaceAddress!!,false) { isOK, userResponse ->
            if (isOK) {
                val authenticationController = get().entourageComponent.authenticationController
                val me = authenticationController.me
                if (me != null && userResponse != null) {
                    userResponse.user.phone = me.phone
                    authenticationController.saveUser(userResponse.user)
                }
                displayToast(R.string.user_action_zone_send_ok)
                alertDialog.dismiss()
                if (userTypeSelected == UserTypeSelection.NONE) {
                    goNextStep()
                }
                else {
                    goNextStepSdfNeighbour()
                }
            }
            else {
                alertDialog.dismiss()
                displayToast(R.string.user_action_zone_send_failed)
            }
        }
    }

    fun updateUserEmailPwd() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_EMAIL_SUBMIT)
        OnboardingAPI.getInstance(get()).updateUser(temporaryEmail) { isOK, userResponse ->
            Timber.d("Return update useremail ?")
            if (isOK && userResponse != null) {
                val authenticationController = get().entourageComponent.authenticationController
                authenticationController.saveUser(userResponse.user)
            }
            else {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ERROR_ONBOARDING_EMAIL_SUBMIT_ERROR)
            }
            alertDialog.dismiss()
            goNextStep()
        }
    }

    fun updateUserPhoto() {
        Timber.d("Send upload Photo Prépare")
        alertDialog.show(R.string.user_photo_uploading)
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_PHOTO_SUBMIT)
        OnboardingAPI.getInstance(get()).prepareUploadPhoto { avatarKey, presignedUrl, error ->
            Timber.d("Send upload Photo Return")
            if (!avatarKey.isNullOrEmpty() && !presignedUrl.isNullOrEmpty()) {
                val path: String? = temporaryImageUri?.getPath()
                if (path == null) return@prepareUploadPhoto
                val file = File(path)
                Timber.d("Send upload Photo file")
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

    fun updateGoal(isAsso:Boolean) {
        alertDialog.show(R.string.onboard_waiting_dialog)
        val _currentGoal = userTypeSelected.getGoalString()

        if (userTypeSelected == UserTypeSelection.NONE) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SKIP)
        }
        else {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SIGNUP)
        }

        OnboardingAPI.getInstance(get()).updateUserGoal(_currentGoal) { isOK, userResponse ->
            if (isOK && userResponse != null) {
                val authenticationController = get().entourageComponent.authenticationController
                authenticationController.saveUser(userResponse.user)
            }
            alertDialog.dismiss()
            if (isAsso) {
                currentPositionAsso = currentPositionAsso + 1
                moveToTunnelAsso()
            }
            else {
                goNextStep()
            }
        }
    }

    //**********
    // Network Asso

    fun updateAssoInfos() {
        if (temporaryAssoInfo?.name?.length ?:0 > 0 && temporaryAssoInfo?.postalCode?.length ?:0 > 0 && temporaryAssoInfo?.userRoleTitle?.length ?:0 > 0) {
            alertDialog.show(R.string.onboard_waiting_dialog)
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUBMIT)
            OnboardingAPI.getInstance(get()).updateAssoInfos(temporaryAssoInfo) { isOK, response ->
                alertDialog.dismiss()
                if (!isOK) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ERROR_ONBOARDING_PRO_SIGNUP_ERROR)
                }
                else {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUCCESS)
                }
                currentPositionAsso = currentPositionAsso + 1
                moveToTunnelAsso()
            }
        }
        else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.attention_pop_title)
                    .setMessage(R.string.onboard_asso_fill_error)
                    .setPositiveButton("OK") { dialog, which -> }
                    .create()
                    .show()
        }
    }

    fun updateAssoActivities() {
        if (temporaryAssoActivities?.hasOneSelectionMin() == true) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_PRO_MOSAIC)
            OnboardingAPI.getInstance(get()).updateUserInterests(temporaryAssoActivities!!.getArrayForWs()) { isOK, userResponse ->
                currentFragmentPosition = currentFragmentPosition + 2
                changeFragment()
            }
        }
        else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.attention_pop_title)
                    .setMessage(R.string.onboard_asso_activity_error)
                    .setPositiveButton("OK") { dialog, which -> }
                    .create()
                    .show()
        }
    }

    fun update2ndAddress() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_ACTION_ZONE2_SUBMIT)
        OnboardingAPI.getInstance(get()).updateAddress(temporary2ndPlaceAddress!!,true) { isOK, userResponse ->
            if (isOK) {
                val authenticationController = get().entourageComponent.authenticationController
                val me = authenticationController.me
                if (me != null && userResponse != null) {
                    userResponse.user.phone = me.phone
                    authenticationController.saveUser(userResponse.user)
                }
                displayToast(R.string.user_action_zone_send_ok)
                alertDialog.dismiss()
                goNextStepSdfNeighbour()
            }
            else {
                alertDialog.dismiss()
                displayToast(R.string.user_action_zone_send_failed)
            }
        }
    }

    fun updateAlone() {
        updateActivities(temporarySdfActivities,true)
    }

    fun updateNeighbour() {
        updateActivities(temporaryNeighbourActivities,false)
    }

    fun updateActivities(activities:SdfNeighbourActivities?,isSdf:Boolean) {
        if (activities?.hasOneSelectionMin() == true) {
            if (isSdf) { EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_INNEED_MOSAIC) }
            else { EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_NEIGHBOR_MOSAIC) }

            OnboardingAPI.getInstance(get()).updateUserInterests(activities.getArrayForWs()) { isOK, userResponse ->
                currentFragmentPosition = currentFragmentPosition + 1
                changeFragment()
            }
        }
        else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.attention_pop_title)
                    .setMessage(R.string.onboard_asso_activity_error)
                    .setPositiveButton("OK") { dialog, which -> }
                    .create()
                    .show()
        }
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
            3 -> fragment = OnboardingPasscodeFragment.newInstance(temporaryCountrycode,temporaryPhone)
            4 -> fragment = OnboardingTypeFragment.newInstance(temporaryUser.firstName, userTypeSelected)
            5 -> {
                val isSdf = userTypeSelected == UserTypeSelection.ALONE
                fragment = OnboardingPlaceFragment.newInstance(temporaryPlaceAddress, false, isSdf)
            }
            6 -> fragment = OnboardingEmailPwdFragment.newInstance(temporaryEmail)
            7 -> fragment = OnboardingPhotoFragment.newInstance(temporaryUser.firstName ?: "")
        }

        if (currentFragmentPosition == PositionType.Passcode.pos || currentFragmentPosition >= PositionType.Type.pos) {
            ui_onboard_main_iv_back?.visibility = View.INVISIBLE
        }
        else {
            ui_onboard_main_iv_back?.visibility = View.VISIBLE
        }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.ui_container, fragment)
                .commit()

        updateButtons()

        val percent = currentFragmentPosition.toFloat() / numberOfSteps.toFloat() * 100
        ui_view_progress?.updatePercent(percent)
    }

    //**********
    // Navigation Asso

    fun moveToTunnelAsso() {
        ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)
        val fragment: Fragment

        when(currentPositionAsso) {
            1 -> fragment = OnboardingAssoStartFragment.newInstance(true)
            2 -> fragment = OnboardingAssoStartFragment.newInstance(false)
            3 -> {
                fragment = OnboardingAssoFillFragment.newInstance(temporaryAssoInfo)
            }
            4 -> {
                fragment = OnboardingAssoActivitiesFragment.newInstance(temporaryAssoActivities,temporaryUser.firstName)
            }
            else -> {
                changeFragment()
                return
            }
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.ui_container, fragment)
                .commit()

        updateButtons()

        val percent = (currentFragmentPosition.toFloat() + currentPositionAsso.toFloat() + 1.5f ) / (numberOfSteps.toFloat() + 4.5f) * 100
        ui_view_progress?.updatePercent(percent)
    }

    fun moveToTunnelAlone() {
        ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)
        val fragment: Fragment

        when(currentPositionAlone) {
            1 ->  {
                fragment = OnboardingPlaceFragment.newInstance(temporary2ndPlaceAddress,true,true)
            }
            2 -> fragment = OnboardingSdfNeighbourActivitiesFragment.newInstance(temporarySdfActivities,temporaryUser.firstName,true)
            else -> {
                changeFragment()
                return
            }
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.ui_container, fragment)
                .commit()

        updateButtons()

        val percent = (currentFragmentPosition.toFloat() + currentPositionAlone.toFloat() ) / (numberOfSteps.toFloat() + currentPositionAlone.toFloat()) * 100
        ui_view_progress?.updatePercent(percent)
    }

    fun moveToTunnelNeighbour() {
        ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)
        val fragment: Fragment

        when(currentPositionNeighbour) {
            1 ->  {
                fragment = OnboardingPlaceFragment.newInstance(temporary2ndPlaceAddress,true,false)
            }
            2 -> fragment = OnboardingSdfNeighbourActivitiesFragment.newInstance(temporaryNeighbourActivities,temporaryUser.firstName,false)
            else -> {
                changeFragment()
                return
            }
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.ui_container, fragment)
                .commit()

        updateButtons()

        val percent = (currentFragmentPosition.toFloat() + currentPositionNeighbour.toFloat() ) / (numberOfSteps.toFloat() + currentPositionNeighbour.toFloat()) * 100
        ui_view_progress?.updatePercent(percent)
    }

    //**********
    // Navigation Methods

    fun goNext() {
        when(currentFragmentPosition) {
            PositionType.Phone.pos -> {callSignup(); return}
            PositionType.Type.pos -> {
                if (userTypeSelected == UserTypeSelection.ASSOS) {
                    if (currentPositionAsso == AssoPositionType.NONE.pos) {
                        updateGoal(true)
                        return
                    }

                    if (currentPositionAsso == AssoPositionType.FILL.pos) {
                        updateAssoInfos()
                        return
                    }

                    if (currentPositionAsso == AssoPositionType.ACTIVITIES.pos) {
                        updateAssoActivities()
                        return
                    }

                    currentPositionAsso = currentPositionAsso+ 1
                    moveToTunnelAsso()
                    return
                }
                else {
                    updateGoal(false)
                    return
                }
            }
            PositionType.Place.pos -> {
                if (userTypeSelected == UserTypeSelection.ALONE) {
                    if (currentPositionAlone == AlonePositionType.PLACE.pos) {
                        update2ndAddress()
                        return
                    }
                    if (currentPositionAlone == AlonePositionType.ACTIVITIES.pos) {
                        updateAlone()
                        return
                    }
                    if (currentPositionAlone == AlonePositionType.NONE.pos) {
                        sendAddress()
                        return
                    }
                    currentPositionAlone = currentPositionAlone + 1
                    moveToTunnelAlone()
                    return
                }

                if (userTypeSelected == UserTypeSelection.NEIGHBOUR) {
                    if (currentPositionNeighbour == NeighbourPositionType.PLACE.pos) {
                        update2ndAddress()
                        return
                    }
                    if (currentPositionNeighbour == NeighbourPositionType.ACTIVITIES.pos) {
                        updateNeighbour()
                        return
                    }
                    if (currentPositionNeighbour == NeighbourPositionType.NONE.pos) {
                        sendAddress()
                        return
                    }
                    currentPositionNeighbour = currentPositionNeighbour + 1
                    moveToTunnelNeighbour()
                    return
                }

                sendAddress(); return
            }
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
                EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_NAMES)
                currentFragmentPosition = currentFragmentPosition + 1
                if (currentFragmentPosition > numberOfSteps) currentFragmentPosition = numberOfSteps
                changeFragment()
            }
        }
    }

    fun goPrevious() {

        if (currentFragmentPosition == PositionType.Type.pos) {
            if (userTypeSelected == UserTypeSelection.ASSOS) {
                if (currentPositionAsso != AssoPositionType.NONE.pos) {
                    currentPositionAsso = currentPositionAsso - 1
                }
                moveToTunnelAsso()
                return
            }
        }

        if (currentFragmentPosition == PositionType.Place.pos) {
            if (userTypeSelected == UserTypeSelection.ALONE) {
                if (currentPositionAlone != AlonePositionType.NONE.pos) {
                    currentPositionAlone = currentPositionAlone - 1
                }
                else {
                    currentFragmentPosition = currentFragmentPosition - 1
                }
                moveToTunnelAlone()
                return
            }
            if (userTypeSelected == UserTypeSelection.NEIGHBOUR) {
                if (currentPositionNeighbour != NeighbourPositionType.NONE.pos) {
                    currentPositionNeighbour = currentPositionNeighbour - 1
                }
                else {
                    currentFragmentPosition = currentFragmentPosition - 1
                }
                moveToTunnelNeighbour()
                return
            }
        }

        if (currentFragmentPosition == PositionType.EmailPwd.pos) {
            if (userTypeSelected == UserTypeSelection.ASSOS) {
                if (currentPositionAsso != AssoPositionType.NONE.pos) {
                    currentFragmentPosition = currentFragmentPosition - 2
                }
                moveToTunnelAsso()
                return
            }
            if (userTypeSelected == UserTypeSelection.ALONE) {
                if (currentPositionAlone != AlonePositionType.NONE.pos) {
                    currentFragmentPosition = currentFragmentPosition - 1
                }
                moveToTunnelAlone()
                return
            }
            if (userTypeSelected == UserTypeSelection.NEIGHBOUR) {
                if (currentPositionNeighbour != NeighbourPositionType.NONE.pos) {
                    currentFragmentPosition = currentFragmentPosition - 1
                }
                moveToTunnelNeighbour()
                return
            }
        }

        currentFragmentPosition = currentFragmentPosition - 1
        if (currentFragmentPosition < 1) currentFragmentPosition = 1

        changeFragment()
    }

    fun goNextStep() {
        currentFragmentPosition = currentFragmentPosition + 1
        changeFragment()
    }

    fun goNextStepSdfNeighbour() {
        if (userTypeSelected == UserTypeSelection.ALONE) {
            currentPositionAlone = currentPositionAlone + 1
            moveToTunnelAlone()
        }
        else {
            currentPositionNeighbour = currentPositionNeighbour + 1
            moveToTunnelNeighbour()
        }
    }

    fun action_pass() {
        if (currentFragmentPosition == PositionType.Place.pos) {
            if ((userTypeSelected == UserTypeSelection.ALONE && currentPositionAlone == AlonePositionType.PLACE.pos) ||
                    (userTypeSelected == UserTypeSelection.NEIGHBOUR && currentPositionNeighbour == NeighbourPositionType.PLACE.pos)) {
                temporary2ndPlaceAddress = null
                temporarySdfActivities = null
                temporaryNeighbourActivities = null
                EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_ACTION_ZONE2_SKIP)
                goNextStepSdfNeighbour()
                return
            }
        }

        if (currentFragmentPosition == PositionType.Type.pos) {
            userTypeSelected = UserTypeSelection.NONE
        }
        if (currentFragmentPosition == PositionType.Photo.pos) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_IGNORE_PHOTO)
            goMain()
            return
        }

        goNext()
    }

    fun goMain() {
        val authenticationController = get().entourageComponent.authenticationController
        authenticationController.me?.let { me ->
            OnboardingAPI.getInstance(get()).getUser(me.id) { isOK, userResponse ->
                if (isOK) {
                    if (userResponse != null) {
                        userResponse.user.phone = me.phone
                        authenticationController.saveUser(userResponse.user)
                    }
                }
                val sharedPreferences = get().sharedPreferences
                sharedPreferences.edit().putInt(EntourageApplication.KEY_ONBOARDING_USER_TYPE, userTypeSelected.pos).apply()
                sharedPreferences.edit().putBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, true).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    fun updateButtons() {
        ui_bt_pass?.visibility = View.INVISIBLE
        when(currentFragmentPosition) {
            PositionType.Names.pos -> ui_bt_previous?.visibility = View.INVISIBLE
            PositionType.Type.pos -> {
                if (currentPositionAsso == AssoPositionType.NONE.pos) {
                    ui_bt_previous?.visibility = View.INVISIBLE
                    ui_bt_pass?.visibility = View.VISIBLE
                } else {
                    ui_bt_previous?.visibility = View.VISIBLE
                }
            }
            PositionType.Place.pos -> {
                if ((userTypeSelected == UserTypeSelection.ALONE && currentPositionAlone == AlonePositionType.PLACE.pos) ||
                        (userTypeSelected == UserTypeSelection.NEIGHBOUR && currentPositionNeighbour == NeighbourPositionType.PLACE.pos)) {
                    ui_bt_pass?.visibility = View.VISIBLE
                }
                ui_bt_previous?.visibility = View.VISIBLE
            }
            else -> ui_bt_previous?.visibility = View.VISIBLE
        }

        if (currentFragmentPosition == PositionType.Photo.pos ) {
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
        Timber.d("Validarte name : $firstname -- $lastname -- Validate : $isValidate")
        Timber.d("Validate name from temp : ${temporaryUser.firstName} -- ${temporaryUser.lastName}  - tempuser: $temporaryUser")
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

        temporaryAssoInfo = null
        temporaryAssoActivities?.reset()
        temporarySdfActivities?.reset()
        temporaryNeighbourActivities?.reset()
        temporaryEmail = ""
        temporaryPlaceAddress = null
        temporary2ndPlaceAddress = null
    }

    override fun updateAddress(placeAddress: User.Address?,is2ndAddress:Boolean) {
        if (is2ndAddress) {
            temporary2ndPlaceAddress= placeAddress
            updateButtonNext(true)
        }
        else {
            temporaryPlaceAddress = placeAddress
            if (placeAddress != null) {
                updateButtonNext(true)
            }
            else {
                updateButtonNext(false)
            }
        }


    }

    override fun updateEmailPwd(email: String?, pwd: String?, pwdConfirm: String?) {
        temporaryEmail = email
        temporaryUser.email = temporaryEmail
    }

    override fun updateUserPhoto(imageUri: Uri?) {
        temporaryImageUri = imageUri
        if (temporaryImageUri != null) {
            updateButtonNext(true)
        }
        else {
            updateButtonNext(false)
        }
    }

    override fun updateButtonNext(isValid:Boolean) {
        if (isValid) {
            ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)
        }
        else {
            ui_bt_next?.disable()
        }
    }

    override fun goPreviousManually() {
        temporaryPhone = null
        goPrevious()
    }

    override fun goNextManually() {
        goNext()
    }

    //**********
    // Callbacks Asso

    override fun updateAssoInfos(asso: Partner?) {
        this.temporaryAssoInfo = asso
    }

    override fun updateAssoActivities(assoActivities: AssoActivities) {
        this.temporaryAssoActivities = assoActivities
    }

    override fun updateSdfNeighbourActivities(sdfNeighbourActivities: SdfNeighbourActivities,isSdf:Boolean) {
        if (isSdf) {
            this.temporarySdfActivities = sdfNeighbourActivities
        }
        else {
            this.temporaryNeighbourActivities = sdfNeighbourActivities
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

enum class AssoPositionType(val pos:Int) {
    START(1),
    INFO(2),
    FILL(3),
    ACTIVITIES(4),
    NONE(0)
}
enum class AlonePositionType(val pos:Int) {
    PLACE(1),
    ACTIVITIES(2),
    NONE(0)
}
enum class NeighbourPositionType(val pos:Int) {
    PLACE(1),
    ACTIVITIES(2),
    NONE(0)
}

enum class UserTypeSelection(val pos:Int) {
    NEIGHBOUR(1),
    ALONE(2),
    ASSOS(3),
    NONE(0);

    fun getGoalString() : String {
       return when(this) {
           NEIGHBOUR -> User.USER_GOAL_NEIGHBOUR
           ALONE -> User.USER_GOAL_ALONE
           ASSOS -> User.USER_GOAL_ASSO
           NONE -> User.USER_GOAL_NONE
       }
    }
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
    fun updateAddress(placeAddress:User.Address?,is2ndAddress:Boolean)
    fun updateEmailPwd(email:String?,pwd:String?,pwdConfirm:String?)
    fun updateUserPhoto(imageUri:Uri?)
    fun updateButtonNext(isValid:Boolean)
    fun goPreviousManually()

    fun updateAssoInfos(asso:Partner?)
    fun updateAssoActivities(assoActivities:AssoActivities)
    fun updateSdfNeighbourActivities(sdfNeighbourActivities:SdfNeighbourActivities,isSdf: Boolean)

    fun goNextManually()
}
