package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Mihai Ionescu on 06/06/2018.
 */
class VisitChatMessage(@field:SerializedName("message_type") var messageType: String, visitedAt: Date) {
    var metadata: VisitChatMetadata

    class VisitChatMetadata(@field:SerializedName("visited_at") var visitedAt: Date)

    class VisitChatMessageWrapper(@field:SerializedName("chat_message") var visitChatMessage: VisitChatMessage)

    companion object {
        const val TYPE_VISIT = "visit"
    }

    init {
        metadata = VisitChatMetadata(visitedAt)
    }
}