package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by mihaiionescu on 18/03/16.
 */
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

    val isTourRelated: Boolean
        get() = Extra.JOINABLE_TYPE_TOUR == extra?.joinableType

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
                if (isTourRelated) {
                    return TAG_TOUR + joinableId
                } else if (isEntourageRelated) {
                    return TAG_ENTOURAGE + joinableId
                }
            }
            return null
        }

    class Extra : Serializable {
        @SerializedName(value = "joinable_id", alternate = ["feed_id"])
        var joinableId: Long = 0

        @SerializedName(value = "joinable_type", alternate = ["feed_type"])
        var joinableType: String? = null

        @SerializedName("user_id")
        var userId = 0

        @SerializedName("entourage_id")
        var entourageId: Long = 0

        @SerializedName("inviter_id")
        var inviterId = 0

        @SerializedName("invitee_id")
        var inviteeId = 0

        @SerializedName("invitation_id")
        var invitationId = 0
        var type: String? = null

        companion object {
            private const val serialVersionUID = 9200479161789347105L
            const val JOINABLE_TYPE_TOUR = "Tour"
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
        private const val TAG_TOUR = "tour-"
        private const val TAG_ENTOURAGE = "entourage-"
    }
}