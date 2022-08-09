package social.entourage.android.new_v8.profile.editProfile

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserResponse

class EditProfilePresenter {

    var isUserUpdated = MutableLiveData<Boolean>()

    fun updateUser(userEdited: ArrayMap<String, Any>) {
        EntourageApplication.get().apiModule.userRequest.updateUser(userEdited)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.user?.let {
                            EntourageApplication.get().authenticationController.saveUser(
                                it
                            )
                            EntourageApplication.get().authenticationController.saveUserPhoneAndCode(
                                it.phone,
                                it.smsCode
                            )
                            isUserUpdated.value = true
                        } ?: run {
                            isUserUpdated.value = false
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

    fun storeActionZone(ignoreActionZone: Boolean) {
        EntourageApplication.get().authenticationController.isIgnoringActionZone =
            ignoreActionZone
    }
}