package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by - on 15/11/2022.
 */

class Conversation(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("uuid_v2")
    val uuid_v2: String? = null,
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("name")
    var title: String? = null,
    @SerializedName("image_url")
    var imageUrl: String? = null,

    @SerializedName("last_message")
    var lastMessage: LastMessage? = null,
    @SerializedName("number_of_unread_messages")
    var numberUnreadMessages: Int? = 0,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("user")
    val user: MemberConversation? = null,
    @SerializedName("chat_messages")
    val message: ArrayList<ChatMessage>? = null,
    @SerializedName("has_personal_post")
    val hasPersonalPost: Boolean? = null,
    @SerializedName("members")
    val members: ArrayList<GroupMember>? = null,
    @SerializedName("member")
    val member: Boolean? = null,
    @SerializedName("members_count")
    val memberCount: Int = 0,
    @SerializedName("creator")
    val isCreator: Boolean? = null,
    @SerializedName("blockers")
    var blockers: ArrayList<String>? = null,
    @SerializedName("author")
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

    fun getLastMessage() : String? {
        return lastMessage?.text
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

class LastMessage (
    @SerializedName("text")
    var text:String? = null,
    @SerializedName("date")
    var date: Date? = null
){}

class MemberConversation (
    @SerializedName("id")
    var id: Int,
    @SerializedName("display_name")
    var displayName: String? = null,
    @SerializedName("avatar_url")
    var imageUrl: String? = null,
    @SerializedName("partner")
    var partner: Partner? = null,
    @SerializedName("partner_role_title")
    var partnerRoleTitle: String? = null,
    @SerializedName("roles")
    var roles: ArrayList<String>? = null
){}

//Block user
class UserBlockedUser (
    @SerializedName("blocked_user")
    var blockedUser: BlockUserDetails,
    @SerializedName("user")
    var user: BlockUserDetails,
){
    var isChecked = false

    fun imBlocker() : Boolean {
        return user.id == EntourageApplication.get().me()?.id
    }
}

class BlockUserDetails (
    @SerializedName("id")
    var id:Int,
    @SerializedName("display_name")
    var displayName:String?,
    @SerializedName("avatar_url")
    var avatarUrl:String?
)

class ConversationAuthor (
    @SerializedName("id")
    var id:Int,
    @SerializedName("display_name")
    var username:String?
)

data class ConversationMembership(
    @SerializedName("status") val status: String?,
    @SerializedName("joinable_status") val joinableStatus: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("subname") val subname: String?,
    @SerializedName("joinable_type") val joinableType: String?,
    @SerializedName("joinable_id") val joinableId: Int?,
    @SerializedName("number_of_people") val numberOfPeople: Int?,
    @SerializedName("number_of_root_chat_messages") val numberOfRootMessages: Int?,
    @SerializedName("number_of_unread_messages") val numberOfUnreadMessages: Int?,
    @SerializedName("last_chat_message") val lastChatMessageText: String? // ❗ Correction ici
){
    fun createdDateString(): String {
        subname?.let { dateString ->
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                val parsedDate = inputFormat.parse(dateString)

                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                parsedDate?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                ""
            }
        } ?: return ""
    }

}


data class ConversationMembershipsWrapper(
    @SerializedName("memberships") val memberships: List<ConversationMembership>
)