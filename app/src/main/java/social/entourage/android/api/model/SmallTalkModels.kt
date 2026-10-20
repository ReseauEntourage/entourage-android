@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import java.io.Serializable
import java.util.Date
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

/**
 * SmallTalk represents a chat room returned by the Entourage “smalltalks” endpoints.
 * Includes all matching‑related flags and nested members.
 */
@KSerializable
data class SmallTalk(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("uuid_v2")
    val uuid: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("subname")
    val subname: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("meeting_url")
    val meetingUrl: String? = null,

    @SerialName("members_count")
    val membersCount: Int? = null,

    @SerialName("last_message")
    val lastMessage: LastMessage? = null,

    @SerialName("number_of_unread_messages")
    val numberOfUnreadMessages: Int? = null,

    @SerialName("has_personal_post")
    val hasPersonalPost: Boolean? = null,

    @SerialName("members")
    val members: ArrayList<GroupMember>? = null,



    @SerialName("match_format")
    val matchFormat: String? = null, // "one" or "many"

    @SerialName("match_locality")
    val matchLocality: Boolean? = null,

    @SerialName("match_gender")
    val matchGender: Boolean? = null,

    @SerialName("created_at")
    val createdAt: Date? = null,

    @SerialName("updated_at")
    val updatedAt: Date? = null
) : Serializable {
    companion object { private const val serialVersionUID = 42L }
}

/**
 * Wrapper for a **single** participant returned by some endpoints (rarely used).
 */
@KSerializable
data class MemberWrapper(
    @SerialName("user") val user: User
) : Serializable {
    companion object { private const val serialVersionUID = 43L }
}

/**
 * Wrapper for the list of participants of a SmallTalk.
 * The API serialises participants under a top‑level key `users`.
 */
@KSerializable
data class MembersWrapper(
    @SerialName("users") val users: MutableList<User> = mutableListOf()
) : Serializable {
    companion object { private const val serialVersionUID = 44L }
}

/**
 * Represents a user’s request to join or create a SmallTalk,
 * now also includes full smalltalk and user objects.
 */
@KSerializable
data class UserSmallTalkRequest(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("uuid_v2")
    val uuid: String? = null,

    @SerialName("smalltalk_id")
    val smalltalkId: Int? = null,

    @SerialName("number_of_unread_messages")
    val numberOfUnreadMessages: Int? = null,

    @SerialName("match_format")
    val matchFormat: String? = null, // "one" ou "many"

    @SerialName("match_locality")
    val matchLocality: Boolean? = null,

    @SerialName("match_gender")
    val matchGender: Boolean? = null,

    @SerialName("user_gender")
    val userGender: String? = null,


    @SerialName("created_at")
    val createdAt: Date? = null,

    @SerialName("matched_at")
    val matchedAt: Date? = null,

    @SerialName("updated_at")
    val updatedAt: Date? = null,

    @SerialName("deleted_at")
    val deletedAt: Date? = null,

    @SerialName("last_match_computation_at")
    val lastMatchComputationAt: Date? = null,

    @SerialName("user")
    val user: User? = null,

    @SerialName("smalltalk")
    val smallTalk: SmallTalk? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 46L
    }
}

@KSerializable
data class UserSmallTalkRequestWithMatchData(
    @SerialName("user_smalltalk_id") val userSmallTalkId: Int = 0,
    @SerialName("smalltalk_id") val smallTalkId: Int? = null,
    @SerialName("users") val users: List<User> = emptyList(),
    @SerialName("has_matched_format") val hasMatchedFormat: Boolean = false,
    @SerialName("has_matched_gender") val hasMatchedGender: Boolean = false,
    @SerialName("has_matched_locality") val hasMatchedLocality: Boolean = false,
    @SerialName("has_matched_interest") val hasMatchedInterest: Boolean = false,
    @SerialName("has_matched_profile") val hasMatchedProfile: Boolean = false,
    @SerialName("unmatch_count") val unmatchCount: Int = 0
)

@KSerializable
data class UserSmallTalkRequestWithMatchDataWrapper(
    @SerialName("user_smalltalks") val requests: List<UserSmallTalkRequestWithMatchData> = emptyList()
)
