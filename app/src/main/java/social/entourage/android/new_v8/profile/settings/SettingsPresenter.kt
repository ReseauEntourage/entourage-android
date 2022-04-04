package social.entourage.android.new_v8.profile.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserResponse
import java.util.HashSet

class SettingsPresenter {

    var accountDeleted: MutableLiveData<Boolean> = MutableLiveData()

    fun logOut(context: Context) {
        val controller = EntourageApplication.get().components.authenticationController
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        val editor = sharedPreferences.edit()
        controller.me?.let { me ->
            (sharedPreferences.getStringSet(
                EntourageApplication.KEY_TUTORIAL_DONE,
                HashSet()
            ) as HashSet<String?>?)?.let { loggedNumbers ->
                loggedNumbers.remove(me.phone)
                editor.putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers)
            }
        }
        editor.remove(EntourageApplication.KEY_REGISTRATION_ID)
        editor.remove(EntourageApplication.KEY_NOTIFICATIONS_ENABLED)
        editor.remove(EntourageApplication.KEY_GEOLOCATION_ENABLED)
        editor.remove(EntourageApplication.KEY_NO_MORE_DEMAND)
        editor.putInt(EntourageApplication.KEY_NB_OF_LAUNCH, 0)
        editor.apply()
        controller.logOutUser()
        EntourageApplication[context].removeAllPushNotifications()
    }

    fun deleteAccount() {
        EntourageApplication.get().components.userRequest.deleteUser()
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    accountDeleted.value = true
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    accountDeleted.value = false
                }
            })
    }
}