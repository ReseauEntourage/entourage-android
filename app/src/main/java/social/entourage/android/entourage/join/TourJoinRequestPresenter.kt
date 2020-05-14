package social.entourage.android.entourage.join

import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.me
import social.entourage.android.R
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.TourRequest
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.EntourageUser.EntourageUserWrapper
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourJoinMessage
import social.entourage.android.api.model.tour.TourJoinMessage.TourJoinMessageWrapper
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by mihaiionescu on 07/03/16.
 */
class TourJoinRequestPresenter @Inject constructor(
        private var tourRequest: TourRequest) {
    // ----------------------------------
    // API CALLS
    // ----------------------------------
    fun sendMessage(fragment: TourJoinRequestFragment, message: String, tour: Tour) {
        if (message.isBlank()) {
            fragment.dismiss()
            return
        }
        val me = me(fragment.context) ?: return
        val joinMessageWrapper = TourJoinMessageWrapper()
        joinMessageWrapper.joinMessage = TourJoinMessage(message.trim { it <= ' ' })
        val call = tourRequest.updateJoinTourMessage(tour.uuid, me.id, joinMessageWrapper)
        call.enqueue(object : Callback<EntourageUserWrapper?> {
            override fun onResponse(call: Call<EntourageUserWrapper?>, response: Response<EntourageUserWrapper?>) {
                if (response.isSuccessful) {
                    if (fragment.activity != null) {
                        Toast.makeText(fragment.requireActivity().applicationContext, R.string.tour_join_request_message_sent, Toast.LENGTH_SHORT).show()
                    }
                    fragment.dismiss()
                } else {
                    if (fragment.activity != null) {
                        Toast.makeText(fragment.requireActivity().applicationContext, R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<EntourageUserWrapper?>, t: Throwable) {
                if (fragment.activity != null) {
                    Toast.makeText(fragment.requireActivity().applicationContext, R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}