package social.entourage.android.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.ShareEntourageMessage
import social.entourage.android.api.model.ShareMessage
import social.entourage.android.api.model.SharePOIMessage
import social.entourage.android.api.request.*

/**
 * Created by Jr (MJ-DEVS) on 10/09/2020.
 */
class MessageSharingAPI(val application: EntourageApplication) {

    private val sharingRequest : SharingRequest
        get() = application.entourageComponent.sharingRequest

    private val entourageRequest : EntourageRequest
        get() = application.entourageComponent.entourageRequest

    fun getSharing(listener:(isOK:Boolean, sharingList: SharingResponse?, error:String?) -> Unit) {
        val call: Call<SharingResponse> = sharingRequest.retrieveSharing()
        call.enqueue(object : Callback<SharingResponse> {
            override fun onResponse(call: Call<SharingResponse>, response: Response<SharingResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body(),null)
                } else {
                    val error = ApiError.fromResponse(response)
                    listener(false,null,error.code)
                }
            }

            override fun onFailure(call: Call<SharingResponse>, t: Throwable) {
                listener(false,null,null)
            }
        })
    }

    fun postSharingMessage(destUuid:String, sharedUuid:String, isPoi:Boolean, listener:(isOK:Boolean) -> Unit) {
        val shareMessage: ShareMessage = if(isPoi) SharePOIMessage(sharedUuid) else ShareEntourageMessage(sharedUuid)

        entourageRequest.addSharingMessage(destUuid, ShareMessageWrapper(shareMessage))
                .enqueue(object : Callback<ChatMessageResponse> {
            override fun onResponse(call: Call<ChatMessageResponse>, response: Response<ChatMessageResponse>) {
                listener(response.isSuccessful)
                //TODO check if we want to display the chat message right now -> response.body().chatMessage
            }

            override fun onFailure(call: Call<ChatMessageResponse>, t: Throwable) {
                listener(false)
            }
        })
    }

    companion object {
        private var instance: MessageSharingAPI? = null

        @Synchronized
        fun getInstance(application: EntourageApplication): MessageSharingAPI {
            return instance ?: MessageSharingAPI(application).also { instance = it}
        }
    }
}

