package social.entourage.android.new_v8.actions

import android.content.Context
import android.net.Uri
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
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.utils.Utils
import timber.log.Timber
import java.io.File
import java.io.IOException

class ActionsPresenter {

    val EVENTS_PER_PAGE = 10

    var getAllActions = MutableLiveData<MutableList<Action>>()
    var getAction = MutableLiveData<Action>()

    var isActionReported = MutableLiveData<Boolean>()
    var newActionCreated = MutableLiveData<Action?>()

    var isLoading: Boolean = false
    var isLastPage: Boolean = false
    var isSendingCreateContrib = false

    /*
        Gets
     */

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
                override fun onFailure(call: Call<ContribsListWrapper>, t: Throwable) {}
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
                override fun onFailure(call: Call<DemandsListWrapper>, t: Throwable) {}
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
                override fun onFailure(call: Call<MyActionsListWrapper>, t: Throwable) {}
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
                    Timber.e(response.body().toString())
                    if (response.isSuccessful) {
                        response.body()?.let { actionWrapper ->
                            getAction.value = actionWrapper.action
                        }
                    }
                }

                override fun onFailure(call: Call<DemandWrapper>, t: Throwable) {
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
                    Timber.e(response.body().toString())
                    if (response.isSuccessful) {
                        response.body()?.let { actionWrapper ->
                            getAction.value = actionWrapper.action
                        }
                    }
                }

                override fun onFailure(call: Call<ContribWrapper>, t: Throwable) {
                }
            })
    }

    /*
        Create / update
     */

    fun createContribWithImage(action: Action, file: File) {

        if (isSendingCreateContrib) return
        isSendingCreateContrib = true
        val request = RequestContent("image/jpeg")
        EntourageApplication.get().apiModule.actionsRequest.prepareAddImage(request)
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
                            uploadFile(action, file, presignedUrl, uploadKey)
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

    fun uploadFile(action: Action, file: File, presignedUrl: String, uploadKey: String?) {
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
                createContrib(action)
            }
        })
    }

    fun createAction(action: Action, isDemand:Boolean, uri:Uri?, context: Context) {
        if (isDemand) {
            createDemand(action)
        }
        else {
            uri?.let { it ->
                val file = Utils.getFile(context, it)
                createContribWithImage(action,file)
            } ?: run { createContrib(action) }
        }
    }

    fun createDemand(action: Action) {
        EntourageApplication.get().apiModule.actionsRequest.createActionDemand(DemandWrapper(action))
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

    fun createContrib(action: Action) {
        EntourageApplication.get().apiModule.actionsRequest.createActionContrib(ContribWrapper(action))
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

    fun updateAction(actionId: Int, actionEdited: ArrayMap<String, Any>) {
        //TODO a faire
    }

    /*
        Cancel
     */

    fun cancelAction(id: Int, isDemand:Boolean) {
        if (!isDemand) {
            cancelContribution(id)
        }
        else {
            cancelDemand(id)
        }
    }

    private fun cancelDemand(id: Int) {
        //TODO a faire
    }

    private fun cancelContribution(id: Int) {
        //TODO a faire
    }
}