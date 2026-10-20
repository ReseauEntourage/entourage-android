@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.utils.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

/**
 * Created by - on 15/11/2022.
 */

@KSerializable
class Conversation(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("uuid_v2")
    val uuid_v2: String? = null,
    @SerialName("uuid")
    val uuid: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("name")
    var title: String? = null,
    @SerialName("image_url")
    var imageUrl: String? = null,
    var subname: String? = null,
    @SerialName("last_message")
    var lastMessage: LastMessage? = null,
    @SerialName("number_of_unread_messages")
    var numberUnreadMessages: Int? = 0,

    @SerialName("section")
    val section: String? = null,

    @SerialName("user")
    val user: MemberConversation? = null,
    @SerialName("chat_messages")
    val message: ArrayList<ChatMessage>? = null,
    @SerialName("has_personal_post")
    val hasPersonalPost: Boolean? = null,
    @SerialName("members")
    val members: ArrayList<GroupMember>? = null,
    @SerialName("member")
    val member: Boolean? = null,
    @SerialName("members_count")
    val memberCount: Int = 0,
    @SerialName("creator")
    val isCreator: Boolean? = null,
    @SerialName("blockers")
    var blockers: ArrayList<String>? = null,
    @SerialName("author")
    var author: ConversationAuthor? = null
) {
    override fun toString(): String {
        return "Conversation(id=$id, user=$user)"
    }

    fun dateFormattedString(context: android.content.Context) : String {
        lastMessage?.date?.let {
            return Utils.formatLastUpdateDate(it, context)
        } ?: return  "-"
    }

    fun getLastMessage(context: android.content.Context): String? {
        if (!lastMessage?.imageUrl.isNullOrEmpty()) {
            // Use the context passed in to fetch the string
            return "📷 " + context.getString(R.string.button_event_add_photo)
        }
        return lastMessage?.text
    }

    fun isLastMessageToday(): Boolean {
        val d = lastMessage?.date ?: return false // pas de date => considère "pas aujourd'hui"
        val calMsg = java.util.Calendar.getInstance().apply { time = d }
        val calNow = java.util.Calendar.getInstance()
        return calMsg.get(java.util.Calendar.YEAR) == calNow.get(java.util.Calendar.YEAR) &&
                calMsg.get(java.util.Calendar.DAY_OF_YEAR) == calNow.get(java.util.Calendar.DAY_OF_YEAR)
    }

    fun getRolesWithPartnerFormated() : String? {
        user?.roles?.let { roles ->
            var roleStr = ""
            for (role in roles) {
                if (roleStr.isNotEmpty()) {
                    roleStr = "$roleStr • $role"
                }
                else {
                    roleStr = role
                }
                break
            }

            user.partner?.name?.let {
                if (roleStr.isNotEmpty()) {
                    roleStr = "$roleStr • $it"
                }
                else {
                    roleStr = it
                }
            }
        return roleStr
        } ?:  run { return null }
    }

    fun hasUnread() : Boolean {
        return (numberUnreadMessages ?: 0) > 0
    }

    /** Copie de la conversation avec le compteur de non-lus remis à 0 (mise à jour optimiste de la liste). */
    fun copyAsRead(): Conversation = Conversation(
        id = id, uuid_v2 = uuid_v2, uuid = uuid, type = type, title = title, imageUrl = imageUrl,
        subname = subname, lastMessage = lastMessage, numberUnreadMessages = 0, section = section,
        user = user, message = message, hasPersonalPost = hasPersonalPost, members = members,
        member = member, memberCount = memberCount, isCreator = isCreator, blockers = blockers,
        author = author
    )

    fun isOneToOne() : Boolean {
        return type == "private"
    }
    fun hasToShowFirstMessage() : Boolean {
        return ((numberUnreadMessages ?: 0) > 0) && (!(hasPersonalPost ?: true))
    }

    fun getPictoTypeFromSection() : Int {
        return when (section) {
            ActionSection.social -> R.drawable.ic_action_section_social
            ActionSection.clothes -> R.drawable.ic_action_section_clothes
            ActionSection.equipment -> R.drawable.ic_action_section_equipment
            ActionSection.hygiene -> R.drawable.ic_action_section_hygiene
            ActionSection.services -> R.drawable.ic_action_section_services
            else -> R.drawable.ic_entourage_category_more
        }
    }

    fun imBlocker() : Boolean {
        val _return = blockers?.contains( "me" )

        return _return ?: false
    }

    fun isTheBlocker() : Boolean {
        val _return = blockers?.contains("participant")

        return _return ?: false
    }

    fun hasBlocker() : Boolean {
        return (blockers?.size ?: 0) > 0
    }
}

