package social.entourage.android.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.feed.FeedItem
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

    override fun getTimestamp(): Date? {
        return requestDate
    }

    override fun hashString(): String {
        return HASH_STRING_HEAD + userId
    }

    override fun getType(): Int {
        return if (isDisplayedAsMember) FEED_MEMBER_CARD else TOUR_USER_JOIN
    }

    override fun getId(): Long {
        return userId.toLong()
    }

    override fun equals(o: Any?): Boolean {
        return if (o == null || o.javaClass != this.javaClass) false else userId == (o as EntourageUser).userId
        //return (this.userId == ((TourUser)o).userId) && (this.status.equals(((TourUser)o).status));
    }

    fun clone(): EntourageUser {
        val clone = EntourageUser()
        clone.userId = userId
        clone.feedItem = feedItem
        clone.displayName = displayName
        clone.email = email
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

    companion object {
        private const val serialVersionUID = 6896801812363434601L
        private const val HASH_STRING_HEAD = "TourUser-"
    }
}