package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.Partner
import social.entourage.android.tools.UtilsV7
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by - on 15/11/2022.
 */

class Conversation(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("name")
    var title: String? = null,

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
    @SerializedName("creator")
    val isCreator: Boolean? = null,
    @SerializedName("blockers")
    var blockers: ArrayList<String>? = null
) {
    override fun toString(): String {
        return "Conversation(id=$id, user=$user)"
    }

    fun dateFormattedString(context: android.content.Context) : String {
        lastMessage?.date?.let {
            return UtilsV7.formatLastUpdateDate(it, context)
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
class UserBlocked (
    @SerializedName("blocked_user")
    var blockedUser:BlockedUser,
    @SerializedName("user")
    var user:BlockedUser,
){
    var isChecked = false

    fun imBlocker() : Boolean {
        return user.id == EntourageApplication.get().me()?.id
    }
}
class BlockedUser (
    @SerializedName("id")
    var id:Int,
    @SerializedName("display_name")
    var displayName:String?,
    @SerializedName("avatar_url")
    var avatarUrl:String?
)
