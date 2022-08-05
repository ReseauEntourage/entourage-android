package social.entourage.android.new_v8.events

import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.UserReport
import social.entourage.android.api.model.UserReportWrapper
import social.entourage.android.api.request.EventsListWrapper
import social.entourage.android.api.request.Report
import social.entourage.android.api.request.ReportWrapper
import social.entourage.android.new_v8.events.list.EVENTS_PER_PAGE
import social.entourage.android.new_v8.models.Events

class EventsPresenter {
    var getAllMyEvents = MutableLiveData<MutableList<Events>>()
    var getAllEvents = MutableLiveData<MutableList<Events>>()
    var isEventReported = MutableLiveData<Boolean>()

    var isLoading: Boolean = false
    var isLastPage: Boolean = false

    fun getMyEvents(userId: Int) {
        EntourageApplication.get().apiModule.eventsRequest.getMyEvents(userId)
            .enqueue(object : Callback<EventsListWrapper> {
                override fun onResponse(
                    call: Call<EventsListWrapper>,
                    response: Response<EventsListWrapper>
                ) {
                    response.body()?.let { allEventsWrapper ->
                        getAllMyEvents.value = allEventsWrapper.allEvents
                    }
                }

                override fun onFailure(call: Call<EventsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getAllEvents(page: Int, per: Int) {
        EntourageApplication.get().apiModule.eventsRequest.getAllEvents(page, per)
            .enqueue(object : Callback<EventsListWrapper> {
                override fun onResponse(
                    call: Call<EventsListWrapper>,
                    response: Response<EventsListWrapper>
                ) {
                    response.body()?.let { allEventsWrapper ->
                        if (allEventsWrapper.allEvents.size < EVENTS_PER_PAGE) isLastPage = true
                        getAllEvents.value = allEventsWrapper.allEvents
                    }
                }

                override fun onFailure(call: Call<EventsListWrapper>, t: Throwable) {
                }
            })
    }


    fun sendReport(
        id: Int,
        reason: String,
        selectedSignalsIdList: MutableList<String>
    ) {
        val userRequest = EntourageApplication.get().apiModule.eventsRequest
        val call = userRequest.reportEvent(
            id, ReportWrapper(Report(reason, selectedSignalsIdList))
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isEventReported.value = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                isEventReported.value = response.isSuccessful
            }
        })
    }

}