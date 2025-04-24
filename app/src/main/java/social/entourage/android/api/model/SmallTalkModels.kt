package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

/**
 * SmallTalk represents a chat room returned by the Entourage “smalltalks” endpoints.
 * Only the matching‑related flags requested by the backend are modelled for now.
 */
data class SmallTalk(
    @SerializedName("id")                val id: Int? = null,
    @SerializedName("uuid_v2")           val uuid: String? = null,
    @SerializedName("match_format")      val matchFormat: String,   // "one" or "many"
    @SerializedName("match_locality")    val matchLocality: Boolean,
    @SerializedName("match_gender")      val matchGender: Boolean,
    @SerializedName("match_interest")    val matchInterest: Boolean,
    @SerializedName("created_at")        val createdAt: Date? = null,
    @SerializedName("updated_at")        val updatedAt: Date? = null
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

data class UserSmallTalkRequest(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("uuid_v2") val uuid: String? = null,
    @SerializedName("smalltalk_id") val smalltalkId: Int? = null,

    @SerializedName("match_format")
    val matchFormat: String, // "one" ou "many"

    @SerializedName("match_locality")
    val matchLocality: Boolean,

    @SerializedName("match_gender")
    val matchGender: Boolean,

    @SerializedName("match_interest")
    val matchInterest: Boolean,

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 46L
    }
}