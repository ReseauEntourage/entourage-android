package social.entourage.android.new_v8.events

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.request.*
import social.entourage.android.new_v8.RefreshController
import social.entourage.android.new_v8.events.create.CreateEvent
import social.entourage.android.new_v8.events.list.EVENTS_PER_PAGE
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.models.Post
import timber.log.Timber
import java.io.File
import java.io.IOException

class EventsPresenter {
    var getAllMyEvents = MutableLiveData<MutableList<Events>>()
    var getAllEvents = MutableLiveData<MutableList<Events>>()
    var getEvent = MutableLiveData<Events>()
    var isEventReported = MutableLiveData<Boolean>()
    var getAllComments = MutableLiveData<MutableList<Post>>()
    var newEventCreated = MutableLiveData<Events>()
    var isEventCreated = MutableLiveData<Boolean>()
    var isUserParticipating = MutableLiveData<Boolean>()
    var getMembers = MutableLiveData<MutableList<EntourageUser>>()
    var getMembersSearch = MutableLiveData<MutableList<EntourageUser>>()
    var getAllPosts = MutableLiveData<MutableList<Post>>()

    var hasUserLeftEvent = MutableLiveData<Boolean>()
    var eventCanceled = MutableLiveData<Boolean>()

    var isEventUpdated = MutableLiveData<Boolean>()
    var hasPost = MutableLiveData<Boolean>()
    var commentPosted = MutableLiveData<Post?>()

    var isLoading: Boolean = false
    var isLastPage: Boolean = false

    var isSendingCreatePost = false

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

    fun getAllEvents(page: Int, per: Int,distance:Int?,latitude:Double?,longitude:Double?) {
        EntourageApplication.get().apiModule.eventsRequest.getAllEvents(page, per,distance,latitude,longitude)
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
                        response.body()?.let { eventWrapper ->
                            getEvent.value = eventWrapper.event
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
                    RefreshController.shouldRefreshEventFragment =
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
                    RefreshController.shouldRefreshEventFragment =
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

    fun cancelEvent(eventId: Int) {
        EntourageApplication.get().apiModule.eventsRequest.cancelEvent(eventId)
            .enqueue(object : Callback<EventWrapper> {
                override fun onResponse(
                    call: Call<EventWrapper>,
                    response: Response<EventWrapper>
                ) {
                    eventCanceled.value =
                        response.isSuccessful && response.body()?.event != null
                }

                override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                }
            })
    }

    fun updateEvent(eventId: Int, eventEdited: ArrayMap<String, Any>) {
        EntourageApplication.get().apiModule.eventsRequest.updateEvent(eventId, eventEdited)
            .enqueue(object : Callback<EventWrapper> {
                override fun onResponse(
                    call: Call<EventWrapper>,
                    response: Response<EventWrapper>
                ) {
                    isEventUpdated.value = response.isSuccessful && response.body()?.event != null
                }

                override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                    isEventUpdated.value = false
                }
            })
    }

    fun getEventPosts(eventId: Int) {
        EntourageApplication.get().apiModule.eventsRequest.getEventPosts(eventId)
            .enqueue(object : Callback<PostListWrapper> {
                override fun onResponse(
                    call: Call<PostListWrapper>,
                    response: Response<PostListWrapper>
                ) {
                    response.body()?.let { allPostsWrapper ->
                        getAllPosts.value = allPostsWrapper.posts
                    }

                }

                override fun onFailure(call: Call<PostListWrapper>, t: Throwable) {
                }
            })
    }

    fun addPost(message: String?, file: File, eventId: Int) {
        if (isSendingCreatePost) return
        isSendingCreatePost = true
        val request = RequestContent("image/jpeg")
        EntourageApplication.get().apiModule.eventsRequest.prepareAddPost(eventId, request)
            .enqueue(object : Callback<PrepareAddPostResponse> {
                override fun onResponse(
                    call: Call<PrepareAddPostResponse>,
                    response: Response<PrepareAddPostResponse>
                ) {
                    Timber.e(response.body().toString())
                    if (response.isSuccessful) {
                        val presignedUrl = response.body()?.presignedUrl
                        val uploadKey = response.body()?.uploadKey
                        presignedUrl?.let {
                            isSendingCreatePost = true
                            uploadFile(eventId, file, presignedUrl, uploadKey, message)
                        } ?: run { isSendingCreatePost = false }
                    }
                    else {
                        isSendingCreatePost = false
                    }
                }

                override fun onFailure(call: Call<PrepareAddPostResponse>, t: Throwable) {
                    isSendingCreatePost = false
                }
            })
    }

