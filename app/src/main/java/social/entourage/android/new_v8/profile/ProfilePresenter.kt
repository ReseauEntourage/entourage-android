package social.entourage.android.new_v8.profile

import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserResponse
import social.entourage.android.user.AvatarUpdatePresenter
import timber.log.Timber

class ProfilePresenter : AvatarUpdatePresenter {

    var isPhotoSuccess = MutableLiveData<Boolean>()

    override fun updateUserPhoto(amazonFile: String) {
        Timber.e("updateUserPhoto")

        val user = ArrayMap<String, Any>()
        user["avatar_key"] = amazonFile
        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = EntourageApplication.get().components.userRequest.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                val authenticationController =
                    EntourageApplication.get().components.authenticationController
                // activity.dismissProgressDialog()
                if (response.isSuccessful) {
                    if (authenticationController.isAuthenticated) {
                        response.body()
                            ?.let { responseBody -> authenticationController.saveUser(responseBody.user) }
                    }
                    isPhotoSuccess.value = true
                } else {
                    isPhotoSuccess.value = false
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                isPhotoSuccess.value = false
            }
        })
    }
}