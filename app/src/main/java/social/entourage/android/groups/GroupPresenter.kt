package social.entourage.android.groups

import android.util.Log
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
import social.entourage.android.api.request.*
import social.entourage.android.RefreshController
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.groups.list.groupPerPage
import social.entourage.android.home.UnreadMessages
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Post
import timber.log.Timber
import java.io.File
import java.io.IOException

class GroupPresenter {

    var isGroupCreated = MutableLiveData<Boolean>()
    var getGroup = MutableLiveData<Group>()
    var getAllGroups = MutableLiveData<MutableList<Group>>()
    var getGroupsSearch = MutableLiveData<MutableList<Group>>()
    var getAllMyGroups = MutableLiveData<MutableList<Group>>()
    var getAllComments = MutableLiveData<MutableList<Post>>()
    var getMembers = MutableLiveData<MutableList<EntourageUser>>()
    var getMembersSearch = MutableLiveData<MutableList<EntourageUser>>()
    var isGroupUpdated = MutableLiveData<Boolean>()
    var newGroupCreated = MutableLiveData<Group>()
    var hasUserJoinedGroup = MutableLiveData<Boolean>()
    var hasUserLeftGroup = MutableLiveData<Boolean>()
    var getAllPosts = MutableLiveData<MutableList<Post>>()
    var hasPost = MutableLiveData<Boolean>()
    var commentPosted = MutableLiveData<Post?>()
    var isGroupReported = MutableLiveData<Boolean>()
    var isPostReported = MutableLiveData<Boolean>()
    var getAllEvents = MutableLiveData<MutableList<Events>>()
    var getCurrentParentPost = MutableLiveData<Post>()

    var isLoading: Boolean = false
    var isLastPage: Boolean = false

    var isSendingCreatePost = false

    var unreadMessages = MutableLiveData<UnreadMessages?>()

