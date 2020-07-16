package social.entourage.android.entourage.invite

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.model.MultipleInvitations
import social.entourage.android.api.model.MultipleInvitations.MultipleInvitationsResponse
import social.entourage.android.api.model.MultipleInvitations.MultipleInvitationsWrapper
import social.entourage.android.api.model.TimestampedObject
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
        } else if (feedItemType == TimestampedObject.TOUR_CARD) {
            // TODO Tour InviteBySMS
            fragment?.onInviteSent(false)
        }
    }

    private fun inviteBySMSEntourage(entourageUUID: String, invitations: MultipleInvitations) {
        entourageRequest.inviteBySMS(entourageUUID, MultipleInvitationsWrapper(invitations))
                .enqueue(object : Callback<MultipleInvitationsResponse> {
            override fun onResponse(call: Call<MultipleInvitationsResponse>, response: Response<MultipleInvitationsResponse>) {
                if (response.isSuccessful) {
                    fragment?.onInviteSent(true)
                } else {
                    fragment?.onInviteSent(false)
                }
            }

            override fun onFailure(call: Call<MultipleInvitationsResponse>, t: Throwable) {
                fragment?.onInviteSent(false)
            }
        })
    }

}