@KSerializable
class LastMessage (
    @SerialName("text")
    var text:String? = null,
    @SerialName("date")
    var date: Date? = null,
    var imageUrl: String? = null
){}

@KSerializable
class MemberConversation (
    @SerialName("id")
    var id: Int = 0,
    @SerialName("display_name")
    var displayName: String? = null,
    @SerialName("avatar_url")
    var imageUrl: String? = null,
    @SerialName("partner")
    var partner: Partner? = null,
    @SerialName("partner_role_title")
    var partnerRoleTitle: String? = null,
    @SerialName("roles")
    var roles: ArrayList<String>? = null,
    @SerialName("is_birthday")
    var isBirthday: Boolean = false
){}

//Block user
@KSerializable
class UserBlockedUser (
    @SerialName("blocked_user")
    var blockedUser: BlockUserDetails,
    @SerialName("user")
    var user: BlockUserDetails){
    var isChecked = false

    fun imBlocker() : Boolean {
        return user.id == EntourageApplication.get().me()?.id
    }
}

@KSerializable
class BlockUserDetails (
    @SerialName("id")
    var id:Int = 0,
    @SerialName("display_name")
    var displayName:String? = null,
    @SerialName("avatar_url")
    var avatarUrl:String? = null
)

@KSerializable
class ConversationAuthor (
    @SerialName("id")
    var id:Int = 0,
    @SerialName("display_name")
    var username:String? = null
)

@KSerializable
data class ConversationMembership(
    @SerialName("status") val status: String? = null,
    @SerialName("joinable_status") val joinableStatus: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("subname") val subname: String? = null,
    @SerialName("joinable_type") val joinableType: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("joinable_id") val joinableId: Int? = null,
    @SerialName("number_of_people") val numberOfPeople: Int? = null,
    @SerialName("number_of_root_chat_messages") val numberOfRootMessages: Int? = null,
    @SerialName("number_of_unread_messages") val numberOfUnreadMessages: Int? = null,
    @SerialName("last_chat_message") val lastChatMessageText: String? = null,
    @SerialName("last_chat_message_image_url") val lastChatMessageImageUrl: String? = null,
    @SerialName("last_chat_message_datetime") val lastChatMessageDate: String? = null
){
    fun getParsedDate(): Date? {
        subname?.let { dateString ->
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                inputFormat.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    fun createdDateString(): String {
        subname?.let { dateString ->
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                val parsedDate = inputFormat.parse(dateString)

                val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                parsedDate?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                ""
            }
        } ?: return ""
    }

}


@KSerializable
data class ConversationMembershipsWrapper(
    @SerialName("memberships") val memberships: List<ConversationMembership> = emptyList()
)


@KSerializable
data class ConversationImage(
    @SerialName("chat_message_id") val chatMessageId: Int? = null,
    @SerialName("url") val url: String? = null
)

@KSerializable
data class ConversationImagesWrapper(
    @SerialName("images") val images: List<ConversationImage> = emptyList()
)

@KSerializable
data class ConversationImageSingleWrapper(
    @SerialName("image") val image: ConversationImage? = null
)