package social.entourage.android.deeplinks

import android.util.Log
import androidx.collection.ArrayMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.request.*
import timber.log.Timber

class UniversalLinkPresenter(val callback:UniversalLinksPresenterCallback) {


    fun getEvent(id: String) {
        EntourageApplication.get().apiModule.appLinksRequest.getEventFromHash(id)
            .enqueue(object : Callback<EventWrapper> {
                override fun onResponse(
                    call: Call<EventWrapper>,
                    response: Response<EventWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { eventWrapper ->
                            callback.onRetrievedEvent(eventWrapper.event)
                        }
                    }
                    if(response.code() >= 400){
                        callback.onErrorRetrievedEvent()
                    }
                }

                override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                }
            })
    }

    fun getGroup(id: String) {
        EntourageApplication.get().apiModule.appLinksRequest.getGroupFromHash(id)
            .enqueue(object : Callback<GroupWrapper> {
                override fun onResponse(
                    call: Call<GroupWrapper>,
                    response: Response<GroupWrapper>
                ) {

                    if (response.isSuccessful) {
                        response.body()?.let { groupWrapper ->
                            callback.onRetrievedGroup(groupWrapper.group)
                        }
                    }
                    if(response.code() >= 400){
                        callback.onErrorRetrievedGroup()
                    }
                }


                override fun onFailure(call: Call<GroupWrapper>, t: Throwable) {
                }
            })
    }

    fun getDetailAction(id: String, isDemand: Boolean) {
        if (isDemand) {
            getDemand(id)
        }
        else {
            getContribution(id)
        }
    }

    private fun getDemand(id: String) {
        EntourageApplication.get().apiModule.appLinksRequest.getDemandFromHash(id)
            .enqueue(object : Callback<DemandWrapper> {
                override fun onResponse(
                    call: Call<DemandWrapper>,
                    response: Response<DemandWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { actionWrapper ->
                            callback.onRetrievedAction(actionWrapper.action,false)
                        }
                        if(response.code() >= 400){
                            callback.onErrorRetrievedAction()
                        }
                    }
                }
                override fun onFailure(call: Call<DemandWrapper>, t: Throwable) {

                }
            })
    }

    private fun getContribution(id: String) {
        EntourageApplication.get().apiModule.appLinksRequest.getContributionFromHash(id)
            .enqueue(object : Callback<ContribWrapper> {
                override fun onResponse(
                    call: Call<ContribWrapper>,
                    response: Response<ContribWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { actionWrapper ->
                            callback.onRetrievedAction(actionWrapper.action,true)
                        }
                    }
                    if(response.code() >= 400){
                        callback.onErrorRetrievedAction()
                    }
                }

                override fun onFailure(call: Call<ContribWrapper>, t: Throwable) {

                }
            })
    }
    fun getDetailConversation(conversationId: String) {
        EntourageApplication.get().apiModule.appLinksRequest.getDiscussionFromHash(conversationId)
            .enqueue(object : Callback<DiscussionDetailWrapper> {
                override fun onResponse(
                    call: Call<DiscussionDetailWrapper>,
                    response: Response<DiscussionDetailWrapper>
                ) {
                    response.body()?.let { discussionWrapper ->
                        callback.onRetrievedDiscussion(discussionWrapper.conversation)
                    }
                    if(response.code() >= 400){
                        callback.onErrorRetrievedDiscussion()
                    }
                }

                override fun onFailure(call: Call<DiscussionDetailWrapper>, t: Throwable) {

                }
            })
    }

    fun addUserToConversation(conversationId: String) {
        EntourageApplication.get().apiModule.discussionsRequest.addUserToConversation(conversationId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                       callback.onUserJoinedConversation()
                    } else {
                        callback.onUserErrorJoinedConversation()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback.onUserErrorJoinedConversation()
                }
            })
    }
}

interface UniversalLinksPresenterCallback{
    fun onRetrievedEvent(event: Events)
    fun onRetrievedGroup(group:Group)
    fun onRetrievedAction(action:Action, isContrib:Boolean)
    fun onRetrievedDiscussion(discussion: Conversation)
    fun onUserJoinedConversation()

    fun onErrorRetrievedDiscussion()
    fun onErrorRetrievedGroup()
    fun onErrorRetrievedEvent()
    fun onErrorRetrievedAction()

    fun onUserErrorJoinedConversation()

}