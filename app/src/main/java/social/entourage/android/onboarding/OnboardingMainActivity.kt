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
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_onboarding_main.*
import social.entourage.android.*
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.User
import social.entourage.android.authentication.AuthenticationController
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
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.CustomProgressDialog
import timber.log.Timber
import java.util.*

/**
 * Created by Jr on 04/05/2020.
 */

class OnboardingMainActivity : AppCompatActivity(),OnboardingCallback {

    lateinit var authenticationController: AuthenticationController

    val LOGIN_ERROR_UNAUTHORIZED = -1
    val LOGIN_ERROR_INVALID_PHONE_FORMAT = -2
    val LOGIN_ERROR_UNKNOWN = -9998
    val LOGIN_ERROR_NETWORK = -9999

    private var currentFragmentPosition = 1
    private val numberOfSteps = 6

    private var temporaryUser = User()
    private var temporaryCountrycode: String? = null
    private var temporaryPhone: String? = null
    private var temporaryPasscode: String? = null
    private var temporaryEmail: String? = null
    private var temporaryImageUri: Uri? = null

    private var userTypeSelected:UserTypeSelection = UserTypeSelection.NONE
    private var currentPositionAsso = 0
    private var currentPositionAlone = 0
    private var currentPositionNeighbour = 0

    private var temporaryPlaceAddress: User.Address? = null
    private var temporaryAssoInfo: Partner? = null
    private var temporaryAssoActivities: AssoActivities? = null

    private var temporary2ndPlaceAddress: User.Address? = null
    private var temporarySdfActivities: SdfNeighbourActivities? = null
    private var temporaryNeighbourActivities: SdfNeighbourActivities? = null

    lateinit var alertDialog: CustomProgressDialog

