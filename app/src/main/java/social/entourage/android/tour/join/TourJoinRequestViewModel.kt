package social.entourage.android.tour.join

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.TourRequest
import social.entourage.android.api.model.EntourageUser.EntourageUserWrapper
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourJoinMessage
import social.entourage.android.api.model.tour.TourJoinMessage.TourJoinMessageWrapper
import social.entourage.android.entourage.join.EntourageJoinRequestViewModel

/**
 * Created by mihaiionescu on 07/03/16.
 */
class TourJoinRequestViewModel : ViewModel() {
    var tourRequest: TourRequest =  EntourageApplication.get().entourageComponent.tourRequest

    var requestResult: MutableLiveData<Int> = MutableLiveData<Int>(EntourageJoinRequestViewModel.REQUEST_NORESULT)

    // ----------------------------------
    // API CALLS
    // ----------------------------------
    fun sendMessage(message: String, tour: Tour) {
        if (message.isBlank()) {
            requestResult.value = REQUEST_OK
            return
        }
        val me = EntourageApplication.get().me() ?: return
        val joinMessageWrapper = TourJoinMessageWrapper()
        joinMessageWrapper.joinMessage = TourJoinMessage(message.trim { it <= ' ' })
        val call = tourRequest.updateJoinTourMessage(tour.uuid, me.id, joinMessageWrapper)
        call.enqueue(object : Callback<EntourageUserWrapper?> {
            override fun onResponse(call: Call<EntourageUserWrapper?>, response: Response<EntourageUserWrapper?>) {
                if (response.isSuccessful) {
                    requestResult.value = REQUEST_OK
                } else {
                    requestResult.value = REQUEST_ERROR
                }
            }

            override fun onFailure(call: Call<EntourageUserWrapper?>, t: Throwable) {
                requestResult.value = REQUEST_ERROR
            }
        })
    }

    companion object {
        const val REQUEST_NORESULT =0
        const val REQUEST_OK =1
        const val REQUEST_ERROR =-1
    }
}