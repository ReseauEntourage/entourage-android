package social.entourage.android.entourage.join.received

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.TourRequest
import social.entourage.android.api.model.EntourageUser.EntourageUserWrapper
import social.entourage.android.api.model.feed.FeedItem
import java.util.*
import javax.inject.Inject

class EntourageJoinRequestReceivedPresenter @Inject constructor(
        private val activity: EntourageJoinRequestReceivedActivity?,
        private val tourRequest: TourRequest,
        private val entourageRequest: EntourageRequest) {

    // ----------------------------------
    // API CALLS
    // ----------------------------------
    internal fun acceptTourJoinRequest(tourUUID: String, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        val call = tourRequest.updateUserTourStatus(tourUUID, userId, user)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (activity != null) {
                    if (response.isSuccessful) {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, true)
                    } else {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, false)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, false)
            }
        })
    }

    internal fun rejectTourJoinRequest(tourUUID: String, userId: Int) {
        val call = tourRequest.removeUserFromTour(tourUUID, userId)
        call.enqueue(object : Callback<EntourageUserWrapper> {
            override fun onResponse(call: Call<EntourageUserWrapper>, response: Response<EntourageUserWrapper>) {
                if (activity != null) {
                    if (response.isSuccessful) {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, true)
                    } else {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, false)
                    }
                }
            }

            override fun onFailure(call: Call<EntourageUserWrapper>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, false)
            }
        })
    }

    internal fun acceptEntourageJoinRequest(entourageUUID: String?, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        val call = entourageRequest.updateUserEntourageStatus(entourageUUID, userId, user)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (activity != null) {
                    if (response.isSuccessful) {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, true)
                    } else {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, false)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, false)
            }
        })
    }

    internal fun rejectEntourageJoinRequest(entourageUUID: String?, userId: Int) {
        val call = entourageRequest.removeUserFromEntourage(entourageUUID, userId)
        call.enqueue(object : Callback<EntourageUserWrapper> {
            override fun onResponse(call: Call<EntourageUserWrapper>, response: Response<EntourageUserWrapper>) {
                if (activity != null) {
                    if (response.isSuccessful) {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, true)
                    } else {
                        activity.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, false)
                    }
                }
            }

            override fun onFailure(call: Call<EntourageUserWrapper>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, false)
            }
        })
    }

}