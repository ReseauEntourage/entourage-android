package social.entourage.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import com.google.firebase.iid.FirebaseInstanceId
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.about.EntourageAboutFragment
import social.entourage.android.api.request.ApplicationInfoRequest
import social.entourage.android.api.request.ApplicationWrapper
import social.entourage.android.api.request.UserRequest
import social.entourage.android.api.request.UserResponse
import social.entourage.android.api.model.ApplicationInfo
import social.entourage.android.about.carousel.CarouselFragment
import social.entourage.android.configuration.Configuration
import social.entourage.android.involvement.GetInvolvedFragment
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.user.AvatarUpdatePresenter
import social.entourage.android.user.UserFragment
import social.entourage.android.user.edit.UserEditFragment
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragmentCompat
import social.entourage.android.user.edit.photo.PhotoEditFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Mihai Ionescu on 27/04/2018.
 */
 class MainPresenter @Inject internal constructor(
        private val activity: MainActivity,
        private val applicationInfoRequest: ApplicationInfoRequest,
        private val userRequest: UserRequest) : AvatarUpdatePresenter {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var checkForUpdate = true

    // ----------------------------------
    // MENU HANDLING
    // ----------------------------------
    fun handleMenu(@IdRes menuId: Int) {
        when (menuId) {
            R.id.action_good_waves -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_GOODWAVES)
                activity.showWebViewForLinkId(Constants.GOOD_WAVES_ID)
            }
            R.id.action_user -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_SHOWPROFIL)
                val userFragment = activity.supportFragmentManager.findFragmentByTag(UserFragment.TAG) as UserFragment?
                        ?: UserFragment.newInstance(activity.authenticationController.me!!.id)
                userFragment.show(activity.supportFragmentManager, UserFragment.TAG)
            }
            R.id.action_edit_profile -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_MODPROFIL)
                val fragment = UserEditFragment()
                fragment.show(activity.supportFragmentManager, UserEditFragment.TAG)
            }
            R.id.action_logout -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_LOGOUT)
                activity.logout()
            }
            R.id.action_blog -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_BLOG)
                activity.showWebViewForLinkId(Constants.SCB_LINK_ID)
            }
            R.id.action_charte -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_CHART)
                val charteIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.CHARTE_LINK_ID)))
                try {
                    activity.startActivity(charteIntent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_goal -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_GOAL)
                activity.showWebViewForLinkId(Constants.GOAL_LINK_ID)
            }
            R.id.action_donation -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_DONATION)
                val donationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.DONATE_LINK_ID)))
                try {
                    activity.startActivity(donationIntent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_involvement -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_FOLLOW)
                GetInvolvedFragment.newInstance().show(activity.supportFragmentManager, GetInvolvedFragment.TAG)
            }
            R.id.action_ambassador -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_AMBASSADOR)
                val ambassadorIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.AMBASSADOR_ID)))
                try {
                    activity.startActivity(ambassadorIntent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.mainprofile_app_version -> {
                val clipboardManager = get().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("FirebaseID", FirebaseInstanceId.getInstance().id)
                clipboardManager.setPrimaryClip(clipData)
            }
            R.id.action_about -> {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_ABOUT)
                EntourageAboutFragment().show(activity.supportFragmentManager, EntourageAboutFragment.TAG)
            }
            else -> Toast.makeText(activity, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------
    // DISPLAY SCREENS METHODS
    // ----------------------------------
    private fun displayAppUpdateDialog() {
        val builder = AlertDialog.Builder(activity)
        val dialog = builder.setView(R.layout.layout_dialog_version_update)
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
            val call = applicationInfoRequest.checkForUpdate()
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 426) {
                        if (!BuildConfig.DEBUG) {
                            displayAppUpdateDialog()
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
                    if (activity.authenticationController.isAuthenticated) {
                        val responseBody = response.body()
                        if (responseBody != null) activity.authenticationController.saveUser(responseBody.user)
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
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
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                activity.dismissProgressDialog()
                if (response.isSuccessful) {
                    if (activity.authenticationController.isAuthenticated) {
                        val responseBody = response.body()
                        if (responseBody != null) activity.authenticationController.saveUser(responseBody.user)
                    }
                    val photoEditFragment = activity.supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
                    if (photoEditFragment != null) {
                        if (photoEditFragment.onPhotoSent(true)) {
                            val photoChooseSourceFragment = activity.supportFragmentManager.findFragmentByTag(PhotoChooseSourceFragmentCompat.TAG) as PhotoChooseSourceFragmentCompat?
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

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
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
        val call = applicationInfoRequest.deleteApplicationInfo(ApplicationWrapper(applicationInfo))
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Timber.d("deleting application info with success")
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
            deleteApplicationInfo()
        }
        //then add new one
        deviceID = pushNotificationToken
        val applicationInfo = ApplicationInfo(pushNotificationToken)
        val applicationWrapper = ApplicationWrapper(applicationInfo)
        applicationWrapper.setNotificationStatus(ApplicationInfo.NOTIF_PERMISSION_AUTHORIZED)
        val call = applicationInfoRequest.updateApplicationInfo(applicationWrapper)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Timber.d("updating application info with success")
                } else {
                    Timber.e("updating application info error")
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