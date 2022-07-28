package social.entourage.android.new_v8.events

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.EventsListWrapper
import social.entourage.android.new_v8.events.list.eventPerPage
import social.entourage.android.new_v8.models.Events

class EventsPresenter {
    var getAllMyEvents = MutableLiveData<MutableList<Events>>()
    var getAllEvents = MutableLiveData<MutableList<Events>>()

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
                        if (allEventsWrapper.allEvents.size < eventPerPage) isLastPage = true
                        getAllEvents.value = allEventsWrapper.allEvents
                    }
                }

                override fun onFailure(call: Call<EventsListWrapper>, t: Throwable) {
                }
            })
    }
}