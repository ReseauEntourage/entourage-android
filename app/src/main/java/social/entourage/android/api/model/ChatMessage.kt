package social.entourage.android.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * Created by mihaiionescu on 25/02/16.
 */
class ChatMessage (var content: String) : TimestampedObject(), Serializable {
    @Expose(serialize = false)
    @SerializedName("id")
    var chatId: Long = 0

    @Expose(serialize = false)
    @SerializedName("created_at")
    var creationDate = Date()

    @Expose(serialize = false)
     private val user: User? = null

    @Expose(serialize = false, deserialize = false)
    var isMe = false

    @Expose(serialize = false)
    @SerializedName("message_type")
    val messageType: String? = null
    val metadata: Metadata? = null

    val userId: Int
        get() = user?.id ?: 0

    val userRole: ArrayList<String>?
        get() = user?.roles

    val userAvatarURL: String?
        get() = user?.avatarURL

    val userName: String
        get() = user?.displayName ?: ""

    val partnerLogoSmall: String?
        get() = user?.partner?.smallLogoUrl

    override val timestamp: Date
        get() = creationDate

    override fun hashString(): String {
        return HASH_STRING_HEAD + chatId
    }

    override fun equals(other: Any?): Boolean {
        return !(other == null || other.javaClass != this.javaClass) && chatId == (other as ChatMessage).chatId
    }

    override val type: Int
        get()  {
            return when {
                TYPE_OUTING.equals(messageType, ignoreCase = true) -> CHAT_MESSAGE_OUTING
                TYPE_STATUS_UPDATE.equals(messageType, ignoreCase = true) -> STATUS_UPDATE_CARD
                isMe -> CHAT_MESSAGE_ME
                else -> CHAT_MESSAGE_OTHER
            }
        }

    override val id: Long
        get() = chatId

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    class Metadata : Serializable {
        val uuid: String? = null
        val title: String? = null
        val operation: String? = null
        val type: String? = null

        @SerializedName("starts_at")
        val startsAt: Date? = null

        @SerializedName("display_address")
        val displayAddress: String? = null
        val status: String? = null

        @SerializedName("outcome_success")
        val isOutcomeSuccess = false

        companion object {
            private const val serialVersionUID = 5065262321819947605L
            const val OPERATION_CREATED = "created"
            const val OPERATION_UPDATED = "updated"
        }
    }

    companion object {
        private const val HASH_STRING_HEAD = "ChatMessage-"
        private const val serialVersionUID = 2171223108739523540L
        const val TYPE_TEXT = "text"
        const val TYPE_VISIT = "visit"
        const val TYPE_OUTING = "outing"
        const val TYPE_STATUS_UPDATE = "status_update"
    }

}