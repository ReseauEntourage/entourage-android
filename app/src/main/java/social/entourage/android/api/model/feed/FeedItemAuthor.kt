package social.entourage.android.api.model.feed

import social.entourage.android.api.model.Partner
import java.io.Serializable
import java.util.ArrayList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class FeedItemAuthor (
    @SerialName("avatar_url") var avatarURLAsString: String? = null,
    @SerialName("id") var userID: Int = 0,
    @SerialName("display_name") var userName: String? = null,
    @SerialName("community_roles") var communityRoles: ArrayList<String>? = null,
    @SerialName("partner") var partner: Partner? = null,
    @SerialName("is_birthday") var isBirthday: Boolean = false) : Serializable {

    fun isSame(author: FeedItemAuthor?): Boolean {
        if (author == null) return false
        if (userID != author.userID) return false
        if (avatarURLAsString != null) {
            if (avatarURLAsString != author.avatarURLAsString) return false
        } else {
            if (author.avatarURLAsString != null) return false
        }
        return partner?.isSame(author.partner) ?: (author.partner == null)
    }

    companion object {
        private const val serialVersionUID = 3412733374231780458L
    }

}