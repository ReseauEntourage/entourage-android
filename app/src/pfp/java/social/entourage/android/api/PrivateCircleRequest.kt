package social.entourage.android.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.VisitChatMessage.VisitChatMessageWrapper

/**
 * Created by Mihai Ionescu on 06/06/2018.
 */
interface PrivateCircleRequest {
    @POST("entourages/{entourage_id}/chat_messages.json")
    fun visitMessage(
            @Path("entourage_id") entourageId: Long,
            @Body message: VisitChatMessageWrapper?
    ): Call<ChatMessageWrapper?>?
}