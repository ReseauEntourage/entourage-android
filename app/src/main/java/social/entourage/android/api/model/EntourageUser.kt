package social.entourage.android.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.feed.FeedItem
import timber.log.Timber
import java.io.Serializable
import java.util.*

/**
 * Created by mihaiionescu on 24/02/16.
 */
class EntourageUser : TimestampedObject(), Serializable {
    @SerializedName("id")
    var userId = 0

    @SerializedName("display_name")
    var displayName: String? = null

    @SerializedName("requested_at")
    private var requestDate: Date? = null
    var message: String? = null

    @SerializedName("confirmed_at")
    var confirmedAt : Boolean? = null

    @SerializedName("avatar_url")
    var avatarURLAsString: String? = null

    @SerializedName("partner")
    var partner: Partner? = null
        private set

    @SerializedName("group_role")
    var groupRole: String? = null
        private set

    @SerializedName("community_roles")
    var communityRoles: List<String>? = null
        private set

    var isDisplayedAsMember = false

    @Expose(serialize = false, deserialize = true)
    private var email: String? = null

    @Expose(serialize = false, deserialize = true)
    var status: String? = null

    @Expose(serialize = false, deserialize = false)
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
        communityRoles?.let {
            var roleStr = ""

            if (isAdmin()) {
                roleStr = "Admin"
            }

            for (role in it) {
                if(roleStr.isNotEmpty()) {
                    roleStr = "$roleStr • $role"
                }
                else {
                    roleStr = role
                }

                break
            }
            partner?.name?.let {
                if (roleStr.isNotEmpty()) {
                    roleStr = "$roleStr • $it"
                }
                else {
                    roleStr = it
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
        if (groupRole == "ambassador") {
            return true
        }
        return false
    }

    companion object {
        private const val serialVersionUID = 6896833312363434601L
        private const val HASH_STRING_HEAD = "EntourageUser-"
    }
}