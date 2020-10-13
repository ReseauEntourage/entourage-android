package social.entourage.android.api

import android.annotation.SuppressLint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.*
import java.io.Serializable

/**
 * Created by Jr (MJ-DEVS) on 10/09/2020.
 */
class EntourageMessageSharingAPI(val application: EntourageApplication) {

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

    fun postSharingEntourage(sharedUuid:String,uuid:String,isPoi:Boolean,listener:(isOK:Boolean) -> Unit) {
        val chatMessage = EntourageMessageSharing()
        chatMessage.metadata = EntourageMessageSharing.Metadata()
        chatMessage.metadata?.uuid = uuid

        if (isPoi) {
            chatMessage.metadata?.type = "poi"
        }

        val chatMessageWrapper = EntourageMessageSharingWrapper(chatMessage)
        val call = entourageRequest.addEntourageMessageSharing(sharedUuid, chatMessageWrapper)
        call.enqueue(object : Callback<ChatMessageResponse> {
            override fun onResponse(call: Call<ChatMessageResponse>, response: Response<ChatMessageResponse>) {
                response.body()?.chatMessage?.let {
                    if (response.isSuccessful) {
                        listener(true)
                        return
                    }
                }
                listener(false)
            }

            override fun onFailure(call: Call<ChatMessageResponse>, t: Throwable) {
                listener(false)
            }
        })
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: EntourageMessageSharingAPI? = null

        @Synchronized
        fun getInstance(application: EntourageApplication): EntourageMessageSharingAPI {
            return instance ?: EntourageMessageSharingAPI(application).also { instance = it}
        }
    }
}

class EntourageMessageSharing : Serializable {

    val message_type = "share" //Used for POST
    var metadata: Metadata? = null


    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    class Metadata : Serializable {
        var uuid: String? = null
        var type = "entourage"
    }
}