package social.entourage.android.onboarding.onboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_onboarding_start.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.tools.utils.Utils
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable
import social.entourage.android.tools.view.CustomProgressDialog
import social.entourage.android.tools.view.countrycodepicker.Country
import timber.log.Timber

class OnboardingStartActivity : AppCompatActivity(), OnboardingStartCallback {
    private val LOGIN_ERROR_UNAUTHORIZED = -1
    private val LOGIN_ERROR_INVALID_PHONE_FORMAT = -2
    private val LOGIN_ERROR_UNKNOWN = -9998
    private val LOGIN_ERROR_NETWORK = -9999

    lateinit var authenticationController: AuthenticationController
    lateinit var alertDialog: CustomProgressDialog

    private var currentFragmentPosition = 1
    private val numberOfSteps = 3

    private var temporaryUser = User()
    private var temporaryCountrycode: Country? = null
    private var temporaryPhone: String? = null
    private var temporaryPasscode: String? = null
    private var temporaryEmail: String? = null
    private var hasConsent = false

    private var isEntour = false
    private var isBeEntour = false
    private var isAsso = false
    private var temporaryPlaceAddress: User.Address? = null

    val errorMessage = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_start)

        temporaryCountrycode = Country(getString(R.string.country_france_code),
            getString(R.string.country_france_number),
            getString(R.string.country_france_name),
            getString(R.string.country_france_flag))

        authenticationController = EntourageApplication.get().authenticationController
        alertDialog = CustomProgressDialog(this)
        temporaryUser = User()

        if (savedInstanceState == null) {
            changeFragment()
        }
        setupViews()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentFragmentPosition >= numberOfSteps) return

        goPrevious()
    }

    /********
     * Network
     */

    private fun callSignup() {
        alertDialog.show(R.string.onboard_waiting_dialog)

        OnboardingAPI.getInstance().createUser(temporaryUser) { isOK, error ->
            alertDialog.dismiss()
            if (isOK) {
                showSmsAndGo(R.string.login_smscode_sent)
            } else {
                if (error != null) {
                    when {
                        error.contains("PHONE_ALREADY_EXIST") -> {
                            showPopAlreadySigned()
                        }
                        error.contains("INVALID_PHONE_FORMAT") -> {
                            errorMessage.value = getString(R.string.login_text_invalid_format)
                        }
                        else -> {
                            errorMessage.value = getString(R.string.login_error_network)
                        }
                    }
                    return@createUser
                }
                errorMessage.value = getString(R.string.login_error_network)
            }
        }
    }

    private fun sendPasscode() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        val phoneNumber =
            Utils.checkPhoneNumberFormat(temporaryCountrycode?.phoneCode, temporaryUser.phone ?: "") ?: run {
                showLoginFail(LOGIN_ERROR_INVALID_PHONE_FORMAT)
                return
            }
        OnboardingAPI.getInstance()
            .login(phoneNumber, temporaryPasscode ?: "") { isOK, loginResponse, error ->
                if (isOK) {
                    Timber.d("Inside login, auth controller : $authenticationController")
                    loginResponse?.let {
                        authenticationController.saveUser(loginResponse.user)
                    }
                    authenticationController.saveUserPhoneAndCode(phoneNumber, temporaryPasscode)

                    //set the tutorial as done
                    val sharedPreferences = EntourageApplication.get().sharedPreferences
                    (sharedPreferences.getStringSet(
                        EntourageApplication.KEY_TUTORIAL_DONE,
                        HashSet()
                    ) as HashSet<String>?)?.let { loggedNumbers ->
                        loggedNumbers.add(phoneNumber)
                        sharedPreferences.edit()
                            .putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers)
                            .apply()
                    }
                    alertDialog.dismiss()
                    goNextStep()
                } else {
                    alertDialog.dismiss()
                    if (error != null) {
                        if (error.contains("INVALID_PHONE_FORMAT")) {
                            showLoginFail(LOGIN_ERROR_INVALID_PHONE_FORMAT)
                            return@login
                        } else if (error.contains("UNAUTHORIZED")) {
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

    private fun resendCode() {
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

    private fun updateAddress() {
        alertDialog.show(R.string.onboard_waiting_dialog)

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
                    goEnd()
                } else {
                    alertDialog.dismiss()
                    displayToast(R.string.user_action_zone_send_failed)
                }
            }
        }
    }

    private fun updateGoal() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        var userType = UserTypeSelection.NEIGHBOUR
        if (isAsso) {
            userType = UserTypeSelection.ASSOS
        }
        else if (isBeEntour) {
            userType = UserTypeSelection.ALONE
        }

        val currentGoal = userType.getGoalString()

        OnboardingAPI.getInstance().updateUserGoal(currentGoal,temporaryEmail,hasConsent) { isOK, userResponse ->
            if (isOK && userResponse != null) {
                authenticationController.saveUser(userResponse.user)
            }
            alertDialog.dismiss()

            updateAddress()
        }
    }

    /***********
     * Network Messages + actions
     */

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

    private fun showPopAlreadySigned() {
        AlertDialog.Builder(this)
            .setTitle("")
            .setMessage(R.string.login_already_registered_go_back)
            .setPositiveButton(R.string.button_OK) { dialog, _ ->
                dialog.dismiss()

                val intent = Intent(this, PreOnboardingChoiceActivity::class.java)
                intent.putExtra("isFromOnboarding", true)
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

    fun displayToast(messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
    }

    /***********
     * Navigation
     */

    private fun goNext() {
        Timber.d("***** Go next = $currentFragmentPosition")
        when (currentFragmentPosition) {
            PositionsType.NamesPhone.pos -> {
                callSignup()
            }
            PositionsType.Passcode.pos -> {
                sendPasscode()
            }
            else -> {
                updateGoal()
            }
        }
    }

    private fun goPrevious() {
        currentFragmentPosition -= 1
        if (currentFragmentPosition < 1) currentFragmentPosition = 1
        changeFragment()
    }

    private fun goNextStep() {
        currentFragmentPosition += 1
        changeFragment()
    }

    private fun goEnd() {
        startActivity(Intent(this, OnboardingEndActivity::class.java))
        finish()
    }

    private fun changeFragment() {
        ui_onboarding_bt_next?.disable()
        val fragment = when (currentFragmentPosition) {
            1 -> OnboardingPhase1Fragment.newInstance(
                temporaryUser.firstName,
                temporaryUser.lastName,
                temporaryCountrycode,
                temporaryPhone,
                temporaryEmail,
                hasConsent
            )
            2 -> OnboardingPhase2Fragment.newInstance(
                temporaryPhone,
                temporaryCountrycode
            )
            3 -> OnboardingPhase3Fragment.newInstance( isEntour,isBeEntour,isAsso, temporaryPlaceAddress)
            else -> Fragment()
        }

        if ( currentFragmentPosition >= PositionsType.Type.pos) {
            icon_back?.visibility = View.GONE
        } else {
            icon_back?.visibility = View.VISIBLE
        }
        try {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.ui_main_container, fragment)
                .commit()
        } catch (e: IllegalStateException) {
            Timber.e(e)
        }

        updateButtons()
    }

    /********
     * Views Update
     */

    private fun setupViews() {
        ui_onboarding_bt_next?.setOnClickListener {
            goNext()
        }

        ui_onboarding_bt_previous?.setOnClickListener {
            goPrevious()
        }

        icon_back?.setOnClickListener {
            startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
            finish()
        }

        ui_onboarding_bt_previous?.visibility = View.INVISIBLE
        ui_onboarding_bt_next?.disable()
    }

    fun updateButtons() {
        when (currentFragmentPosition) {
            PositionsType.NamesPhone.pos -> {
                ui_onboarding_bt_previous?.visibility = View.INVISIBLE
                ui_header_title.text = getString(R.string.onboard_welcome_title)
            }
            PositionsType.Type.pos -> {
                ui_onboarding_bt_previous?.visibility = View.INVISIBLE
                val usern = String.format(getString(R.string.onboard_welcome_title_phase3),temporaryUser.firstName)
                ui_header_title.text = usern
            }
            else -> {
                ui_onboarding_bt_previous?.visibility = View.VISIBLE
                ui_header_title.text = getString(R.string.onboard_welcome_title_phase2)
            }
        }
    }

    /********
     * Implement Callback
     */
    override fun validateNames(
        firstname: String?,
        lastname: String?,
        country: Country?,
        phoneNumber: String?,
        email: String?,
        hasConsent: Boolean
    ) {
        temporaryUser.firstName = firstname
        temporaryUser.lastName = lastname
        temporaryCountrycode = country
        temporaryPhone = phoneNumber
        temporaryEmail = email
        this.hasConsent = hasConsent

        temporaryUser.phone = null
        if (phoneNumber != null) {
            val phoneWithCode = Utils.checkPhoneNumberFormat(country?.phoneCode, phoneNumber)
            if (phoneWithCode != null) {
                temporaryUser.phone = phoneWithCode
            }
        }
    }

    override fun validatePasscode(password: String?) {
        temporaryPasscode = password
    }

    override fun updateUsertypeAndAddress(isEntour:Boolean, isBeEntour:Boolean, isAsso:Boolean,address: User.Address?) {
        this.isEntour = isEntour
        this.isBeEntour = isBeEntour
        this.isAsso = isAsso
        this.temporaryPlaceAddress = address

        if ((isEntour || isBeEntour || isAsso) && address != null) {
            updateButtonNext(true)
        }
        else {
            updateButtonNext(false)
        }
    }

    override fun updateButtonNext(isValid: Boolean) {
        if (isValid) {
            ui_onboarding_bt_next.enable()
        }
        else {
            ui_onboarding_bt_next.disable()
        }
    }

    override fun goPreviousManually() {
        goPrevious()
    }

    override fun requestNewCode() {
        resendCode()
    }
}
