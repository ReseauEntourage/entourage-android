package social.entourage.android.api.model.feed

import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.Partner
import java.io.Serializable

class FeedItemAuthor (
        @field:SerializedName("avatar_url") var avatarURLAsString: String?,
        @field:SerializedName("id") var userID: Int,
        @field:SerializedName("display_name") var userName: String?,
        @field:SerializedName("partner") var partner: Partner?,
        @field:SerializedName("partner_with_current_user") var isPartnerWithCurrentUser:Boolean) : Serializable {

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