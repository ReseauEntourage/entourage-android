package social.entourage.android.new_v8.events

import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.request.*
import social.entourage.android.new_v8.events.create.CreateEvent
import social.entourage.android.new_v8.events.list.EVENTS_PER_PAGE
import social.entourage.android.new_v8.models.Events
import timber.log.Timber

class EventsPresenter {
    var getAllMyEvents = MutableLiveData<MutableList<Events>>()
    var getAllEvents = MutableLiveData<MutableList<Events>>()
    var getEvent = MutableLiveData<Events>()
    var isEventReported = MutableLiveData<Boolean>()

    var newEventCreated = MutableLiveData<Events>()
    var isEventCreated = MutableLiveData<Boolean>()
    var isUserParticipating = MutableLiveData<Boolean>()
    var getMembers = MutableLiveData<MutableList<EntourageUser>>()
    var getMembersSearch = MutableLiveData<MutableList<EntourageUser>>()

    var hasUserLeftEvent = MutableLiveData<Boolean>()


    var isLoading: Boolean = false
    var isLastPage: Boolean = false

    fun getMyEvents(userId: Int, page: Int, per: Int) {
        EntourageApplication.get().apiModule.eventsRequest.getMyEvents(userId, page, per)
            .enqueue(object : Callback<EventsListWrapper> {
                override fun onResponse(
                    call: Call<EventsListWrapper>,
                    response: Response<EventsListWrapper>
                ) {
                    response.body()?.let { allEventsWrapper ->
                        if (allEventsWrapper.allEvents.size < EVENTS_PER_PAGE) isLastPage = true
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

    fun createEvent(event: CreateEvent) {
        EntourageApplication.get().apiModule.eventsRequest.createEvent(CreateEventWrapper(event))
            .enqueue(object : Callback<EventWrapper> {
                override fun onResponse(
                    call: Call<EventWrapper>,
                    response: Response<EventWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            isEventCreated.value = true
                            newEventCreated.value = it.event
                        } ?: run {
                            isEventCreated.value = false
                        }
                    } else {
                        isEventCreated.value = false
                    }
                }

                override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                    isEventCreated.value = false
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


    fun getEvent(id: Int) {
        EntourageApplication.get().apiModule.eventsRequest.getEvent(id)
            .enqueue(object : Callback<EventWrapper> {
                override fun onResponse(
                    call: Call<EventWrapper>,
                    response: Response<EventWrapper>
                ) {
                    Timber.e(response.body().toString())
                    if (response.isSuccessful) {
                        response.body()?.let { groupWrapper ->
                            getEvent.value = groupWrapper.event
                        }
                    }
                }

                override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                }
            })
    }


    fun participate(eventId: Int) {
        EntourageApplication.get().apiModule.eventsRequest.participate(eventId)
            .enqueue(object : Callback<EntourageUserResponse> {
                override fun onResponse(
                    call: Call<EntourageUserResponse>,
                    response: Response<EntourageUserResponse>
                ) {
                    isUserParticipating.value =
                        response.isSuccessful && response.body()?.user != null
                    RefreshController.shouldRefreshFragment =
                        response.isSuccessful && response.body()?.user != null
                }

                override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                    isUserParticipating.value = false
                }
            })
    }

    fun leaveEvent(eventId: Int) {
        EntourageApplication.get().apiModule.eventsRequest.leaveEvent(eventId)
            .enqueue(object : Callback<EntourageUserResponse> {
                override fun onResponse(
                    call: Call<EntourageUserResponse>,
                    response: Response<EntourageUserResponse>
                ) {
                    hasUserLeftEvent.value =
                        response.isSuccessful && response.body()?.user != null
                    RefreshController.shouldRefreshFragment =
                        response.isSuccessful && response.body()?.user != null
                }

                override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                    hasUserLeftEvent.value = false
                }
            })
    }

    fun getEventMembersSearch(searchTxt: String) {
        val listTmp: MutableList<EntourageUser> = mutableListOf()
        getMembers.value?.forEach {
            if (it.displayName?.lowercase()?.contains(searchTxt.lowercase()) == true) {
                listTmp.add(it)
            }
        }
        getMembersSearch.value = listTmp
    }

    fun getEventMembers(eventId: Int) {
        EntourageApplication.get().apiModule.eventsRequest.getMembers(eventId)
            .enqueue(object : Callback<MembersWrapper> {
                override fun onResponse(
                    call: Call<MembersWrapper>,
                    response: Response<MembersWrapper>
                ) {
                    response.body()?.let { allMembersWrapper ->
                        getMembers.value = allMembersWrapper.users
                    }

                }

                override fun onFailure(call: Call<MembersWrapper>, t: Throwable) {
                }
            })
    }

}