@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import social.entourage.android.api.model.feed.FeedItem
import java.io.Serializable
import java.util.Date
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

/**
 * Created by mihaiionescu on 24/02/16.
 */
@KSerializable
class EntourageUser : TimestampedObject(), Serializable {
    @SerialName("id")
    var userId = 0

    @SerialName("display_name")
    var displayName: String? = null

    @SerialName("requested_at")
    private var requestDate: Date? = null
    var message: String? = null

    @SerialName("confirmed_at")
    var confirmedAt : Boolean? = null

    @SerialName("participate_at")
    var participateAt : String? = null

    @SerialName("avatar_url")
    var avatarURLAsString: String? = null

    @SerialName("partner")
    var partner: Partner? = null
        private set

    @SerialName("group_role")
    var groupRole: String? = null
        private set

    @SerialName("photo_acceptance")
    var photoAcceptance: Boolean? = null
        private set

    @SerialName("community_roles")
    var communityRoles: List<String>? = null
        private set
    @SerialName("roles")
    var roles: List<String>? = null
        private set

    @SerialName("birthday")
    var birthday: Boolean = false

    @SerialName("birthday_today")
    var birthdayToday: Boolean = false


    @SerialName("birthdate")
    var birthdate: String? = null

    var isDisplayedAsMember = false

    private var email: String? = null

    var status: String? = null

    @Transient
    var feedItem: FeedItem? = null

    var partner_role_title:String? = null

    override val timestamp: Date?
    get() = requestDate

    override fun hashString(): String {
        return HASH_STRING_HEAD + userId
    }

    override val type: Int
        get() = if (isDisplayedAsMember) FEED_MEMBER_CARD else USER_JOIN

    override val id: Long
        get() = userId.toLong()

    override fun equals(other: Any?): Boolean {
        return if (other == null || other.javaClass != this.javaClass) false else userId == (other as EntourageUser).userId
    }

    fun clone(): EntourageUser {
        val clone = EntourageUser()
        clone.userId = userId
        clone.feedItem = feedItem
        clone.displayName = displayName
        clone.email = email
        clone.confirmedAt = confirmedAt
        clone.status = status
        clone.requestDate = requestDate
        clone.message = message
        clone.avatarURLAsString = avatarURLAsString
        clone.partner = partner
        clone.groupRole = groupRole
        clone.communityRoles = communityRoles
        clone.isDisplayedAsMember = isDisplayedAsMember
        return clone
    }

    fun getCommunityRoleWithPartnerFormated() : String? {
        communityRoles?.let { roles->
            var roleStr = ""

            if (isAdmin()) {
                roleStr = "Admin"
            }

            for (role in roles) {
                var displayRole = role
                if (role == "Ambassadeur" || role.equals("ambassador", ignoreCase = true)) {
                    displayRole = "Animateur Entourage"
                } else if (role == "Équipe Entourage") {
                    displayRole = "Équipe"
                } else if (role == "Association") {
                   displayRole = "Association"
                }

                if (displayRole.isNotEmpty()) {
                    if(roleStr.isNotEmpty()) {
                        roleStr = "$roleStr • $displayRole"
                    }
                    else {
                        roleStr = displayRole
                    }
                }

                break //TODO: why do we get only the first role ?
            }
            partner?.name?.let { partnerName->
                var finalPartnerName = partnerName
                if (roleStr.contains("Association", ignoreCase = true) && partnerName.startsWith("Association", ignoreCase = true)) {
                    finalPartnerName = partnerName.replaceFirst("Association", "", true).trim()
                }

                if (roleStr.isNotEmpty()) {
                    roleStr = "$roleStr • $finalPartnerName"
                }
                else {
                    roleStr = finalPartnerName
                }
            }
            return roleStr
        }


        return null
    }

    fun isAdmin() : Boolean {
        if (groupRole == "creator") {
            return true
        }
        return false
    }

    fun isAmbassador() : Boolean {
        if (communityRoles?.contains("Ambassadeur") == true ||communityRoles?.contains("ambassador") == true) {
            return true
        }
        return false
    }

    companion object {
        private const val serialVersionUID = 6896833312363434601L
        private const val HASH_STRING_HEAD = "EntourageUser-"
    }
}