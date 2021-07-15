package social.entourage.android.entourage.my

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.request.*
import social.entourage.android.api.tape.Events
import social.entourage.android.entourage.my.filter.MyEntouragesFilter
import social.entourage.android.tools.EntBus
import timber.log.Timber
import java.net.UnknownHostException
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
    fun getMyFeeds(page: Int, per: Int,isUnreadOnly:Boolean) {
        val filter = MyEntouragesFilter.get(fragment.context)
        val call = newsfeedRequest.retrieveMyFeeds(
                page,
                per,
                filter.actionGroupTypes,
                filter.tourTypes,
                filter.status,
                filter.showOwnEntouragesOnly,
                filter.showPartnerEntourages,
                filter.showJoinedEntourages,
                isUnreadOnly
        )
        call.enqueue(object : Callback<NewsfeedItemResponse> {
            override fun onResponse(call: Call<NewsfeedItemResponse>, response: Response<NewsfeedItemResponse>) {
                response.body()?.let {
                    if (response.isSuccessful) {
                        if (page == 1) {
                            it.unreadCount?.let {count -> EntBus.post(Events.OnUnreadCountUpdate(count)) }
                        }
                        fragment.onNewsfeedReceived(it.newsfeedItems)
                        return
                    }
                }
                fragment.onNewsfeedReceived(null)
            }

            override fun onFailure(call: Call<NewsfeedItemResponse>, t: Throwable) {
                fragment.onNewsfeedReceived(null)
                if (t is UnknownHostException) fragment.showErrorMessage()
            }
        })
    }

    fun getMyPendingInvitations() {
        val call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING)
        call.enqueue(object : Callback<InvitationListResponse> {
            override fun onResponse(call: Call<InvitationListResponse>, response: Response<InvitationListResponse>) {
                response.body()?.invitations?.let {
                    if (response.isSuccessful) {
                        onInvitationsReceived(it)
                        return
                    }
                }
                fragment.onNoInvitationReceived()
            }

            override fun onFailure(call: Call<InvitationListResponse>, t: Throwable) {
                fragment.onNoInvitationReceived()
                if (t is UnknownHostException) fragment.showErrorMessage()
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
            call.enqueue(object : Callback<EntourageResponse> {
                override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                    if (response.isSuccessful && response.body()?.entourage is BaseEntourage) {
                        it.entourage = response.body()?.entourage
                        fragment.addInvitation(it)
                    }
                }
                override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                    Timber.w(t, "Entourage for Invitation not found")
                    if (t is UnknownHostException) fragment.showErrorMessage()
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