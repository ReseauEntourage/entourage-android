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
class EntourageJoinRequestPresenter @Inject constructor(
        private var entourageRequest: EntourageRequest) {
    // ----------------------------------
    // API CALLS
    // ----------------------------------
    fun sendMessage(fragment: EntourageJoinRequestFragment, message: String, entourage: BaseEntourage) {
        if (message.isBlank()) {
            fragment.dismiss()
            return
        }
        val me = me(fragment.context) ?: return
        val info = HashMap<String, Any>()
        val messageHashMap = HashMap<String, String>()
        messageHashMap["message"] = message
        info["request"] = messageHashMap
        val call = entourageRequest.updateUserEntourageStatus(entourage.uuid, me.id, info)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                try {
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
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                if (fragment.activity != null) {
                    Toast.makeText(fragment.requireActivity().applicationContext, R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}