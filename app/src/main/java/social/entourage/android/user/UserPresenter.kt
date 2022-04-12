package social.entourage.android.user

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.EntourageConversation
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.request.EntourageResponse
import social.entourage.android.api.request.UserRequest
import social.entourage.android.api.request.UserResponse
import social.entourage.android.api.model.User
import social.entourage.android.api.model.User.UserConversation
import social.entourage.android.authentication.AuthenticationController
import javax.inject.Inject

/**
 * Presenter controlling the UserFragment
 * @see UserFragment
 */
class UserPresenter(private val fragment: UserFragment) {

    private val userRequest: UserRequest
        get() = EntourageApplication.get().components.userRequest
    private val entourageRequest: EntourageRequest
        get() = EntourageApplication.get().components.entourageRequest
    private val authenticationController: AuthenticationController
        get() = EntourageApplication.get().components.authenticationController

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    val authenticatedUser: User?
        get() = authenticationController.me

    fun getUser(userId: Int) {
        userRequest.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.user?.let {fragment.onUserReceived(it)}
                } else {
                    fragment.onUserReceivedError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment.onUserReceivedError()
            }
        })
    }

    fun updateUser(user: User) {
        userRequest.updateUser(user.arrayMapForUpdate).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    //update the logged user
                    response.body()?.user?.let {
                        authenticationController.saveUser(it)
                        authenticationController.saveUserPhoneAndCode(user.phone, user.smsCode)
                        //inform the fragment
                        fragment.onUserUpdated(it)
                    }
                } else {
                    fragment.onUserUpdatedError()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                fragment.onUserUpdatedError()
            }
        })
    }

    fun getConversation(conversation: UserConversation) {
        conversation.uuid?.let { uuid->
            entourageRequest.retrieveEntourageById(uuid, 0, 0)
                .enqueue(object : Callback<EntourageResponse> {
            override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                if (response.isSuccessful) {
                    //show the entourage information
                    (response.body()?.entourage as? EntourageConversation)?.let {
                        fragment.onConversationFound(it)
                    }
                } else {
                    fragment.onConversationNotFound()
                }
            }

            override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                fragment.onConversationNotFound()
            }
        })
    } ?: run {
            fragment.onConversationNotFound()
        }
    }
}