package social.entourage.android.user

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.UserRequest
import social.entourage.android.api.UserResponse
import social.entourage.android.api.model.BaseEntourage.EntourageWrapper
import social.entourage.android.api.model.User
import social.entourage.android.api.model.User.UserConversation
import social.entourage.android.authentication.AuthenticationController
import javax.inject.Inject

/**
 * Presenter controlling the UserFragment
 * @see UserFragment
 */
class UserPresenter @Inject constructor(
        private val fragment: UserFragment?,
        private val userRequest: UserRequest,
        private val entourageRequest: EntourageRequest,
        private val authenticationController: AuthenticationController) {

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    val authenticatedUser: User?
        get() = authenticationController.me

    fun getUser(userId: Int) {
        userRequest.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.user?.let {fragment?.onUserReceived(it)}
                } else {
                    fragment?.onUserReceivedError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment?.onUserReceivedError()
            }
        })
    }

    /*val isUserToursOnly: Boolean
        get() = authenticationController.isUserToursOnly

    fun saveUserToursOnly(choice: Boolean) {
        authenticationController.saveUserToursOnly(choice)
    }*/

    fun updateUser(user: User) {
        userRequest.updateUser(user.arrayMapForUpdate).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    //update the logged user
                    response.body()?.user?.let {
                        authenticationController.saveUser(it)
                        authenticationController.saveUserPhoneAndCode(user.phone, user.smsCode)
                        //inform the fragment
                        fragment?.onUserUpdated(it)
                    }
                } else {
                    fragment?.onUserUpdatedError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment?.onUserUpdatedError()
            }
        })
    }

    fun getConversation(conversation: UserConversation) {
        entourageRequest.retrieveEntourageById(conversation.uuid, 0, 0)
                .enqueue(object : Callback<EntourageWrapper> {
            override fun onResponse(call: Call<EntourageWrapper>, response: Response<EntourageWrapper>) {
                if (response.isSuccessful) {
                    //show the entourage information
                    response.body()?.entourage?.let { fragment?.onConversationFound(it) }
                } else {
                    fragment?.onConversationNotFound()
                }
            }

            override fun onFailure(call: Call<EntourageWrapper>, t: Throwable) {
                fragment?.onConversationNotFound()
            }
        })
    }
}