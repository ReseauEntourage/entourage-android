package social.entourage.android.api.model.notification

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames

/**
 * Created by mihaiionescu on 18/03/16.
 */
@KSerializable
class PushNotificationContent : Serializable {
    var extra: Extra? = null
    var message: String? = null

    val type: String
        get() = extra?.type ?: ""

    val userId: Int
        get() = extra?.userId ?: 0

    val joinableId: Long
        get() = extra?.joinableId ?: 0

    val joinableUUID: String
        get() = extra?.joinableId?.toString() ?: ""

    val isEntourageRelated: Boolean
        get() = Extra.JOINABLE_TYPE_ENTOURAGE == extra?.joinableType

    /*val feedItemName: String
        get() {
            if (message == null) return ""
            val index = message!!.lastIndexOf(':')
            return if (index == -1 || index >= message!!.length - 2) "" else message!!.substring(index + 2)
        }*/

    /**
     * Returns the tag used by the notification (it can be null)
     * @return the tag
     */
    val notificationTag: String?
        get() {
            if (TYPE_NEW_JOIN_REQUEST == type || TYPE_NEW_CHAT_MESSAGE == type) {
                if (isEntourageRelated) {
                    return TAG_ENTOURAGE + joinableId
                }
            }
            return null
        }

    @KSerializable
    class Extra : Serializable {
        @SerialName("joinable_id")
        @JsonNames("feed_id")
        var joinableId: Long = 0

        @SerialName("joinable_type")
        @JsonNames("feed_type")
        var joinableType: String? = null

        @SerialName("user_id")
        var userId = 0

        @SerialName("entourage_id")
        var entourageId: Long = 0

        @SerialName("inviter_id")
        var inviterId = 0

        @SerialName("invitee_id")
        var inviteeId = 0

        @SerialName("popup")
        var popup:String? = null

        @SerialName("invitation_id")
        var invitationId = 0
        var type: String? = null

        @SerialName("instance")
        var instance:String? = null

        @SerialName("instance_id")
        var instanceId:Int? = null

        @SerialName("post_id")
        var postId:Int? = null

        @SerialName("chat_message_id")
        var chatMessageId:Int? = null

        @SerialName("welcome")
        val welcome: Boolean? = false
        //value = h1, j2, j8, j11

        @SerialName("stage")
        val stage: String? = null

        @SerialName("badge")
        val badge: String? = null

        @SerialName("tracking")
        val tracking: String? = null

        @SerialName("url")
        val url: String? = null

        companion object {
            private const val serialVersionUID = 9200479161789347105L
            const val JOINABLE_TYPE_ENTOURAGE = "Entourage"
        }
    }

    companion object {
        private const val serialVersionUID = -8538280256790931663L
        const val TYPE_NEW_CHAT_MESSAGE = "NEW_CHAT_MESSAGE"
        const val TYPE_JOIN_REQUEST_ACCEPTED = "JOIN_REQUEST_ACCEPTED"
        const val TYPE_JOIN_REQUEST_CANCELED = "JOIN_REQUEST_CANCELED"
        const val TYPE_NEW_JOIN_REQUEST = "NEW_JOIN_REQUEST"
        const val TYPE_ENTOURAGE_INVITATION = "ENTOURAGE_INVITATION"
        const val TYPE_INVITATION_STATUS = "INVITATION_STATUS"
        private const val TAG_ENTOURAGE = "entourage-"
    }
}