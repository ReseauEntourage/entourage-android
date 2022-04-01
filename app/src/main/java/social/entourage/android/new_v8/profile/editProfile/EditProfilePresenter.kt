package social.entourage.android.new_v8.profile.editProfile

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserResponse
import timber.log.Timber

class EditProfilePresenter {

    var isUserUpdated = MutableLiveData<Boolean>()

    fun updateUser(userEdited: ArrayMap<String, Any>) {
        EntourageApplication.get().components.userRequest.updateUser(userEdited)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.user?.let {
                            EntourageApplication.get().components.authenticationController.saveUser(
                                it
                            )
                            EntourageApplication.get().components.authenticationController.saveUserPhoneAndCode(
                                it.phone,
                                it.smsCode
                            )
                            isUserUpdated.value = true
                        }
                    } else {
                        isUserUpdated.value = false
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    isUserUpdated.value = false
                }
            })
    }
}