package social.entourage.android.authentication.login

import androidx.collection.ArrayMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.*
import social.entourage.android.api.model.User
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.tools.Utils.checkPhoneNumberFormat
import social.entourage.android.user.AvatarUpdatePresenter
import java.io.IOException
import java.util.*

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
abstract class BaseLoginPresenter (
        protected val activity: LoginActivity?,
        private val loginRequest: LoginRequest,
        private val userRequest: UserRequest,
        val authenticationController: AuthenticationController?) : AvatarUpdatePresenter {
    private var isTutorialDone = false

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun login(countryCode: String?, phone: String, smsCode: String) {
        checkPhoneNumberFormat(countryCode, phone)?.let { phoneNumber ->
            val user = HashMap<String, String>()
            user["phone"] = phoneNumber
            user["sms_code"] = smsCode
            val sharedPreferences = EntourageApplication.get().sharedPreferences
            val loggedNumbers = sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>? ?: return
            isTutorialDone = loggedNumbers.contains(phoneNumber)
            activity?.startLoader()
            val call = loginRequest.login(user)
            call.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    activity?.let { activity ->
                        if (response.isSuccessful) {
                            response.body()?.user?.let { body ->
                                activity.stopLoader()
                                authenticationController?.apply {
                                    this.saveUser(body)
                                    this.saveUserPhoneAndCode(phoneNumber, smsCode)
                                    this.saveUserToursOnly(false)
                                }
                                if (isTutorialDone) {
                                    activity.startMapActivity()
                                } else {
                                    activity.launchFillInProfileView(phoneNumber, body)
                                }
                            }
                        } else {
                            response.errorBody()?.string()?.let {errorBody ->
                                try {
                                    when {
                                        errorBody.contains("INVALID_PHONE_FORMAT") -> {
                                            activity.loginFail(LoginActivity.LOGIN_ERROR_INVALID_PHONE_FORMAT)
                                        }
                                        errorBody.contains("UNAUTHORIZED") -> {
                                            activity.loginFail(LoginActivity.LOGIN_ERROR_UNAUTHORIZED)
                                        }
                                        else -> {
                                            activity.loginFail(LoginActivity.LOGIN_ERROR_UNKNOWN)
                                        }
                                    }
                                } catch (e: IOException) {
                                    activity.loginFail(LoginActivity.LOGIN_ERROR_UNKNOWN)
                                }
                            } ?: run {
                                activity.loginFail(LoginActivity.LOGIN_ERROR_UNKNOWN)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    activity?.loginFail(LoginActivity.LOGIN_ERROR_NETWORK)
                }
            })
        } ?: run {
            activity?.stopLoader()
            activity?.loginFail(LoginActivity.LOGIN_ERROR_INVALID_PHONE_FORMAT)
        }
    }

    @JvmOverloads
    fun sendNewCode(phone: String?, isOnboarding: Boolean = false) {
        if (phone != null) {
            val user: MutableMap<String, String> = ArrayMap()
            user["phone"] = phone
            val code: MutableMap<String, String> = ArrayMap()
            code["action"] = "regenerate"
            val request = ArrayMap<String, Any>()
            request["user"] = user
            request["code"] = code
            val call = userRequest.regenerateSecretCode(request)
            call.enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.user?.let { activity?.newCodeAsked(it, isOnboarding)}
                    } else {
                        val error = ApiError.fromResponse(response)
                        if (error.code == "USER_NOT_FOUND") {
                            registerUserPhone(phone)
                        } else {
                            activity?.newCodeAsked(null, isOnboarding)
                        }
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    activity?.newCodeAsked(null, isOnboarding)
                }
            })
        }
    }

    fun updateUserEmail(email: String?) {
        authenticationController?.user?.email = email
    }

    fun updateUserName(firstname: String?, lastname: String?) {
        authenticationController?.user?.apply {
            this.firstName = firstname
            this.lastName = lastname
        }
    }

    fun updateUserToServer() {
        val user = authenticationController?.user ?: return
        activity?.startLoader()
        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = userRequest.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                activity?.let { activity ->
                    activity.stopLoader()
                    if (response.isSuccessful) {
                        response.body()?.user?.let {
                            authenticationController.saveUser(it)
                            activity.showPhotoChooseSource()
                            activity.displayToast(R.string.login_text_profile_update_success)
                            return
                        }
                    }
                    activity.displayToast(R.string.login_text_profile_update_fail)
                    EntourageEvents.logEvent(if (user.email == null) EntourageEvents.EVENT_NAME_SUBMIT_ERROR else EntourageEvents.EVENT_EMAIL_SUBMIT_ERROR)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                activity?.let { activity ->
                    activity.stopLoader()
                    activity.displayToast(R.string.login_text_profile_update_fail)
                }
                EntourageEvents.logEvent(if (user.email == null) EntourageEvents.EVENT_NAME_SUBMIT_ERROR else EntourageEvents.EVENT_EMAIL_SUBMIT_ERROR)
            }
        })
    }

    override fun updateUserPhoto(amazonFile: String) {
        val user = ArrayMap<String, Any>()
        user["avatar_key"] = amazonFile
        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = userRequest.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.user?.let {
                        if (authenticationController?.isAuthenticated == true) {
                            authenticationController.saveUser(it)
                        }
                        activity?.onUserPhotoUpdated(true)
                        return
                    }
                }
                activity?.onUserPhotoUpdated(false)
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                activity?.onUserPhotoUpdated(false)
            }
        })
    }

    fun registerUserPhone(phoneNumber: String) {
        val user: MutableMap<String, String> = ArrayMap()
        user["phone"] = phoneNumber
        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = userRequest.registerUser(request)
        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                activity?.let { activity ->
                    if (response.isSuccessful) {
                        activity.registerPhoneNumberSent(phoneNumber, true)

                        // send the facebook event
                        registerUserWithFacebook()
                    } else {
                        response.errorBody()?.string()?.let { errorString ->
                            try {
                                when {
                                    errorString.contains("PHONE_ALREADY_EXIST") -> {
                                        // Phone number already registered
                                        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_2_E)
                                        activity.registerPhoneNumberSent(phoneNumber, false)
                                        activity.displayToast(R.string.registration_number_error_already_registered)
                                    }
                                    errorString.contains("INVALID_PHONE_FORMAT") -> {
                                        activity.displayToast(R.string.login_text_invalid_format)
                                    }
                                    else -> {
                                        activity.displayToast(R.string.login_error)
                                    }
                                }
                            } catch (e: IOException) {
                                activity.displayToast(R.string.login_error)
                            }
                        } ?: run {
                            activity.displayToast(R.string.login_error)
                        }
                        activity.registerPhoneNumberSent(phoneNumber, false)
                        EntourageEvents.logEvent(EntourageEvents.EVENT_PHONE_SUBMIT_FAIL)
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                activity?.let {
                    it.displayToast(R.string.login_error_network)
                    it.registerPhoneNumberSent(phoneNumber, false)
                    EntourageEvents.logEvent(EntourageEvents.EVENT_PHONE_SUBMIT_ERROR)
                }
            }
        })
    }
    // ----------------------------------
    // OVERRIDABLE METHODS
    // ----------------------------------
    /**
     * Method that shows if we need to show Terms and conditions screen when user presses login button at startup
     * @return true if we need to show the screen, false otherwise
     */
    fun shouldShowTermsAndConditions(): Boolean {
        return false
    }

    /**
     * Returns true if we continue with the registration funnel, when the user presses the 'Commencer' button
     * false if we just dismiss the T&C screen and proceed to the login page
     * @return
     */
    fun shouldContinueWithRegistration(): Boolean {
        return true
    }

    /**
     * Returns true if we need to show the input name view
     * @param user the user to check
     * @return
     */
    fun shouldShowNameView(user: User): Boolean {
        return user.firstName.isNullOrEmpty() || user.lastName.isNullOrEmpty()
    }

    /**
     * Returns true if we need to show the input email view
     * @param user the user to check
     * @return
     */
    fun shouldShowEmailView(user: User): Boolean {
        return user.email.isNullOrEmpty()
    }

    /**
     * Returns true if we need to show the set action zone view
     * @return
     */
    fun shouldShowActionZoneView(): Boolean {
        return (!authenticationController!!.userPreferences.isIgnoringActionZone
                && shouldShowActionZoneView(authenticationController.user))
    }

    /**
     * Returns true if we need to show the set action zone view
     * @param user the user to check
     * @return
     */
    fun shouldShowActionZoneView(user: User): Boolean {
        return user.address == null || user.address.displayAddress.isNullOrEmpty()
    }

    /**
     * Returns true if we need to show the choose photo source view
     * @param user the user to check
     * @return
     */
    fun shouldShowPhotoChooseView(user: User): Boolean {
        return user.avatarURL.isNullOrEmpty()
    }

    /**
     * Post user registration call that should send the log event to Facebook.<br></br>
     * By default it does nothing.
     */
    protected open fun registerUserWithFacebook() {}

}