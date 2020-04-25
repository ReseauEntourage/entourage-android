package social.entourage.android.entourage.my

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.InvitationRequest
import social.entourage.android.api.NewsfeedRequest
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.Invitation.InvitationsWrapper
import social.entourage.android.api.model.Newsfeed.NewsfeedWrapper
import social.entourage.android.entourage.my.filter.MyEntouragesFilter
import javax.inject.Inject

/**
 * Created by mihaiionescu on 03/08/16.
 */
class MyEntouragesPresenter @Inject constructor(
        private val fragment: MyEntouragesFragment,
        private val newsfeedRequest: NewsfeedRequest,
        private val invitationRequest: InvitationRequest) {

    // ----------------------------------
    // Methods
    // ----------------------------------
    fun getMyFeeds(page: Int, per: Int) {
        val filter = MyEntouragesFilter.getMyEntouragesFilter(fragment.context)
        val call = newsfeedRequest.retrieveMyFeeds(
                page,
                per,
                filter.entourageTypes,
                filter.tourTypes,
                filter.status,
                filter.showOwnEntouragesOnly,
                filter.showPartnerEntourages,
                filter.showJoinedEntourages
        )
        call.enqueue(object : Callback<NewsfeedWrapper> {
            override fun onResponse(call: Call<NewsfeedWrapper>, response: Response<NewsfeedWrapper>) {
                if (response.isSuccessful) {
                    fragment.onNewsfeedReceived(response.body()!!.newsfeed)
                } else {
                    fragment.onNewsfeedReceived(null)
                }
            }

            override fun onFailure(call: Call<NewsfeedWrapper>, t: Throwable) {
                fragment.onNewsfeedReceived(null)
            }
        })
    }

    fun getMyPendingInvitations() {
        val call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING)
        call.enqueue(object : Callback<InvitationsWrapper> {
            override fun onResponse(call: Call<InvitationsWrapper>, response: Response<InvitationsWrapper>) {
                if (response.isSuccessful) {
                    fragment.onInvitationsReceived(response.body()!!.invitations)
                } else {
                    fragment.onInvitationsReceived(null)
                }
            }

            override fun onFailure(call: Call<InvitationsWrapper>, t: Throwable) {
                fragment.onInvitationsReceived(null)
            }
        })
    }

    fun clear() {
        EntourageApplication.get().clearFeedStorage()
    }

}