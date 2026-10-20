package social.entourage.android.api.request

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.BaseEntourage.EntourageJoinInfo
import social.entourage.android.api.model.EntourageReport.EntourageReportWrapper
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class EntourageUserResponse {
    var user: EntourageUser? = null
}

@KSerializable
class ChatMessageWrapper(@SerialName("chat_message") var chatMessage: Post)
@KSerializable
class ShareMessageWrapper(@SerialName("chat_message") var chatMessage: ShareMessage)

@KSerializable
class ChatMessageResponse {
    @SerialName("chat_message")
    var chatMessage: ChatMessage? = null
}

interface EntourageRequest {
    @POST("entourages/{entourage_id}/chat_messages.json")
    fun addSharingMessage(
        @Path("entourage_id") entourageUUID: String,
        @Body message: ShareMessageWrapper
    ): Call<ChatMessageResponse>
}
