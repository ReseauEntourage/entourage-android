package social.entourage.android.user.edit

import androidx.collection.ArrayMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.request.UserRequest
import social.entourage.android.api.request.UserResponse
import social.entourage.android.api.model.User
import social.entourage.android.authentication.AuthenticationController
import javax.inject.Inject

/**
 * Created by mihaiionescu on 01/11/16.
 */
class UserEditPresenter @Inject constructor(
        private val fragment: UserEditFragment?,
        private val userRequest: UserRequest,
        private val authenticationController: AuthenticationController) {

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun updateUser(user: User) {
        userRequest.updateUser(user.arrayMapForUpdate).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.user?.let {
                        //update the logged user
                        authenticationController.saveUser(it)
                        authenticationController.saveUserPhoneAndCode(user.phone, user.smsCode)
                        //inform the fragment
                        fragment?.onUserUpdated(it)
                    }
                } else {
                    fragment?.onUserUpdateError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment?.onUserUpdateError()
            }
        })
    }

    fun saveNewPassword(newPassword: String) {
        val userMap = ArrayMap<String, Any>()
        userMap["sms_code"] = newPassword
        userRequest.updateUser(userMap).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    //inform the fragment
                    authenticationController.me?.phone?.let { phone ->
                        authenticationController.saveUserPhoneAndCode(phone, newPassword)
                    }
                    fragment?.onSaveNewPassword(newPassword)
                } else {
                    fragment?.onSavePasswordError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment?.onSavePasswordError()
            }
        })
    }

    fun deleteAccount() {
        userRequest.deleteUser().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                fragment?.onDeletedAccount(response.isSuccessful)
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment?.onDeletedAccount(false)
            }
        })
    }
}