    fun addPost(eventId: Int, params: ArrayMap<String, Any>) {
        if (isSendingCreatePost) return
        isSendingCreatePost = true
        EntourageApplication.get().apiModule.eventsRequest.addPost(eventId, params)
            .enqueue(object : Callback<PostWrapper> {
                override fun onResponse(
                    call: Call<PostWrapper>,
                    response: Response<PostWrapper>
                ) {
                    hasPost.value = response.isSuccessful
                }

                override fun onFailure(call: Call<PostWrapper>, t: Throwable) {
                    hasPost.value = false
                    isSendingCreatePost = false
                }
            })
    }

    fun uploadFile(
        eventId: Int,
        file: File,
        presignedUrl: String,
        uploadKey: String?,
        message: String?
    ) {
        val client: OkHttpClient = EntourageApplication.get().apiModule.okHttpClient
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(presignedUrl)
            .put(requestBody)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e("response ${e.message}")
                isSendingCreatePost = false
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val messageChat = ArrayMap<String, Any>()
                messageChat["image_url"] = uploadKey
                if (!message.isNullOrBlank() && !message.isNullOrEmpty())
                    messageChat["content"] = message
                val chatMessage = ArrayMap<String, Any>()
                chatMessage["chat_message"] = messageChat
                isSendingCreatePost = false
                addPost(eventId, chatMessage)
            }
        })
    }

    fun getPostComments(eventId: Int, postId: Int) {
        EntourageApplication.get().apiModule.eventsRequest.getPostComments(eventId, postId)
            .enqueue(object : Callback<PostListWrapper> {
                override fun onResponse(
                    call: Call<PostListWrapper>,
                    response: Response<PostListWrapper>
                ) {
                    response.body()?.let { allCommentsWrapper ->
                        getAllComments.value = allCommentsWrapper.posts
                    }
                }

                override fun onFailure(call: Call<PostListWrapper>, t: Throwable) {
                }
            })
    }

    fun addComment(groupId: Int, comment: Post?) {
        val messageChat = ArrayMap<String, Any>()
        messageChat["content"] = comment?.content
        messageChat["parent_id"] = comment?.postId.toString()
        val chatMessage = ArrayMap<String, Any>()
        chatMessage["chat_message"] = messageChat
        EntourageApplication.get().apiModule.eventsRequest.addPost(groupId, chatMessage)
            .enqueue(object : Callback<PostWrapper> {
                override fun onResponse(
                    call: Call<PostWrapper>,
                    response: Response<PostWrapper>
                ) {
                    commentPosted.value = response.body()?.post
                }

                override fun onFailure(call: Call<PostWrapper>, t: Throwable) {
                    commentPosted.value = null
                }
            })
    }

    fun updateEvent(eventId: Int, eventEdited: CreateEvent) {
        EntourageApplication.get().apiModule.eventsRequest.updateEvent(eventId, CreateEventWrapper(eventEdited))
            .enqueue(object : Callback<EventWrapper> {
                override fun onResponse(
                    call: Call<EventWrapper>,
                    response: Response<EventWrapper>
                ) {
                    isEventUpdated.value = response.isSuccessful && response.body()?.event != null
                }

                override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                    isEventUpdated.value = false
                }
            })
    }


    fun updateEventSiblings(eventId: Int, eventEdited: CreateEvent) {
        EntourageApplication.get().apiModule.eventsRequest.updateEventSiblings(eventId, CreateEventWrapper(eventEdited))
            .enqueue(object : Callback<EventWrapper> {
                override fun onResponse(
                    call: Call<EventWrapper>,
                    response: Response<EventWrapper>
                ) {
                    isEventUpdated.value = response.isSuccessful && response.body()?.event != null
                }

                override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                    isEventUpdated.value = false
                }
            })
    }
}