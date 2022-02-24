package social.entourage.android.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Query
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.ConversationsRequest
import social.entourage.android.api.request.ConversationsResponse
import social.entourage.android.api.request.MessagesMetadatasResponse

/**
 * Created by Jerome on 22/12/2021.
 */
class ConversationsAPI(val application: EntourageApplication) {

    private val conversationsRequest : ConversationsRequest
        get() = application.components.conversationsRequest

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

    fun getMessagesMetadatas(listener:(conversations: MessagesMetadatasResponse?, error:String?) -> Unit) {
        val call: Call<MessagesMetadatasResponse> = conversationsRequest.retrieveMessagesMetadatas()
        call.enqueue(object : Callback<MessagesMetadatasResponse> {
            override fun onResponse(call: Call<MessagesMetadatasResponse>, response: Response<MessagesMetadatasResponse>) {
                if (response.isSuccessful) {
                    listener(response.body(),null)
                } else {
                    val error = ApiError.fromResponse(response)
                    listener(null,error.code)
                }
            }

            override fun onFailure(call: Call<MessagesMetadatasResponse>, t: Throwable) {
                listener(null,null)
            }
        })
    }
}