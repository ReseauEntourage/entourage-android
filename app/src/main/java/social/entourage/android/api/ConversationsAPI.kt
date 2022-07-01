package social.entourage.android.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Query
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.ConversationsRequest
import social.entourage.android.api.request.ConversationsResponse
import social.entourage.android.api.request.MessagesMetadataResponse

/**
 * Created by Jerome on 22/12/2021.
 */
class ConversationsAPI(val application: EntourageApplication) {

    private val conversationsRequest : ConversationsRequest
        get() = application.apiModule.conversationsRequest

    fun getMessagesOne2One(@Query("page") page: Int,
                           @Query("per") per: Int,
                           listener:(conversations: ConversationsResponse?, error:String?) -> Unit) {
        val call: Call<ConversationsResponse> = conversationsRequest.retrieveMessagesOne2One(page, per)
        call.enqueue(object : Callback<ConversationsResponse> {
            override fun onResponse(call: Call<ConversationsResponse>, response: Response<ConversationsResponse>) {
                if (response.isSuccessful) {
                    listener(response.body(),null)
                } else {
                    val error = ApiError.fromResponse(response)
                    listener(null,error.code)
                }
            }

            override fun onFailure(call: Call<ConversationsResponse>, t: Throwable) {
                listener(null,null)
            }
        })
    }

    fun getMessagesGroup(@Query("page") page: Int,
                         @Query("per") per: Int,
                         listener:(conversations: ConversationsResponse?, error:String?) -> Unit) {
        val call: Call<ConversationsResponse> = conversationsRequest.retrieveMessagesGroup(page, per)
        call.enqueue(object : Callback<ConversationsResponse> {
            override fun onResponse(call: Call<ConversationsResponse>, response: Response<ConversationsResponse>) {
                if (response.isSuccessful) {
                    listener(response.body(),null)
                } else {
                    val error = ApiError.fromResponse(response)
                    listener(null,error.code)
                }
            }

            override fun onFailure(call: Call<ConversationsResponse>, t: Throwable) {
                listener(null,null)
            }
        })
    }

    fun getMessagesMetadata(listener:(conversations: MessagesMetadataResponse?, error:String?) -> Unit) {
        val call: Call<MessagesMetadataResponse> = conversationsRequest.retrieveMessagesMetadata()
        call.enqueue(object : Callback<MessagesMetadataResponse> {
            override fun onResponse(call: Call<MessagesMetadataResponse>, response: Response<MessagesMetadataResponse>) {
                if (response.isSuccessful) {
                    listener(response.body(),null)
                } else {
                    val error = ApiError.fromResponse(response)
                    listener(null,error.code)
                }
            }

            override fun onFailure(call: Call<MessagesMetadataResponse>, t: Throwable) {
                listener(null,null)
            }
        })
    }
}