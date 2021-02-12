package social.entourage.android.user.edit

import android.os.Build
import androidx.collection.ArrayMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserRequest
import social.entourage.android.api.request.UserResponse
import social.entourage.android.api.model.User
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.UserFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragmentCompat
import javax.inject.Inject

/**
 * Created by mihaiionescu on 01/11/16.
 */
class UserEditPresenter @Inject constructor(
        private val fragment: UserEditFragment?,
        private val userRequest: UserRequest,
        private val authenticationController: AuthenticationController) {

    var editedUser: User? = null
        get() = field ?: authenticationController.me?.clone().also { field = it}
        private set

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
                    editedUser?.smsCode = newPassword
                    fragment?.onSaveNewPassword()
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

    fun storeActionZone(ignoreActionZone: Boolean) {
        authenticationController.isIgnoringActionZone = ignoreActionZone

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            (fragment?.parentFragmentManager?.findFragmentByTag(UserEditActionZoneFragmentCompat.TAG) as UserEditActionZoneFragmentCompat?)?.let { userEditActionZoneFragmentCompat ->
                if (!userEditActionZoneFragmentCompat.isStateSaved) {
                    userEditActionZoneFragmentCompat.dismiss()
                }
            }
        } else {
            (fragment?.parentFragmentManager?.findFragmentByTag(UserEditActionZoneFragment.TAG) as UserEditActionZoneFragment?)?.let { userEditActionZoneFragment ->
                if (!userEditActionZoneFragment.isStateSaved) {
                    userEditActionZoneFragment.dismiss()
                }
            }
        }
    }

    fun deleteSecondaryAddress() {
        userRequest.deleteSecondaryAddressLocation()
                .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    authenticationController.me?.let { me->
                        me.addressSecondary = null
                        authenticationController.saveUser(me)
                        fragment?.initUserData()
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    fun updateUser() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_SAVE)
        editedUser?.let { user->
            // If we have an user fragment in the stack, let it handle the update
            (fragment?.parentFragmentManager?.findFragmentByTag(UserFragment.TAG) as UserFragment?)?.saveAccount(user)
                    ?: run {
                        updateUser(user)
                    }
        }
    }

    fun initEditedUser() {
        authenticationController.me?.let { me ->
            editedUser?.let { user ->
                user.avatarURL = me.avatarURL
                user.partner = me.partner
                user.address = me.address
                user.addressSecondary = me.addressSecondary
                user.interests.clear()
                user.interests.addAll(me.interests)
                user.goal = me.goal
                fragment?.initUserData()
            }
        }
    }
}