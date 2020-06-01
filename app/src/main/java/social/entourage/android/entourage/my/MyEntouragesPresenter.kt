package social.entourage.android.entourage.my

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.InvitationRequest
import social.entourage.android.api.NewsfeedRequest
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.Invitation.InvitationsWrapper
import social.entourage.android.api.model.feed.NewsfeedItem.NewsfeedItemWrapper
import social.entourage.android.entourage.my.filter.MyEntouragesFilter
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by mihaiionescu on 03/08/16.
 */
class MyEntouragesPresenter @Inject constructor(
        private val fragment: MyEntouragesFragment,
        private val newsfeedRequest: NewsfeedRequest,
        private val entourageRequest: EntourageRequest,
        private val invitationRequest: InvitationRequest) {

    // ----------------------------------
    // Methods
    // ----------------------------------
    fun getMyFeeds(page: Int, per: Int) {
        val filter = MyEntouragesFilter.get(fragment.context)
        val call = newsfeedRequest.retrieveMyFeeds(
                page,
                per,
                filter.actionGroupTypes,
                filter.tourTypes,
                filter.status,
                filter.showOwnEntouragesOnly,
                filter.showPartnerEntourages,
                filter.showJoinedEntourages
        )
        call.enqueue(object : Callback<NewsfeedItemWrapper> {
            override fun onResponse(call: Call<NewsfeedItemWrapper>, response: Response<NewsfeedItemWrapper>) {
                response.body()?.let {
                    if (response.isSuccessful) {
                        fragment.onNewsfeedReceived(it.newsfeedItems)
                        return
                    }
                }
                fragment.onNewsfeedReceived(null)
            }

            override fun onFailure(call: Call<NewsfeedItemWrapper>, t: Throwable) {
                fragment.onNewsfeedReceived(null)
            }
        })
    }

    fun getMyPendingInvitations() {
        val call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING)
        call.enqueue(object : Callback<InvitationsWrapper> {
            override fun onResponse(call: Call<InvitationsWrapper>, response: Response<InvitationsWrapper>) {
                response.body()?.invitations?.let {
                    if (response.isSuccessful) {
                        onInvitationsReceived(it)
                        return
                    }
                }
                fragment.onNoInvitationReceived()
            }

            override fun onFailure(call: Call<InvitationsWrapper>, t: Throwable) {
                fragment.onNoInvitationReceived()
            }
        })
    }

    fun onInvitationsReceived(invitationList: List<Invitation>) {
        // check if the fragment is still attached
        if (!fragment.isAdded) {
            return
        }
        fragment.removeOldInvitations(invitationList)
        invitationList.forEach {
            val call = entourageRequest.retrieveEntourageById(it.entourageUUID, 0, 0)
            call.enqueue(object : Callback<BaseEntourage.EntourageWrapper> {
                override fun onResponse(call: Call<BaseEntourage.EntourageWrapper>, response: Response<BaseEntourage.EntourageWrapper>) {
                    if (response.isSuccessful && response.body()?.entourage is BaseEntourage) {
                        it.entourage = response.body()?.entourage
                        fragment.addInvitation(it)
                    }
                }
                override fun onFailure(call: Call<BaseEntourage.EntourageWrapper>, t: Throwable) {
                    Timber.w("Entourage for Invitation not found")
                }
            })
        }
        // reset the semaphore
        fragment.isRefreshingInvitations = false
    }

    fun clear() {
        EntourageApplication.get().clearFeedStorage()
    }

}