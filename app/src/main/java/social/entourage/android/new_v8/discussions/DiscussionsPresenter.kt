package social.entourage.android.new_v8.discussions

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.*
import social.entourage.android.new_v8.home.UnreadMessages
import social.entourage.android.new_v8.models.Conversation
import social.entourage.android.new_v8.models.GroupMember
import social.entourage.android.new_v8.models.Post

/**
 * Created by - on 15/11/2022.
 */
class DiscussionsPresenter {

    var getAllMessages = MutableLiveData<MutableList<Conversation>>()
    var isLoading: Boolean = false
    var isLastPage: Boolean = false

    var isConversationReported = MutableLiveData<Boolean>()
    var hasUserLeftConversation = MutableLiveData<Boolean>()

    var getAllComments = MutableLiveData<MutableList<Post>?>()
    var commentPosted = MutableLiveData<Post?>()

    var unreadMessages = MutableLiveData<UnreadMessages?>()

    var detailConversation = MutableLiveData<Conversation?>()

    var newConversation = MutableLiveData<Conversation?>()

    var getMembersSearch = MutableLiveData<MutableList<GroupMember>>()

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

    fun createOrGetConversation(userId:Int) {
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
}