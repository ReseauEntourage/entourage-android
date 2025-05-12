package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date
import java.util.*

/**
 * SmallTalk represents a chat room returned by the Entourage “smalltalks” endpoints.
 * Includes all matching‑related flags and nested members.
 */
data class SmallTalk(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("uuid_v2")
    val uuid: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("subname")
    val subname: String? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("members_count")
    val membersCount: Int? = null,

    @SerializedName("last_message")
    val lastMessage: LastMessage? = null,

    @SerializedName("number_of_unread_messages")
    val numberOfUnreadMessages: Int? = null,

    @SerializedName("has_personal_post")
    val hasPersonalPost: Boolean? = null,

    @SerializedName("members")
    val members: ArrayList<GroupMember>? = null,

    @SerializedName("match_format")
    val matchFormat: String? = null, // "one" or "many"

    @SerializedName("match_locality")
    val matchLocality: Boolean? = null,

    @SerializedName("match_gender")
    val matchGender: Boolean? = null,

    @SerializedName("match_interest")
    val matchInterest: Boolean? = null,

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null
) : Serializable {
    companion object { private const val serialVersionUID = 42L }
}

/**
 * Wrapper for a **single** participant returned by some endpoints (rarely used).
 */
data class MemberWrapper(
    @SerializedName("user") val user: User
) : Serializable {
    companion object { private const val serialVersionUID = 43L }
}

/**
 * Wrapper for the list of participants of a SmallTalk.
 * The API serialises participants under a top‑level key `users`.
 */
data class MembersWrapper(
    @SerializedName("users") val users: MutableList<User>
) : Serializable {
    companion object { private const val serialVersionUID = 44L }
}

/**
 * Represents a user’s request to join or create a SmallTalk,
 * now also includes full smalltalk and user objects.
 */
data class UserSmallTalkRequest(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("uuid_v2")
    val uuid: String? = null,

    @SerializedName("smalltalk_id")
    val smalltalkId: Int? = null,

    @SerializedName("match_format")
    val matchFormat: String? = null, // "one" ou "many"

    @SerializedName("match_locality")
    val matchLocality: Boolean? = null,

    @SerializedName("match_gender")
    val matchGender: Boolean? = null,

    @SerializedName("user_gender")
    val userGender: String? = null,

    @SerializedName("match_interest")
    val matchInterest: Boolean? = null,

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("matched_at")
    val matchedAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null,

    @SerializedName("deleted_at")
    val deletedAt: Date? = null,

    @SerializedName("last_match_computation_at")
    val lastMatchComputationAt: Date? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("smalltalk")
    val smallTalk: SmallTalk? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 46L
    }
}
