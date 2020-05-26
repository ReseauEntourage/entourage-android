package social.entourage.android

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.api.AppRequest
import social.entourage.android.api.UserRequest
import social.entourage.android.api.UserResponse
import social.entourage.android.api.model.ApplicationInfo
import social.entourage.android.api.model.ApplicationInfo.ApplicationWrapper
import social.entourage.android.carousel.CarouselFragment
import social.entourage.android.configuration.Configuration
import social.entourage.android.involvement.GetInvolvedFragment
import social.entourage.android.user.AvatarUpdatePresenter
import social.entourage.android.user.UserFragment
import social.entourage.android.user.edit.UserEditFragment
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment
import social.entourage.android.user.edit.photo.PhotoEditFragment
import timber.log.Timber

/**
 * The base class for MainPresenter<br></br>
 * The derived classes will be per app
 * Created by Mihai Ionescu on 27/04/2018.
 */
abstract class MainBasePresenter internal constructor(
        protected val activity: MainActivity,
        private val appRequest: AppRequest,
        private val userRequest: UserRequest) : AvatarUpdatePresenter {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var checkForUpdate = true

    // ----------------------------------
    // MENU HANDLING
    // ----------------------------------
    protected open fun handleMenu(@IdRes menuId: Int) {
        when (menuId) {
            R.id.action_user -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_TAP_MY_PROFILE)
                val userFragment = activity.supportFragmentManager.findFragmentByTag(UserFragment.TAG) as UserFragment?
                        ?: UserFragment.newInstance(activity.getAuthenticationController().user.id)
                userFragment.show(activity.supportFragmentManager, UserFragment.TAG)
            }
            R.id.action_edit_profile -> {
                val fragment = UserEditFragment()
                fragment.show(activity.supportFragmentManager, UserEditFragment.TAG)
            }
            R.id.action_logout -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_LOGOUT)
                activity.logout()
            }
            R.id.action_blog -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_BLOG)
                activity.showWebViewForLinkId(Constants.SCB_LINK_ID)
            }
            R.id.action_charte -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_CHART)
                val charteIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.CHARTE_LINK_ID)))
                try {
                    activity.startActivity(charteIntent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_goal -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_GOAL)
                activity.showWebViewForLinkId(Constants.GOAL_LINK_ID)
            }
            R.id.action_donation -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_DONATION)
                val donationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.DONATE_LINK_ID)))
                try {
                    activity.startActivity(donationIntent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_involvement -> {
                GetInvolvedFragment.newInstance().show(activity.supportFragmentManager, GetInvolvedFragment.TAG)
            }
            else -> Toast.makeText(activity, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------
    // DISPLAY SCREENS METHODS
    // ----------------------------------
    private fun displayAppUpdateDialog() {
        val builder = AlertDialog.Builder(activity)
        val dialog = builder.setView(R.layout.dialog_version_update)
                .setCancelable(false)
                .create()
        dialog.show()
        val updateButton = dialog.findViewById<Button>(R.id.update_dialog_button)
        updateButton?.setOnClickListener {
            try {
                val uri = Uri.parse(activity.getString(R.string.market_url, activity.packageName))
                activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) {
                Toast.makeText(activity, R.string.error_google_play_store_not_installed, Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }
        }
    }

    fun displayTutorial(forced: Boolean) {
        if (!forced && !Configuration.showTutorial()) return
        //Configuration.INSTANCE.showTutorial() is always false
        try {
            CarouselFragment().show(activity.supportFragmentManager, CarouselFragment.TAG)
        } catch (e: Exception) {
            // This is just to see if we still get the Illegal state exception
            Timber.e(e)
        }
    }

    // ----------------------------------
    // HELPER METHODS
    // ----------------------------------
    private var deviceID: String?
        get() = get().sharedPreferences
                .getString(EntourageApplication.KEY_REGISTRATION_ID, null)
        private set(pushNotificationToken) {
            val editor = get().sharedPreferences.edit()
            editor.putString(EntourageApplication.KEY_REGISTRATION_ID, pushNotificationToken)
            editor.apply()
        }

    // ----------------------------------
    // API CALLS METHODS
    // ----------------------------------
    fun checkForUpdate() {
        if (checkForUpdate) {
            val call = appRequest.checkForUpdate()
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    if (response.code() == 426) {
                        if (!BuildConfig.DEBUG) {
                            displayAppUpdateDialog()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Timber.w(t, "Error connecting to API")
                }
            })
            checkForUpdate = false
        }
    }

    fun updateUser(email: String?, smsCode: String?, phone: String?, location: Location?) {
        val deviceId = deviceID ?: return
        val user = ArrayMap<String, Any>()
        if (email != null) {
            user[KEY_EMAIL] = email
        }
        if (smsCode != null) {
            user[KEY_SMS_COE] = smsCode
        }
        if (phone != null) {
            user[KEY_PHONE] = phone
        }
        if (location != null) {
            user[KEY_DEVICE_LOCATION] = location
        }
        user[KEY_DEVICE_ID] = deviceId
        user[KEY_DEVICE_TYPE] = ANDROID_DEVICE
        val call = userRequest.updateUser(user)
        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    if (activity.authenticationController.isAuthenticated) {
                        val responseBody = response.body()
                        if (responseBody != null) activity.authenticationController.saveUser(responseBody.user)
                    }
                    Timber.d("success")
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                Timber.e(t)
            }
        })
    }

    override fun updateUserPhoto(amazonFile: String) {
        val user = ArrayMap<String, Any>()
        user["avatar_key"] = amazonFile
        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = userRequest.updateUser(request)
        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                activity.dismissProgressDialog()
                if (response.isSuccessful) {
                    if (activity.authenticationController.isAuthenticated) {
                        val responseBody = response.body()
                        if (responseBody != null) activity.authenticationController.saveUser(responseBody.user)
                    }
                    val photoEditFragment = activity.supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
                    if (photoEditFragment != null) {
                        if (photoEditFragment.onPhotoSent(true)) {
                            val photoChooseSourceFragment = activity.supportFragmentManager.findFragmentByTag(PhotoChooseSourceFragment.TAG) as PhotoChooseSourceFragment?
                            photoChooseSourceFragment?.dismiss()
                        }
                    }
                } else {
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
                    val photoEditFragment = activity.supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
                    photoEditFragment?.onPhotoSent(false)
                    Timber.e(activity.getString(R.string.user_photo_error_not_saved))
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                activity.dismissProgressDialog()
                Timber.e(t)
                Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
                val photoEditFragment = activity.supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
                photoEditFragment?.onPhotoSent(false)
            }
        })
    }

    fun deleteApplicationInfo() {
        val previousDeviceID = deviceID
        if (previousDeviceID.isNullOrBlank()) {
            return
        }
        val applicationInfo = ApplicationInfo(previousDeviceID)
        val applicationWrapper = ApplicationWrapper()
        applicationWrapper.applicationInfo = applicationInfo
        val call = appRequest.deleteApplicationInfo(applicationWrapper)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    Timber.d("deleting application info with success")
                } else {
                    Timber.e("deleting application info error")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Timber.e(t)
            }
        })
        deviceID = null
    }

    fun updateApplicationInfo(pushNotificationToken: String) {
        //delete old one if existing
        if (pushNotificationToken != deviceID) {
            deleteApplicationInfo()
        }
        //then add new one
        deviceID = pushNotificationToken
        val applicationInfo = ApplicationInfo(pushNotificationToken)
        val applicationWrapper = ApplicationWrapper()
        applicationWrapper.applicationInfo = applicationInfo
        applicationWrapper.setNotificationStatus(ApplicationInfo.NOTIF_PERMISSION_AUTHORIZED)
        val call = appRequest.updateApplicationInfo(applicationWrapper)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    Timber.d("updating application info with success")
                } else {
                    Timber.e("updating application info error")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
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