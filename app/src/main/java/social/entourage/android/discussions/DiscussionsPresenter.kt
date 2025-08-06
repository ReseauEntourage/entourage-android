package social.entourage.android.discussions

import android.util.Log
import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.*
import social.entourage.android.home.UnreadMessages
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.ConversationMembership
import social.entourage.android.api.model.ConversationMembershipsWrapper
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserBlockedUser
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Created by - on 15/11/2022.
 */
class DiscussionsPresenter:ViewModel() {

    var getAllMessages = MutableLiveData<MutableList<Conversation>>()
    var isLoading: Boolean = false
    var isLastPage: Boolean = false
    var isConversationReported = MutableLiveData<Boolean>()
    var isConversationDeleted = MutableLiveData<Boolean>()
    var hasUserLeftConversation = MutableLiveData<Boolean>()
    var isMessageDeleted = MutableLiveData<Boolean>()

    var getAllComments = MutableLiveData<MutableList<Post>?>()
    var commentPosted = MutableLiveData<Post?>()

    var unreadMessages = MutableLiveData<UnreadMessages?>()

    var detailConversation = MutableLiveData<Conversation?>()

    var newConversation = MutableLiveData<Conversation?>()

    var getMembersSearch = MutableLiveData<MutableList<GroupMember>>()
    var conversationUsers = MutableLiveData<MutableList<GroupMember>>()

    var hasBlockUser = MutableLiveData<Boolean>()
    var getBlockedUsers = MutableLiveData<MutableList<UserBlockedUser>?>()

    var hasUserUnblock = MutableLiveData<Boolean>()
    var hasUserjoined = MutableLiveData<Boolean>()
    val messageDeleted = MutableLiveData<String>()

    var currentPageComments = 1
    val perPageComments = 50
    var isLastPageComments = false
    var isLoadingComments = false
    val memberships = MutableLiveData<List<ConversationMembership>>()
    var currentPageMemberships = 1
    val perPageMemberships = 50
    var isLastPageMemberships = false
    var isLoadingMemberships = false

