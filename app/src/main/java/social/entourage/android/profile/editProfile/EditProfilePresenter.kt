package social.entourage.android.profile.editProfile

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserRequest
import social.entourage.android.api.request.UserResponse
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.profile.activities_settings.EditPasswordActivity

class EditProfilePresenter {

    var isUserUpdated = MutableLiveData<Boolean>()

    private val userRequest: UserRequest
        get() = EntourageApplication.get().apiModule.userRequest
    private val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController

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
    fun saveNewPassword(fragment: EditPasswordFragment, newPassword: String) {
        val userMap = ArrayMap<String, Any>()
        userMap["sms_code"] = newPassword
        userRequest.updateUser(userMap).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    //inform the fragment
                    authenticationController.me?.phone?.let { phone ->
                        authenticationController.saveUserPhoneAndCode(phone, newPassword)
                    }
                    authenticationController.me?.smsCode = newPassword
                    fragment.onSaveNewPassword()
                } else {
                    fragment.onSavePasswordError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment.onSavePasswordError()
            }
        })
    }
    fun saveNewPasswordActivity(activity: EditPasswordActivity, newPassword: String) {
        val userMap = ArrayMap<String, Any>()
        userMap["sms_code"] = newPassword
        userRequest.updateUser(userMap).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    //inform the fragment
                    authenticationController.me?.phone?.let { phone ->
                        authenticationController.saveUserPhoneAndCode(phone, newPassword)
                    }
                    authenticationController.me?.smsCode = newPassword
                    activity.onSaveNewPassword()
                } else {
                    activity.onSavePasswordError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                activity.onSavePasswordError()
            }
        })
    }
}