package social.entourage.android

import android.content.Intent
import android.location.Location
import android.widget.Toast
import androidx.collection.ArrayMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.model.ApplicationInfo
import social.entourage.android.api.request.ApplicationInfoRequest
import social.entourage.android.api.request.ApplicationWrapper
import social.entourage.android.api.request.UserRequest
import social.entourage.android.api.request.UserResponse
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.configuration.Configuration
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingLanguage
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity
import timber.log.Timber

/**
 * Created by Mihai Ionescu on 27/04/2018.
 */
class MainPresenter(private val activity: MainActivity) {
    private val applicationInfoRequest: ApplicationInfoRequest
        get() = EntourageApplication.get().apiModule.applicationInfoRequest
    private val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController
    private val userRequest: UserRequest
        get() = EntourageApplication.get().apiModule.userRequest

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var checkForUpdate = true

    // ----------------------------------
    // DISPLAY SCREENS METHODS
    // ----------------------------------
    fun displayTutorial(forced: Boolean) {
        if (!forced && !Configuration.showTutorial()) return
        //Configuration.INSTANCE.showTutorial() is always false
        try {
            activity.startActivity(Intent(activity, PreOnboardingLanguage::class.java))
            activity.finish()
        } catch (e: Exception) {
            // This is just to see if we still get the Illegal state exception
            Timber.e(e)
        }
    }

    // ----------------------------------
    // HELPER METHODS
    // ----------------------------------
    private var deviceID: String?
        get() = EntourageApplication.get().sharedPreferences
            .getString(EntourageApplication.KEY_REGISTRATION_ID, null)
        private set(pushNotificationToken) {
            val editor = EntourageApplication.get().sharedPreferences.edit()
            editor.putString(EntourageApplication.KEY_REGISTRATION_ID, pushNotificationToken)
            editor.apply()
        }

    // ----------------------------------
    // API CALLS METHODS
    // ----------------------------------
    fun checkForUpdate(mainActivity: MainActivity) {
        if (checkForUpdate) {
            val call = applicationInfoRequest.checkForUpdate()
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 426) {
                        if (!BuildConfig.DEBUG) {
                            mainActivity.displayAppUpdateDialog()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Timber.w(t, "Error connecting to API")
                }
            })
            checkForUpdate = false
        }
    }

    fun updateUserLocation(location: Location?) {
        val deviceId = deviceID ?: return
        val user = ArrayMap<String, Any>()
        if (location != null) {
            user[KEY_DEVICE_LOCATION] = location
        }
        user[KEY_DEVICE_ID] = deviceId
        user[KEY_DEVICE_TYPE] = ANDROID_DEVICE
        val call = userRequest.updateUser(user)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    if (authenticationController.isAuthenticated) {
                        response.body()?.user?.let { user -> authenticationController.saveUser(user) }
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Timber.e(t)
            }
        })
    }

    fun deleteApplicationInfo(listener:() -> Unit) {
        val previousDeviceID = deviceID
        if (previousDeviceID.isNullOrBlank()) {
            listener()
            return
        }
        val applicationInfo = ApplicationInfo(previousDeviceID)
        val call = applicationInfoRequest.deleteApplicationInfo(ApplicationWrapper(applicationInfo))
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    listener()
                } else {
                    Timber.e("deleting application info error")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Timber.e(t)
            }
        })
        deviceID = null
    }

    fun updateApplicationInfo(pushNotificationToken: String) {
        //delete old one if existing
        if (pushNotificationToken != deviceID) {
            deleteApplicationInfo {}
        }
        //then add new one
        deviceID = pushNotificationToken
        val applicationInfo = ApplicationInfo(pushNotificationToken)
        val applicationWrapper = ApplicationWrapper(applicationInfo)
        applicationWrapper.setNotificationStatus(ApplicationInfo.NOTIF_PERMISSION_AUTHORIZED)
        applicationInfoRequest.updateApplicationInfo(applicationWrapper)
            .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Timber.e("updating application info error")
                }
                else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Timber.e(t)
            }
        })
    }

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_SMS_COE = "sms_code"
        private const val KEY_PHONE = "phone"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_TYPE = "device_type"
        private const val KEY_DEVICE_LOCATION = "device_location"
        private const val ANDROID_DEVICE = "android"
    }

}