    override val errorMessage = MutableLiveData<String>()

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding_main)

        authenticationController = EntourageApplication.get().authenticationController
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
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_PHONE_SUBMIT)
        OnboardingAPI.getInstance().createUser(temporaryUser) { isOK, error ->
            alertDialog.dismiss()
            if (isOK) {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_PHONE_SUBMIT_SUCCESS)
                showSmsAndGo(R.string.login_smscode_sent)
            }
            else {
                if (error != null) {
                    when {
                        error.contains("PHONE_ALREADY_EXIST") -> {
                            showPopAlreadySigned()
                            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_EXIST)
                        }
                        error.contains("INVALID_PHONE_FORMAT") -> {
                            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR)
                            errorMessage.value = getString(R.string.login_text_invalid_format)
                        }
                        else -> {
                            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR)
                            errorMessage.value = getString(R.string.login_error_network)
                        }
                    }
                    return@createUser
                }
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR)
                errorMessage.value = getString(R.string.login_error_network)
            }
        }
    }

    private fun showPopAlreadySigned() {
        AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(R.string.login_already_registered_go_back)
                .setPositiveButton(R.string.button_OK) { dialog, _ ->
                    dialog.dismiss()

                    val intent = Intent(this, PreOnboardingChoiceActivity::class.java)
                    intent.putExtra("isFromOnboarding",true)
                    startActivity(intent)
                    finish()
                }
                .create()
                .show()
    }

    private fun showSmsAndGo(textId: Int) {
        displayToast(textId)
        temporaryUser.phone = temporaryPhone
        goNextStep()
    }

    fun sendPasscode() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_SIGNUP_SUBMIT)
        val phoneNumber = checkPhoneNumberFormat(temporaryCountrycode, temporaryUser.phone ?:"") ?: run {
            showLoginFail(LOGIN_ERROR_INVALID_PHONE_FORMAT)
            return
        }
        OnboardingAPI.getInstance().login(phoneNumber,temporaryPasscode ?: "") { isOK, loginResponse, error ->
            if (isOK) {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_SIGNUP_SUCCESS)
                Timber.d("Inside login, auth controller : $authenticationController")
                loginResponse?.let {
                    authenticationController.saveUser(loginResponse.user)
                }
                authenticationController.saveUserPhoneAndCode(phoneNumber, temporaryPasscode)

                //set the tutorial as done
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                (sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>?)?.let {loggedNumbers ->
                    loggedNumbers.add(phoneNumber)
                    sharedPreferences.edit().putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers).apply()
                }
                alertDialog.dismiss()
                goNextStep()
            }
            else {
                alertDialog.dismiss()
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_ONBOARDING_SINGUP_FAIL)
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

    private fun showLoginFail(errorCode: Int) {
        @StringRes val errorMessage: Int = when (errorCode) {
            LOGIN_ERROR_INVALID_PHONE_FORMAT -> {
                R.string.login_error_invalid_phone_format
            }
            LOGIN_ERROR_UNAUTHORIZED -> {
                R.string.login_error_invalid_credentials
            }
            LOGIN_ERROR_NETWORK -> {
                R.string.login_error_network
            }
            else -> {
                R.string.login_error
            }
        }
        if (!isFinishing) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.login_error_title)
                    .setMessage(errorMessage)
                    .setPositiveButton(R.string.login_retry_label) { _, _ -> }
                    .create()
                    .show()
        }
    }

    fun resendCode() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_SMS)
        temporaryPhone?.let { tempPhone ->
            OnboardingAPI.getInstance().requestNewCode(tempPhone) { isOK, _, _ ->
                if (isOK) {
                    displayToast(R.string.login_smscode_sent)
                    return@requestNewCode
                }
            }
        }
        displayToast(R.string.login_text_lost_code_ko)
    }

    fun sendAddress() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_ACTION_ZONE_SUBMIT)
        temporaryPlaceAddress?.let {
            OnboardingAPI.getInstance().updateAddress(it, false) { isOK, userResponse ->
                if (isOK) {
                    val me = authenticationController.me
                    if (me != null && userResponse != null) {
                        userResponse.user.phone = me.phone
                        authenticationController.saveUser(userResponse.user)
                    }
                    displayToast(R.string.user_action_zone_send_ok)
                    alertDialog.dismiss()
                    goNextStep()
                } else {
                    alertDialog.dismiss()
                    displayToast(R.string.user_action_zone_send_failed)
                }
            }
        }
    }

    fun updateUserEmailPwd() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_EMAIL_SUBMIT)
        OnboardingAPI.getInstance().updateUser(temporaryEmail) { isOK, userResponse ->
            Timber.d("Return update useremail ?")
            if (isOK && userResponse != null) {
                authenticationController.saveUser(userResponse.user)
            }
            else {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_ONBOARDING_EMAIL_SUBMIT_ERROR)
            }
            alertDialog.dismiss()
            goMain()
        }
    }

    private fun updateGoal(isAsso: Boolean) {
        alertDialog.show(R.string.onboard_waiting_dialog)
        val currentGoal = userTypeSelected.getGoalString()

        if (userTypeSelected == UserTypeSelection.NONE) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SKIP)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SIGNUP)
        }

        OnboardingAPI.getInstance().updateUserGoal(currentGoal) { isOK, userResponse ->
            if (isOK && userResponse != null) {
                authenticationController.saveUser(userResponse.user)
            }
            alertDialog.dismiss()
            if (isAsso) {
                currentPositionAsso += 1
                moveToTunnelAsso()
            }
            else {
                updateActivities()
                goNextStep()
            }
        }
    }

    fun updateActivities() {
        val activitiesSelection = SdfNeighbourActivities()
        val isSdf = userTypeSelected == UserTypeSelection.ALONE

        activitiesSelection.setupForSdf(isSdf)
        OnboardingAPI.getInstance().updateUserInterests(activitiesSelection.getArrayForWs()) { isOK, userResponse ->
            if (isOK && userResponse != null) {
                val authenticationController = EntourageApplication.get().authenticationController
                authenticationController.saveUser(userResponse.user)
            }
        }
    }

    //**********
    // Network Asso

    fun updateAssoInfos() {
        if (temporaryAssoInfo?.name?.length ?:0 > 0 && temporaryAssoInfo?.postalCode?.length ?:0 > 0 && temporaryAssoInfo?.userRoleTitle?.length ?:0 > 0) {
            alertDialog.show(R.string.onboard_waiting_dialog)
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUBMIT)
            OnboardingAPI.getInstance().updateAssoInfos(temporaryAssoInfo) { isOK, _ ->
                alertDialog.dismiss()
                if (!isOK) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_ONBOARDING_PRO_SIGNUP_ERROR)
                }
                else {
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUCCESS)
                }
                currentPositionAsso += 1
                moveToTunnelAsso()
            }
        }
        else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.attention_pop_title)
                    .setMessage(R.string.onboard_asso_fill_error)
                    .setPositiveButton("OK") { _, _ -> }
                    .create()
                    .show()
        }
    }

    fun updateAssoActivities() {
        if (temporaryAssoActivities?.hasOneSelectionMin() == true) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_PRO_MOSAIC)
            temporaryAssoActivities?.let {
                OnboardingAPI.getInstance().updateUserInterests(it.getArrayForWs()) { _, _ ->
                    currentFragmentPosition += 2
                    changeFragment()
                }
            }
        }
        else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.attention_pop_title)
                    .setMessage(R.string.onboard_asso_activity_error)
                    .setPositiveButton("OK") { _, _ -> }
                    .create()
                    .show()
        }
    }

    //**********//**********//**********
    // Navigation
    //**********//**********//**********

    private fun changeFragment() {
        ui_bt_next?.disable()
        val fragment = when(currentFragmentPosition) {
            1 -> OnboardingNamesFragment.newInstance(temporaryUser.firstName, temporaryUser.lastName)
            2 -> OnboardingPhoneFragment.newInstance(temporaryUser.firstName, temporaryCountrycode, temporaryPhone)
            3 -> OnboardingPasscodeFragment.newInstance(temporaryCountrycode, temporaryPhone)
            4 -> OnboardingTypeFragment.newInstance(temporaryUser.firstName, userTypeSelected)
            5 -> {
                val isSdf = userTypeSelected == UserTypeSelection.ALONE
                OnboardingPlaceFragment.newInstance(temporaryPlaceAddress, false, isSdf)
            }
            6 -> OnboardingEmailPwdFragment.newInstance(temporaryEmail)
            else -> Fragment()
        }

        if (currentFragmentPosition == PositionType.Passcode.pos || currentFragmentPosition >= PositionType.Type.pos) {
            ui_onboard_main_iv_back?.visibility = View.INVISIBLE
        }
        else {
            ui_onboard_main_iv_back?.visibility = View.VISIBLE
        }
        try {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.ui_container, fragment)
                    .commit()
        } catch (e: IllegalStateException) {
            Timber.e(e)
        }

        updateButtons()

        val percent = currentFragmentPosition.toFloat() / numberOfSteps.toFloat() * 100
        ui_view_progress?.updatePercent(percent)
    }

    //**********
    // Navigation Asso

    private fun moveToTunnelAsso() {
        ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)

        val fragment: Fragment = when(currentPositionAsso) {
            1 -> OnboardingAssoStartFragment.newInstance(true)
            2 -> OnboardingAssoStartFragment.newInstance(false)
            3 -> {
                OnboardingAssoFillFragment.newInstance(temporaryAssoInfo)
            }
            4 -> {
                OnboardingAssoActivitiesFragment.newInstance(temporaryAssoActivities,temporaryUser.firstName)
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

    private fun moveToTunnelAlone() {
        ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)

        val fragment: Fragment = when(currentPositionAlone) {
            1 ->  {
                OnboardingPlaceFragment.newInstance(temporary2ndPlaceAddress, is2ndAddress = true, isSdf = true)
            }
            2 -> OnboardingSdfNeighbourActivitiesFragment.newInstance(temporarySdfActivities,temporaryUser.firstName,true)
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

        val fragment: Fragment = when(currentPositionNeighbour) {
            1 ->  {
                OnboardingPlaceFragment.newInstance(temporary2ndPlaceAddress, is2ndAddress = true, isSdf = false)
            }
            2 -> OnboardingSdfNeighbourActivitiesFragment.newInstance(temporaryNeighbourActivities,temporaryUser.firstName,false)
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
            PositionType.Phone.pos -> {
                callSignup()
                return
            }
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

                    currentPositionAsso += 1
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
                    if (currentPositionAlone == AlonePositionType.NONE.pos) {
                        sendAddress()
                        return
                    }
                    currentPositionAlone += 1
                    moveToTunnelAlone()
                    return
                }

                if (userTypeSelected == UserTypeSelection.NEIGHBOUR) {
                    if (currentPositionNeighbour == NeighbourPositionType.NONE.pos) {
                        sendAddress()
                        return
                    }
                    currentPositionNeighbour += 1
                    moveToTunnelNeighbour()
                    return
                }

                sendAddress(); return
            }
            PositionType.EmailPwd.pos -> {updateUserEmailPwd(); return}
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
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_NAMES)
                currentFragmentPosition += 1
                if (currentFragmentPosition > numberOfSteps) currentFragmentPosition = numberOfSteps
                changeFragment()
            }
        }
    }

    fun goPrevious() {

        if (currentFragmentPosition == PositionType.Type.pos) {
            if (userTypeSelected == UserTypeSelection.ASSOS) {
                if (currentPositionAsso != AssoPositionType.NONE.pos) {
                    currentPositionAsso -= 1
                }
                moveToTunnelAsso()
                return
            }
        }

        if (currentFragmentPosition == PositionType.EmailPwd.pos) {
            if (userTypeSelected == UserTypeSelection.ASSOS) {
                if (currentPositionAsso != AssoPositionType.NONE.pos) {
                    currentFragmentPosition -= 2
                }
                moveToTunnelAsso()
                return
            }
        }

        currentFragmentPosition -= 1
        if (currentFragmentPosition < 1) currentFragmentPosition = 1

        changeFragment()
    }

    fun goNextStep() {
        currentFragmentPosition += 1
        changeFragment()
    }

    fun goMain() {
        authenticationController.me?.let { me ->
            OnboardingAPI.getInstance().getUser(me.id) { isOK, userResponse ->
                if (isOK) {
                    if (userResponse != null) {
                        userResponse.user.phone = me.phone
                        authenticationController.saveUser(userResponse.user)
                    }
                }
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                sharedPreferences.edit().putInt(EntourageApplication.KEY_ONBOARDING_USER_TYPE, userTypeSelected.pos).apply()
                sharedPreferences.edit().putBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, true).apply()
                sharedPreferences.edit().putBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN,false).apply()
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
                  //  ui_bt_pass?.visibility = View.VISIBLE
                } else {
                    ui_bt_previous?.visibility = View.VISIBLE
                }
            }
            PositionType.Place.pos -> {
                ui_bt_previous?.visibility = View.VISIBLE
            }
            PositionType.EmailPwd.pos -> {
                ui_bt_previous?.visibility = View.VISIBLE
                ui_bt_pass?.visibility = View.VISIBLE
            }
            else -> ui_bt_previous?.visibility = View.VISIBLE
        }
    }

    fun action_pass() {
        if (currentFragmentPosition == PositionType.EmailPwd.pos) {
            goMain()
        }
    }

    fun displayToast(messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
    }

    //**********//**********//**********
    // Callbacks
    //**********//**********//**********

    override fun validateNames(firstname: String?, lastname: String?, isValidate: Boolean) {
        temporaryUser.firstName = firstname ?: ""
        temporaryUser.lastName = lastname ?: ""
        Timber.d("Validate name : $firstname -- $lastname -- Validate : $isValidate")
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

    override fun updateButtonNext(isValid: Boolean) {
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
            this.temporarySdfActivities?.setupForSdf(true)
        }
        else {
            this.temporaryNeighbourActivities = sdfNeighbourActivities
            this.temporaryNeighbourActivities?.setupForSdf(false)
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
    EmailPwd(6)
}

enum class AssoPositionType(val pos:Int) {
    START(1),
    INFO(2),
    FILL(3),
    ACTIVITIES(4),
    NONE(0)
}
enum class AlonePositionType(val pos:Int) {
    NONE(0)
}
enum class NeighbourPositionType(val pos:Int) {
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
    val errorMessage: MutableLiveData<String>

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
