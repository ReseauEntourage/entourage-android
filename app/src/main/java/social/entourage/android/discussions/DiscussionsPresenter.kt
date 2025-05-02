package social.entourage.android.discussions

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.*
import social.entourage.android.home.UnreadMessages
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserBlockedUser
import timber.log.Timber

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

}