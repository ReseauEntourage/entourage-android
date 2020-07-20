package social.entourage.android.entourage.join.received

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.request.TourRequest
import social.entourage.android.api.model.EntourageUser.EntourageUserResponse
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
        tourRequest.updateUserTourStatus(tourUUID, userId, user)
                .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, response.isSuccessful)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, false)
            }
        })
    }

    internal fun rejectTourJoinRequest(tourUUID: String, userId: Int) {
        tourRequest.removeUserFromTour(tourUUID, userId)
                .enqueue(object : Callback<EntourageUserResponse> {
            override fun onResponse(call: Call<EntourageUserResponse>, response: Response<EntourageUserResponse>) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, response.isSuccessful)
            }

            override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, false)
            }
        })
    }

    internal fun acceptEntourageJoinRequest(entourageUUID: String, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        entourageRequest.updateUserEntourageStatus(entourageUUID, userId, user)
                .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, response.isSuccessful)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_ACCEPTED, false)
            }
        })
    }

    internal fun rejectEntourageJoinRequest(entourageUUID: String, userId: Int) {
        entourageRequest.removeUserFromEntourage(entourageUUID, userId)
                .enqueue(object : Callback<EntourageUserResponse> {
            override fun onResponse(call: Call<EntourageUserResponse>, response: Response<EntourageUserResponse>) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, response.isSuccessful)
            }

            override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                activity?.onUserTourStatusChanged(FeedItem.JOIN_STATUS_REJECTED, false)
            }
        })
    }
}