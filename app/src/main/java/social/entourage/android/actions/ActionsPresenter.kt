package social.entourage.android.actions

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionCancel
import social.entourage.android.api.request.ContribCancelWrapper
import social.entourage.android.api.request.ContribWrapper
import social.entourage.android.api.request.ContribsListWrapper
import social.entourage.android.api.request.DemandCancelWrapper
import social.entourage.android.api.request.DemandWrapper
import social.entourage.android.api.request.DemandsListWrapper
import social.entourage.android.api.request.MyActionsListWrapper
import social.entourage.android.api.request.PrepareAddPostResponse
import social.entourage.android.api.request.Report
import social.entourage.android.api.request.ReportWrapper
import social.entourage.android.api.request.RequestContent
import social.entourage.android.api.request.UnreadCountWrapper
import social.entourage.android.home.UnreadMessages
import social.entourage.android.tools.utils.Utils
import timber.log.Timber
import java.io.File
import java.io.IOException

class ActionsPresenter : ViewModel() {

    var getAllActions = MutableLiveData<MutableList<Action>>()
    var errorLoadingActions = MutableLiveData<Boolean>()
    var getAction = MutableLiveData<Action?>()

    var isActionReported = MutableLiveData<Boolean>()
    var newActionCreated = MutableLiveData<Action?>()

    var isActionUpdated = MutableLiveData<Boolean>()

    var isLoading: Boolean = false
    var isLastPage: Boolean = false
    var isSendingCreateContrib = false

    var unreadMessages = MutableLiveData<UnreadMessages?>()

    val searchQuery = MutableLiveData<String>()
    val actionSearch = MutableLiveData<MutableList<Action>>()
    var isLastPageSearch = false
    var isContrib = true
    var isMine = false
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun getAllContribs(page: Int, per: Int,distance:Int?,latitude:Double?,longitude:Double?,sections: String?) {
        EntourageApplication.get().apiModule.actionsRequest.getAllActionsContrib(page,per,sections,distance,latitude,longitude)
            .enqueue(object : Callback<ContribsListWrapper> {
                override fun onResponse(
                    call: Call<ContribsListWrapper>,
                    response: Response<ContribsListWrapper>
                ) {
                    response.body()?.let { allActionsWrapper ->
                        if (allActionsWrapper.allActions.size < EVENTS_PER_PAGE) isLastPage = true
                        getAllActions.value = allActionsWrapper.allActions
                    }
                }
                override fun onFailure(call: Call<ContribsListWrapper>, t: Throwable) {
                    errorLoadingActions.value = true
                }
            })
    }

    fun getAllDemands(page: Int, per: Int,distance:Int?,latitude:Double?,longitude:Double?,sections: String?) {
        EntourageApplication.get().apiModule.actionsRequest.getAllActionsDemand(page,per,sections,distance,latitude,longitude)
            .enqueue(object : Callback<DemandsListWrapper> {
                override fun onResponse(
                    call: Call<DemandsListWrapper>,
                    response: Response<DemandsListWrapper>
                ) {
                    response.body()?.let { allActionsWrapper ->
                        if (allActionsWrapper.allActions.size < EVENTS_PER_PAGE) isLastPage = true
                        getAllActions.value = allActionsWrapper.allActions
                    }
                }
                override fun onFailure(call: Call<DemandsListWrapper>, t: Throwable) {
                    errorLoadingActions.value = true
                }
            })
    }

    fun getMyActions(page: Int, per: Int) {
        EntourageApplication.get().apiModule.actionsRequest.getMyActions(page,per)
            .enqueue(object : Callback<MyActionsListWrapper> {
                override fun onResponse(
                    call: Call<MyActionsListWrapper>,
                    response: Response<MyActionsListWrapper>
                ) {
                    response.body()?.let { allActionsWrapper ->
                        if (allActionsWrapper.allActions.size < EVENTS_PER_PAGE) isLastPage = true
                        getAllActions.value = allActionsWrapper.allActions
                    }
                }
                override fun onFailure(call: Call<MyActionsListWrapper>, t: Throwable) {
                    errorLoadingActions.value = true
                }
            })
    }

