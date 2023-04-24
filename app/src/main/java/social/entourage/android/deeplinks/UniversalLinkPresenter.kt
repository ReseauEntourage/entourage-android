package social.entourage.android.deeplinks

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.request.ContribWrapper
import social.entourage.android.api.request.DemandWrapper
import social.entourage.android.api.request.EventWrapper
import social.entourage.android.api.request.GroupWrapper
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
                    Timber.wtf("wtf " + response.body())
                    if (response.isSuccessful) {
                        response.body()?.let { actionWrapper ->
                            callback.onRetrievedAction(actionWrapper.action,true)
                        }
                    }
                }

                override fun onFailure(call: Call<ContribWrapper>, t: Throwable) {
                    Timber.wtf("wtf failure")

                }
            })
    }

}

interface UniversalLinksPresenterCallback{
    fun onRetrievedEvent(event: Events)
    fun onRetrievedGroup(group:Group)
    fun onRetrievedAction(action:Action, isContrib:Boolean)
}