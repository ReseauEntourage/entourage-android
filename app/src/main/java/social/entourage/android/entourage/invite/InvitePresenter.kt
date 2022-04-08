package social.entourage.android.entourage.invite

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.model.MultipleInvitations
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.request.MultipleInvitationsWrapper
import javax.inject.Inject

/**
 * Created by mihaiionescu on 12/07/16.
 */
class InvitePresenter @Inject constructor(
        private val fragment: InviteBaseFragment?,
        private val entourageRequest: EntourageRequest) {

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun inviteBySMS(feedItemUUID: String, feedItemType: Int, invitations: MultipleInvitations) {
        if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            inviteBySMSEntourage(feedItemUUID, invitations)
        }
    }

    private fun inviteBySMSEntourage(entourageUUID: String, invitations: MultipleInvitations) {
        entourageRequest.inviteBySMS(entourageUUID, MultipleInvitationsWrapper(invitations))
                .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fragment?.onInviteSent(true)
                } else {
                    fragment?.onInviteSent(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                fragment?.onInviteSent(false)
            }
        })
    }

}