    fun createGroup(group: Group) {
        EntourageApplication.get().apiModule.groupRequest.createGroup(GroupWrapper(group))
            .enqueue(object : Callback<GroupWrapper> {
                override fun onResponse(
                    call: Call<GroupWrapper>,
                    response: Response<GroupWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            isGroupCreated.value = true
                            newGroupCreated.value = it.group
                        } ?: run {
                            isGroupCreated.value = false
                        }
                    } else {
                        isGroupCreated.value = false
                    }
                }

                override fun onFailure(call: Call<GroupWrapper>, t: Throwable) {
                    isGroupCreated.value = false
                }
            })
    }

    fun getGroup(id: Int) {
        EntourageApplication.get().apiModule.groupRequest.getGroup(id)
            .enqueue(object : Callback<GroupWrapper> {
                override fun onResponse(
                    call: Call<GroupWrapper>,
                    response: Response<GroupWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { groupWrapper ->
                            getGroup.value = groupWrapper.group
                        }
                    }
                }

                override fun onFailure(call: Call<GroupWrapper>, t: Throwable) {
                }
            })
    }

    fun updateGroup(id: Int, userEdited: ArrayMap<String, Any>) {
        EntourageApplication.get().apiModule.groupRequest.updateGroup(id, userEdited)
            .enqueue(object : Callback<GroupWrapper> {
                override fun onResponse(
                    call: Call<GroupWrapper>,
                    response: Response<GroupWrapper>
                ) {
                    isGroupUpdated.value = response.isSuccessful && response.body()?.group != null
                }

                override fun onFailure(call: Call<GroupWrapper>, t: Throwable) {
                    isGroupUpdated.value = false
                }
            })
    }

    fun getAllGroups(page: Int, per: Int) {
        EntourageApplication.get().apiModule.groupRequest.getAllGroups(page, per)
            .enqueue(object : Callback<GroupsListWrapper> {
                override fun onResponse(
                    call: Call<GroupsListWrapper>,
                    response: Response<GroupsListWrapper>
                ) {
                    response.body()?.let { allGroupsWrapper ->
                        if (allGroupsWrapper.allGroups.size < groupPerPage) isLastPage = true
                        getAllGroups.value = allGroupsWrapper.allGroups
                    }
                }

                override fun onFailure(call: Call<GroupsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getGroupsSearch(searchTxt: String) {
        EntourageApplication.get().apiModule.groupRequest.getGroupsSearch(searchTxt)
            .enqueue(object : Callback<GroupsListWrapper> {
                override fun onResponse(
                    call: Call<GroupsListWrapper>,
                    response: Response<GroupsListWrapper>
                ) {
                    response.body()?.let { allGroupsWrapper ->
                        getGroupsSearch.value = allGroupsWrapper.allGroups
                    }
                }

                override fun onFailure(call: Call<GroupsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getMyGroups(page: Int, per: Int, userId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getMyGroups(userId, page, per)
            .enqueue(object : Callback<GroupsListWrapper> {
                override fun onResponse(
                    call: Call<GroupsListWrapper>,
                    response: Response<GroupsListWrapper>
                ) {
                    response.body()?.let { allGroupsWrapper ->
                        if (allGroupsWrapper.allGroups.size < groupPerPage) isLastPage = true
                        getAllMyGroups.value = allGroupsWrapper.allGroups
                    }
                }

                override fun onFailure(call: Call<GroupsListWrapper>, t: Throwable) {
                }
            })
    }

    fun joinGroup(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.joinGroup(groupId)
            .enqueue(object : Callback<EntourageUserResponse> {
                override fun onResponse(
                    call: Call<EntourageUserResponse>,
                    response: Response<EntourageUserResponse>
                ) {
                    hasUserJoinedGroup.value =
                        response.isSuccessful && response.body()?.user != null
                    RefreshController.shouldRefreshFragment =
                        response.isSuccessful && response.body()?.user != null
                }

                override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                    hasUserJoinedGroup.value = false
                }
            })
    }

    fun leaveGroup(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.leaveGroup(groupId)
            .enqueue(object : Callback<EntourageUserResponse> {
                override fun onResponse(
                    call: Call<EntourageUserResponse>,
                    response: Response<EntourageUserResponse>
                ) {
                    hasUserLeftGroup.value =
                        response.isSuccessful && response.body()?.user != null
                    RefreshController.shouldRefreshFragment =
                        response.isSuccessful && response.body()?.user != null

                }

                override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                    hasUserLeftGroup.value = false
                }
            })
    }

    fun getGroupMembers(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getMembers(groupId)
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

    fun getGroupPosts(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getGroupPosts(groupId)
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

    fun getPostComments(groupId: Int, postId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getPostComments(groupId, postId)
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

    fun getGroupMembersSearch(searchTxt: String) {
        val listTmp: MutableList<EntourageUser> = mutableListOf()
        getMembers.value?.forEach {
            if (it.displayName?.lowercase()?.contains(searchTxt.lowercase()) == true) {
                listTmp.add(it)
            }
        }
        getMembersSearch.value = listTmp
    }

    fun addPost(message: String?, file: File, groupId: Int) {
        if (isSendingCreatePost) return
        isSendingCreatePost = true
        val request = RequestContent("image/jpeg")
        EntourageApplication.get().apiModule.groupRequest.prepareAddPost(groupId, request)
            .enqueue(object : Callback<PrepareAddPostResponse> {
                override fun onResponse(
                    call: Call<PrepareAddPostResponse>,
                    response: Response<PrepareAddPostResponse>
                ) {
                    if (response.isSuccessful) {
                        val presignedUrl = response.body()?.presignedUrl
                        val uploadKey = response.body()?.uploadKey
                        presignedUrl?.let {
                            uploadFile(groupId, file, presignedUrl, uploadKey, message)
                        } ?: run { isSendingCreatePost = false }
                    }
                    else {
                        isSendingCreatePost = false
                    }
                }

                override fun onFailure(call: Call<PrepareAddPostResponse>, t: Throwable) {
                    hasUserLeftGroup.value = false
                    isSendingCreatePost = false
                }
            })
    }

    fun uploadFile(
        groupId: Int,
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
                addPost(groupId, chatMessage)
            }
        })
    }

    fun addPost(groupId: Int, params: ArrayMap<String, Any>) {
        if (isSendingCreatePost) return
        isSendingCreatePost = true
        EntourageApplication.get().apiModule.groupRequest.addPost(groupId, params)
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

    fun addComment(groupId: Int, comment: Post?) {
        val messageChat = ArrayMap<String, Any>()
        messageChat["content"] = comment?.content
        messageChat["parent_id"] = comment?.postId.toString()
        val chatMessage = ArrayMap<String, Any>()
        chatMessage["chat_message"] = messageChat
        EntourageApplication.get().apiModule.groupRequest.addPost(groupId, chatMessage)
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

    fun sendReport(
        groupId: Int,
        reason: String,
        selectedSignalsIdList: MutableList<String>
    ) {
        EntourageApplication.get().apiModule.groupRequest.reportGroup(
            groupId,
            ReportWrapper(Report(reason, selectedSignalsIdList))
        ).enqueue(object :
            Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isGroupReported.value = false
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                isGroupReported.value = response.isSuccessful
            }
        })
    }

    fun sendReportPost(
        groupId: Int,
        postId: Int,
        reason: String,
        selectedSignalsIdList: MutableList<String>
    ) {

        EntourageApplication.get().apiModule.groupRequest.reportPost(
            groupId,
            postId,
            ReportWrapper(Report(reason, selectedSignalsIdList))
        ).enqueue(object :
            Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isPostReported.value = false
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                isPostReported.value = response.isSuccessful
            }
        })
    }

    fun getGroupEvents(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getGroupEvents(groupId)
            .enqueue(object : Callback<EventsListWrapper> {
                override fun onResponse(
                    call: Call<EventsListWrapper>,
                    response: Response<EventsListWrapper>
                ) {
                    response.body()?.let { allEventsWrapper ->
                        getAllEvents.value = allEventsWrapper.allEvents
                    }

                }

                override fun onFailure(call: Call<EventsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getUnreadCount() {
        EntourageApplication.get().apiModule.userRequest.getUnreadCountForUser()
            .enqueue(object : Callback<UnreadCountWrapper> {
                override fun onResponse(
                    call: Call<UnreadCountWrapper>,
                    response: Response<UnreadCountWrapper>
                ) {
                    if (response.isSuccessful) {
                        unreadMessages.value = response.body()?.unreadMessages
                    }
                }

                override fun onFailure(call: Call<UnreadCountWrapper>, t: Throwable) {
                    unreadMessages.value = null
                }
            })
    }

    fun getCurrentParentPost(eventId: Int, postId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getPostDetail(eventId,postId)
            .enqueue(object : Callback<PostWrapper> {
                override fun onResponse(
                    call: Call<PostWrapper>,
                    response: Response<PostWrapper>
                ) {
                    response.body()?.let { post ->
                        getCurrentParentPost.value = post.post
                    }
                }

                override fun onFailure(call: Call<PostWrapper>, t: Throwable) {
                }
            })
    }
}