    fun getAllMessages(page: Int, per: Int) {
        isLoading = true
        EntourageApplication.get().apiModule.discussionsRequest.getAllConversations(page, per)
            .enqueue(object : Callback<DiscussionsListWrapper> {
                override fun onResponse(
                    call: Call<DiscussionsListWrapper>,
                    response: Response<DiscussionsListWrapper>
                ) {
                    response.body()?.let { allConversationsWrapper ->
                        if (allConversationsWrapper.allConversations.size < messagesPerPage) isLastPage = true
                        getAllMessages.value = allConversationsWrapper.allConversations
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<DiscussionsListWrapper>, t: Throwable) {
                    isLoading = false
                }
            })
    }

    //Detail Conversation:
    fun addComment(groupId: Int, comment: Post?) {
        val messageChat = ArrayMap<String, Any>()
        messageChat["content"] = comment?.content
        val chatMessage = ArrayMap<String, Any>()
        chatMessage["chat_message"] = messageChat
        EntourageApplication.get().apiModule.discussionsRequest.addPost(groupId, chatMessage)
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

    fun getDetailConversation(conversationId: Int) {
        EntourageApplication.get().apiModule.discussionsRequest.getDetailConversation(conversationId)
            .enqueue(object : Callback<DiscussionDetailWrapper> {
                override fun onResponse(
                    call: Call<DiscussionDetailWrapper>,
                    response: Response<DiscussionDetailWrapper>
                ) {
                    response.body()?.let { discussionWrapper ->
                        detailConversation.value = discussionWrapper.conversation
                    }
                }

                override fun onFailure(call: Call<DiscussionDetailWrapper>, t: Throwable) {
                    detailConversation.value = null
                }
            })
    }

    fun getPostComments(conversationId: Int) {
        //TODO: pagination after MVP
        EntourageApplication.get().apiModule.discussionsRequest.getMessagesFor(conversationId,1,200)
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
                    getAllComments.value = null
                }
            })
    }

    fun createOrGetConversation(userId:String) {
        val params = ArrayMap<String,Any>()
        val userParam = ArrayMap<String,Any>()
        userParam["user_id"] = userId
        params["conversation"] = userParam
        EntourageApplication.get().apiModule.discussionsRequest.createOrGetConversation(params)
            .enqueue(object : Callback<DiscussionDetailWrapper> {
                override fun onResponse(
                    call: Call<DiscussionDetailWrapper>,
                    response: Response<DiscussionDetailWrapper>
                ) {
                    response.body()?.let { allCommentsWrapper ->
                        newConversation.value = allCommentsWrapper.conversation
                    }
                }

                override fun onFailure(call: Call<DiscussionDetailWrapper>, t: Throwable) {
                    newConversation.value = null
                }
            })
    }

    fun deleteMessage(
        discussionId: Int,
        messageId: Int,
    ) {
        EntourageApplication.get().apiModule.discussionsRequest.deleteMessage(
            discussionId,
            messageId
        ).enqueue(object :
            Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                isMessageDeleted.value = response.isSuccessful
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

    fun getMembersSearch(searchTxt: String) {
        val listTmp: MutableList<GroupMember> = mutableListOf()
        detailConversation.value?.members?.forEach {
            if (it.displayName?.lowercase()?.contains(searchTxt.lowercase()) == true) {
                listTmp.add(it)
            }
        }
        getMembersSearch.value = listTmp
    }

    fun sendReport(id: Int, reason: String,
                   selectedSignalsIdList: MutableList<String>) {
        val userRequest = EntourageApplication.get().apiModule.discussionsRequest
        val call = userRequest.reportConversation(
            id, ReportWrapper(Report(reason, selectedSignalsIdList))
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isConversationReported.value = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                isConversationReported.value = response.isSuccessful
            }
        })
    }

    fun leaveConverstion(id: Int) {
        val userRequest = EntourageApplication.get().apiModule.discussionsRequest
        val call = userRequest.leaveConversation(id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                hasUserLeftConversation.value = true
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                hasUserLeftConversation.value = response.isSuccessful
            }
        })
    }

    //BLock user

    fun blockUser(userId:Int) {
        val userRequest = EntourageApplication.get().apiModule.discussionsRequest
        val call = userRequest.blockUser(userId)
        call.enqueue(object : Callback<UserBlockedWrapper> {
            override fun onFailure(call: Call<UserBlockedWrapper>, t: Throwable) {
                hasBlockUser.value = false
            }

            override fun onResponse(call: Call<UserBlockedWrapper>, response: Response<UserBlockedWrapper>) {
                hasBlockUser.value = response.isSuccessful
            }
        })
    }

    fun getBlockedUsers() {
        val userRequest = EntourageApplication.get().apiModule.discussionsRequest
        val call = userRequest.getBlockedUsers()
        call.enqueue(object : Callback<UsersBlockedWrapper> {
            override fun onFailure(call: Call<UsersBlockedWrapper>, t: Throwable) {
                getBlockedUsers.value = null
            }

            override fun onResponse(call: Call<UsersBlockedWrapper>, response: Response<UsersBlockedWrapper>) {
                getBlockedUsers.value = response.body()?.blockedUsers
            }
        })
    }

    fun unblockUsers(usersId: ArrayList<Int>) {

        val param = ArrayMap<String,Any>()
        param["blocked_user_ids"] = usersId
        val userRequest = EntourageApplication.get().apiModule.discussionsRequest
        val call = userRequest.unblockUsers(param)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                hasUserUnblock.value = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                hasUserUnblock.value = true
            }
        })
    }

    fun addUserToConversation(conversationId: String) {
        EntourageApplication.get().apiModule.discussionsRequest.addUserToConversation(conversationId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        hasUserjoined.postValue(true)
                    } else {
                        hasUserjoined.postValue(false)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    hasUserjoined.postValue(false)
                }
            })
    }

    fun fetchAllConversations(page: Int, per: Int) {
        isLoading = true
        EntourageApplication.get().apiModule.discussionsRequest.getAllConversations(page, per)
            .enqueue(object : Callback<DiscussionsListWrapper> {
                override fun onResponse(
                    call: Call<DiscussionsListWrapper>,
                    response: Response<DiscussionsListWrapper>
                ) {
                    response.body()?.let { wrapper ->
                        if (wrapper.allConversations.size < per) isLastPage = true
                        getAllMessages.value = wrapper.allConversations
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<DiscussionsListWrapper>, t: Throwable) {
                    isLoading = false
                }
            })
    }

    fun fetchPrivateConversations(page: Int, per: Int) {
        isLoading = true
        EntourageApplication.get().apiModule.discussionsRequest.getPrivateConversations(page, per)
            .enqueue(object : Callback<DiscussionsListWrapper> {
                override fun onResponse(
                    call: Call<DiscussionsListWrapper>,
                    response: Response<DiscussionsListWrapper>
                ) {
                    response.body()?.let { wrapper ->
                        if (wrapper.allConversations.size < per) isLastPage = true
                        getAllMessages.value = wrapper.allConversations
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<DiscussionsListWrapper>, t: Throwable) {
                    isLoading = false
                }
            })
    }

    fun fetchOutingConversations(page: Int, per: Int) {
        isLoading = true
        EntourageApplication.get().apiModule.discussionsRequest.getOutingConversations(page, per)
            .enqueue(object : Callback<DiscussionsListWrapper> {
                override fun onResponse(
                    call: Call<DiscussionsListWrapper>,
                    response: Response<DiscussionsListWrapper>
                ) {
                    response.body()?.let { wrapper ->
                        if (wrapper.allConversations.size < per) isLastPage = true
                        getAllMessages.value = wrapper.allConversations
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<DiscussionsListWrapper>, t: Throwable) {
                    isLoading = false
                }
            })
    }

    fun fetchUsersForConversation(conversationId: Int) {
        EntourageApplication.get().apiModule.discussionsRequest
            .getUsersForConversation(conversationId)
            .enqueue(object : Callback<UserListWithConversationWrapper> {
                override fun onResponse(
                    call: Call<UserListWithConversationWrapper>,
                    response: Response<UserListWithConversationWrapper>
                ) {
                    response.body()?.let { wrapper ->
                        conversationUsers.value = wrapper.users
                    } ?: run {
                        conversationUsers.value = mutableListOf()
                    }
                }

                override fun onFailure(call: Call<UserListWithConversationWrapper>, t: Throwable) {
                    conversationUsers.value = mutableListOf()
                }
            })
    }

    fun addCommentWithImage(groupId: Int, message: String?, file: File) {
        val request = RequestContent("image/jpeg")
        EntourageApplication.get().apiModule.discussionsRequest.prepareAddPost(groupId, request)
            .enqueue(object : Callback<PrepareAddPostResponse> {
                override fun onResponse(call: Call<PrepareAddPostResponse>, response: Response<PrepareAddPostResponse>) {
                    if (response.isSuccessful) {
                        val presignedUrl = response.body()?.presignedUrl
                        val uploadKey = response.body()?.uploadKey
                        if (presignedUrl != null && uploadKey != null) {
                            uploadFile(groupId, file, presignedUrl, uploadKey, message)
                        }
                    }
                }

                override fun onFailure(call: Call<PrepareAddPostResponse>, t: Throwable) {
                    Log.wtf("wtf", "t : " + t.message )
                }
            })
    }

    private fun uploadFile(groupId: Int, file: File, presignedUrl: String, uploadKey: String, message: String?) {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = Request.Builder().url(presignedUrl).put(requestBody).build()

        EntourageApplication.get().apiModule.okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e("Upload failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    Timber.e("Upload image failed. HTTP ${response.code}")
                    return
                }

                val chatMessageMap = ArrayMap<String, Any>()
                chatMessageMap["image_url"] = uploadKey
                chatMessageMap["message_type"] = "image"
                chatMessageMap["content"] = message ?: "📷"

                val rootMap = ArrayMap<String, Any>()
                rootMap["chat_message"] = chatMessageMap

                Timber.d("POST /chat_messages payload: $rootMap")

                EntourageApplication.get().apiModule.discussionsRequest
                    .addPost(groupId, rootMap)
                    .enqueue(object : Callback<PostWrapper> {
                        override fun onResponse(call: Call<PostWrapper>, response: Response<PostWrapper>) {
                            Timber.d("Réponse message avec image: ${response.body()?.post}")
                            commentPosted.postValue(response.body()?.post)
                        }

                        override fun onFailure(call: Call<PostWrapper>, t: Throwable) {
                            Timber.e("Erreur lors de l’envoi du message image: ${t.message}")
                            commentPosted.postValue(null)
                        }
                    })
            }
        })
    }



    fun loadInitialComments(convId: Int) {
        currentPageComments = 1
        isLastPageComments = false
        fetchComments(convId, 1)
    }

    fun loadMoreComments(convId: Int) {
        if (isLoadingComments || isLastPageComments) return
        currentPageComments++
        fetchComments(convId, currentPageComments)
    }

    private fun fetchComments(convId: Int, page: Int) {
        isLoadingComments = true
        EntourageApplication.get().apiModule.discussionsRequest
            .getMessagesFor(convId, page, perPageComments)
            .enqueue(object : Callback<PostListWrapper> {
                override fun onResponse(call: Call<PostListWrapper>, resp: Response<PostListWrapper>) {
                    val posts = resp.body()?.posts ?: emptyList()
                    if (page == 1) getAllComments.value = posts.toMutableList()
                    else {
                        val current = getAllComments.value ?: mutableListOf()
                        current.addAll(0, posts)  // on insère au début
                        getAllComments.value = current
                    }
                    if (posts.size < perPageComments) isLastPageComments = true
                    isLoadingComments = false
                }
                override fun onFailure(call: Call<PostListWrapper>, t: Throwable) {
                    isLoadingComments = false
                }
            })
    }

    // In DiscussionsPresenter
    fun fetchMemberships(type: String?, reset: Boolean = false) {
        if (isLoadingMemberships) return
        if (reset) {
            currentPageMemberships = 1
            isLastPageMemberships = false
            memberships.value = emptyList()
        }
        if (isLastPageMemberships) return

        isLoadingMemberships = true
        EntourageApplication.get().apiModule.discussionsRequest
            .getConversationMemberships(type, currentPageMemberships, perPageMemberships)
            .enqueue(object : Callback<ConversationMembershipsWrapper> {
                override fun onResponse(
                    call: Call<ConversationMembershipsWrapper>,
                    response: Response<ConversationMembershipsWrapper>
                ) {
                    val newItems = response.body()?.memberships.orEmpty()
                    if (newItems.size < perPageMemberships) {
                        isLastPageMemberships = true
                    }
                    val current = memberships.value.orEmpty().toMutableList()
                    current.addAll(newItems)
                    memberships.postValue(current)
                    currentPageMemberships++
                    isLoadingMemberships = false
                }

                override fun onFailure(call: Call<ConversationMembershipsWrapper>, t: Throwable) {
                    isLoadingMemberships = false
                }
            })
    }
}