    /*
        Report
     */

    fun sendReport(id: Int, reason: String,
                   selectedSignalsIdList: MutableList<String>,
                   isDemand: Boolean) {

        if (!isDemand) {
            sendContribReport(id,reason,selectedSignalsIdList)
        }
        else {
            sendDemandReport(id,reason,selectedSignalsIdList)
        }
    }

    private fun sendContribReport(id: Int, reason: String,
                                  selectedSignalsIdList: MutableList<String>) {
        val userRequest = EntourageApplication.get().apiModule.actionsRequest
        val call = userRequest.reportContribution(
            id, ReportWrapper(Report(reason, selectedSignalsIdList))
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isActionReported.value = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                isActionReported.value = response.isSuccessful
            }
        })
    }

    private fun sendDemandReport(id: Int, reason: String,
                                 selectedSignalsIdList: MutableList<String>) {
        val userRequest = EntourageApplication.get().apiModule.actionsRequest
        val call = userRequest.reportDemand(
            id, ReportWrapper(Report(reason, selectedSignalsIdList))
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isActionReported.value = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                isActionReported.value = response.isSuccessful
            }
        })
    }

    /*
        Detail
     */

    fun getDetailAction(id: Int, isDemand: Boolean) {
        if (isDemand) {
            getDemand(id)
        }
        else {
            getContribution(id)
        }
    }

    private fun getDemand(id: Int) {
        EntourageApplication.get().apiModule.actionsRequest.getDemand(id)
            .enqueue(object : Callback<DemandWrapper> {
                override fun onResponse(
                    call: Call<DemandWrapper>,
                    response: Response<DemandWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { actionWrapper ->
                            getAction.value = actionWrapper.action
                        }
                    }
                    else {
                        getAction.value = null
                    }
                }

                override fun onFailure(call: Call<DemandWrapper>, t: Throwable) {
                    getAction.value = null
                }
            })
    }

    private fun getContribution(id: Int) {
        EntourageApplication.get().apiModule.actionsRequest.getContribution(id)
            .enqueue(object : Callback<ContribWrapper> {
                override fun onResponse(
                    call: Call<ContribWrapper>,
                    response: Response<ContribWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { actionWrapper ->
                            getAction.value = actionWrapper.action
                        }
                    }
                    else {
                        getAction.value = null
                    }
                }

                override fun onFailure(call: Call<ContribWrapper>, t: Throwable) {
                    getAction.value = null
                }
            })
    }

    /*
        Create / update
     */

    fun createContribWithImage(autoPost: Boolean,action: Action, file: File, isUpdate:Boolean) {

        if (isSendingCreateContrib) return
        isSendingCreateContrib = true
        val request = RequestContent("image/jpeg")
        EntourageApplication.get().apiModule.actionsRequest.prepareAddImage(request)
            .enqueue(object : Callback<PrepareAddPostResponse> {
                override fun onResponse(
                    call: Call<PrepareAddPostResponse>,
                    response: Response<PrepareAddPostResponse>
                ) {
                    if (response.isSuccessful) {
                        val presignedUrl = response.body()?.presignedUrl
                        val uploadKey = response.body()?.uploadKey
                        presignedUrl?.let {
                            uploadFile(action, file, presignedUrl, uploadKey,isUpdate,autoPost)
                        } ?: run { isSendingCreateContrib = false }
                    }
                    else {
                        isSendingCreateContrib = false
                    }
                }

                override fun onFailure(call: Call<PrepareAddPostResponse>, t: Throwable) {
                    isSendingCreateContrib = false
                }
            })
    }

    fun uploadFile(action: Action, file: File, presignedUrl: String, uploadKey: String?, isUpdate:Boolean,autoPost: Boolean) {
        val client: OkHttpClient = EntourageApplication.get().apiModule.okHttpClient
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(presignedUrl)
            .put(requestBody)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e("response ${e.message}")
                isSendingCreateContrib = false
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                isSendingCreateContrib = false
                action.imageUrl = uploadKey
                if (isUpdate) {
                    updateContrib(action)
                }
                else {
                    createContrib(autoPost,action)
                }
            }
        })
    }

    fun createDemand(autoPost:Boolean, action: Action) {
        EntourageApplication.get().apiModule.actionsRequest.createActionDemand(autoPost, DemandWrapper(action))
            .enqueue(object : Callback<DemandWrapper> {
                override fun onResponse(
                    call: Call<DemandWrapper>,
                    response: Response<DemandWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            newActionCreated.value = it.action
                        } ?: run {
                            newActionCreated.value = null
                        }
                    } else {
                        newActionCreated.value = null
                    }
                }

                override fun onFailure(call: Call<DemandWrapper>, t: Throwable) {
                    newActionCreated.value = null
                }
            })
    }

    fun createAction(action: Action, isDemand:Boolean, uri:Uri?, context: Context, autoPost: Boolean) {
        if (isDemand) {
            createDemand(autoPost,action)
        }
        else {
            uri?.let { it ->
                val file = Utils.getFile(context, it)
                createContribWithImage(autoPost,action,file,false)
            } ?: run { createContrib(autoPost,action) }
        }
    }

    fun createContrib(autoPost:Boolean,action: Action) {
        EntourageApplication.get().apiModule.actionsRequest.createActionContrib(autoPost,ContribWrapper(action))
            .enqueue(object : Callback<ContribWrapper> {
                override fun onResponse(
                    call: Call<ContribWrapper>,
                    response: Response<ContribWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            newActionCreated.value = it.action
                        } ?: run {
                            newActionCreated.value = null
                        }
                    } else {
                        newActionCreated.value = null
                    }
                }

                override fun onFailure(call: Call<ContribWrapper>, t: Throwable) {
                    newActionCreated.value = null
                }
            })
    }

    fun updateAction(actionEdited: Action?, newAction: Action, isDemand: Boolean, uri:Uri?, context: Context,autoPost: Boolean) {

        if (actionEdited == null || newAction.id == null) {
            newActionCreated.value = null
            return
        }

        if (isDemand) {
            updateDemand(newAction)
        }
        else {
            uri?.let { it ->
                val file = Utils.getFile(context, it)
                createContribWithImage(autoPost,newAction,file,true)
            } ?: run { updateContrib(newAction) }
        }
    }

    fun updateDemand(action: Action) {
        EntourageApplication.get().apiModule.actionsRequest.updateActionDemand(action.id!!,DemandWrapper(action))
            .enqueue(object : Callback<DemandWrapper> {
                override fun onResponse(
                    call: Call<DemandWrapper>,
                    response: Response<DemandWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            isActionUpdated.value = true
                        } ?: run {
                            isActionUpdated.value = false
                        }
                    } else {
                        isActionUpdated.value = false
                    }
                }

                override fun onFailure(call: Call<DemandWrapper>, t: Throwable) {
                    isActionUpdated.value = false
                }
            })
    }

    fun updateContrib(action: Action) {
        EntourageApplication.get().apiModule.actionsRequest.updateActionContrib(action.id!!,ContribWrapper(action))
            .enqueue(object : Callback<ContribWrapper> {
                override fun onResponse(
                    call: Call<ContribWrapper>,
                    response: Response<ContribWrapper>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            isActionUpdated.value =true
                        } ?: run {
                            isActionUpdated.value = false
                        }
                    } else {
                        isActionUpdated.value = false
                    }
                }

                override fun onFailure(call: Call<ContribWrapper>, t: Throwable) {
                    isActionUpdated.value = false
                }
            })
    }

    /*
        Cancel
     */

    fun cancelAction(id: Int, isDemand:Boolean, isClosedOk:Boolean, message:String?) {
        if (!isDemand) {
            cancelContribution(id,isClosedOk,message)
        }
        else {
            cancelDemand(id,isClosedOk,message)
        }
    }

    private fun cancelDemand(id: Int, isClosedOk:Boolean, message:String?) {
        val params = ActionCancel(isClosedOk,message)

        EntourageApplication.get().apiModule.actionsRequest.cancelDemand(id,
            DemandCancelWrapper(params)
        )
            .enqueue(object : Callback<DemandWrapper> {
                override fun onResponse(
                    call: Call<DemandWrapper>,
                    response: Response<DemandWrapper>
                ) {
                    getAction.value = response.body()?.action
                    getAction.value?.setCancel()
                    getAction.postValue( getAction.value)
                }

                override fun onFailure(call: Call<DemandWrapper>, t: Throwable) {}
            })
    }

    private fun cancelContribution(id: Int, isClosedOk:Boolean, message:String?) {
        val params = ActionCancel(isClosedOk,message)
        EntourageApplication.get().apiModule.actionsRequest.cancelContribution(id,
            ContribCancelWrapper(params)
        )
            .enqueue(object : Callback<ContribWrapper> {
                override fun onResponse(
                    call: Call<ContribWrapper>,
                    response: Response<ContribWrapper>
                ) {
                    getAction.value = response.body()?.action
                    getAction.value?.setCancel()
                    getAction.postValue( getAction.value)
                }

                override fun onFailure(call: Call<ContribWrapper>, t: Throwable) {}
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

    fun getAllContribsWithFilter(page: Int, per: Int, distance: Int?, latitude: Double?, longitude: Double?, sections: String?) {
        EntourageApplication.get().apiModule.actionsRequest.getAllActionsContribWithFilter(page, per, sections, distance, latitude, longitude)
            .enqueue(object : Callback<ContribsListWrapper> {
                override fun onResponse(call: Call<ContribsListWrapper>, response: Response<ContribsListWrapper>) {
                    response.body()?.let { allActionsWrapper ->
                        if (allActionsWrapper.allActions.size < EVENTS_PER_PAGE) isLastPage = true
                        getAllActions.value = allActionsWrapper.allActions
                    }
                }
                override fun onFailure(call: Call<ContribsListWrapper>, t: Throwable) {
                    errorLoadingActions.value = true
                }
            })
    }

    fun getAllDemandsWithFilter(page: Int, per: Int, distance: Int?, latitude: Double?, longitude: Double?, sections: String?) {
        EntourageApplication.get().apiModule.actionsRequest.getAllActionsDemandWithFilter(page, per, sections, distance, latitude, longitude)
            .enqueue(object : Callback<DemandsListWrapper> {
                override fun onResponse(call: Call<DemandsListWrapper>, response: Response<DemandsListWrapper>) {
                    response.body()?.let { allActionsWrapper ->
                        if (allActionsWrapper.allActions.size < EVENTS_PER_PAGE) isLastPage = true
                        getAllActions.value = allActionsWrapper.allActions
                    }
                }
                override fun onFailure(call: Call<DemandsListWrapper>, t: Throwable) {
                    errorLoadingActions.value = true
                }
            })
    }
    fun getAllContribsWithSearchQuery(query: String, page: Int, per: Int) {
        EntourageApplication.get().apiModule.actionsRequest.getAllContribsWithSearchQuery(query, page, per)
            .enqueue(object : Callback<ContribsListWrapper> {
                override fun onResponse(call: Call<ContribsListWrapper>, response: Response<ContribsListWrapper>) {
                    response.body()?.let { allActionsWrapper ->
                        getAllActions.value = allActionsWrapper.allActions
                        if (allActionsWrapper.allActions.size < per) isLastPageSearch = true
                    }
                }

                override fun onFailure(call: Call<ContribsListWrapper>, t: Throwable) {
                    errorLoadingActions.value = true
                }
            })
    }

    fun getAllDemandsWithSearchQuery(query: String, page: Int, per: Int) {
        EntourageApplication.get().apiModule.actionsRequest.getAllDemandsWithSearchQuery(query, page, per)
            .enqueue(object : Callback<DemandsListWrapper> {
                override fun onResponse(call: Call<DemandsListWrapper>, response: Response<DemandsListWrapper>) {
                    response.body()?.let { allActionsWrapper ->
                        getAllActions.value = allActionsWrapper.allActions
                        if (allActionsWrapper.allActions.size < per) isLastPageSearch = true
                    }
                }

                override fun onFailure(call: Call<DemandsListWrapper>, t: Throwable) {
                    errorLoadingActions.value = true
                }
            })
    }

    companion object {
        const val EVENTS_PER_PAGE = 10
    }
}