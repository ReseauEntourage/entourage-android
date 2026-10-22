@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import android.content.Context
import androidx.collection.ArrayMap
import social.entourage.android.R.string
import social.entourage.android.api.model.feed.FeedItemAuthor
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Date
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

@KSerializable
class User : Serializable {
    // ----------------------------------
    // ATTRIBUTES (Serialized)
    // ----------------------------------
    var id: Int = 0
    var email: String? = null

    @SerialName("first_name")
    var firstName: String? = null

    @SerialName("last_name")
    var lastName: String? = null

    @SerialName("birthdate")
    var birthday: String? = null
    @SerialName("gender")
    var gender: String? = null

    @SerialName("interests")
    var interests: ArrayList<String> = ArrayList()

    @SerialName("involvements")
    var involvements: ArrayList<String> = ArrayList()

    @SerialName("orientations")
    var orientations: ArrayList<String> = ArrayList()

    @SerialName("concerns")
    var concerns: ArrayList<String> = ArrayList()

    @SerialName("display_name")
    val displayName: String? = ""
    var partner: Partner? = null

    @SerialName("avatar_url")
    var avatarURL: String? = null

    @SerialName("availability")
    var availability: Map<String, List<String>> = emptyMap()

    @SerialName("firebase_properties")
    val firebaseProperties: Map<String, String>? = null
    var about: String? = ""
        get() = field ?: ""
    val roles: ArrayList<String>? = null
    val conversation: UserConversation? = null
    var address: Address? = null

    @SerialName("address_2")
    var addressSecondary: Address? = null
    var goal: String? = null
    var stats: Stats? = null
    var organization: Organization? = null

    @SerialName("engaged")
    var isEngaged: Boolean = false

    @SerialName("unread_count")
    var unreadCount: Int? = null

    @SerialName("permissions")
    private var permissions: Map<String, Map<String, Boolean>>? = null

    @SerialName("travel_distance")
    var travelDistance: Int? = null

    @SerialName("willing_to_engage_locally")
    var willingToEngageLocally: Boolean? = null

    @SerialName("created_at")
    var createdAt: Date? = null

    @SerialName("is_birthday")
    var isBirthday: Boolean = false

    @SerialName("badges")
    var badges: MutableList<social.entourage.android.badges.ApiBadge>? = null

    // ----------------------------------
    // ATTRIBUTES (Not Serialized)
    // ----------------------------------
    var phone: String? = null

    var smsCode: String? = null

    val token: String? = null

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------
    constructor() {
        email = ""
        about = ""
        stats = null
        organization = null
    }

    private constructor(
        id: Int,
        email: String,
        displayName: String,
        stats: Stats,
        organization: Organization,
        token: String,
        avatarURL: String?
    ) {
        this.id = id
        this.email = email
        this.stats = stats
        this.organization = organization
        about = ""
        this.avatarURL = avatarURL
    }

    fun clone(): User {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(this)
        val bais = ByteArrayInputStream(baos.toByteArray())
        val ois = ObjectInputStream(bais)
        return ois.readObject() as User
    }

    // ----------------------------------
    // Other methods
    // ----------------------------------
    fun isCreateEventActive(): Boolean {
        return permissions?.get("outing")?.get("creation") ?: false
    }

    fun asAuthor(): FeedItemAuthor {
        return FeedItemAuthor(avatarURL, id, displayName,roles, partner)
    }

    val arrayMapForUpdate: ArrayMap<String, Any>
        get() {
            val userMap = ArrayMap<String, Any>()
            userMap["first_name"] = firstName
            userMap["last_name"] = lastName
            if (email != null) {
                userMap["email"] = email
            }
            if (smsCode != null) {
                userMap["sms_code"] = smsCode
            }
            userMap["about"] = about
            return userMap
        }

    fun hasSignedEthicsCharter(): Boolean {
        return roles?.contains(USER_ROLE_ETHICS_CHARTER_SIGNED) == true
    }



    fun getFormattedInterests(context: Context): String {
        val interestsString = StringBuilder()
        var isFirst = true
        interests.forEach { interest ->
            if (isFirst) {
                isFirst = false
            } else {
                interestsString.append(", ")
            }
            val stringId = getStringId(interest)
            interestsString.append(
                context.getString(
                    when (stringId) {
                        string.rencontrer_sdf,
                        string.event_sdf,
                        string.questions_sdf,
                        string.aide_sdf,
                        string.m_orienter_sdf,
                        string.trouver_asso_sdf,
                        string.m_informer_riverain,
                        string.event_riverain,
                        string.entourer_riverain,
                        string.dons_riverain,
                        string.benevolat_riverain,
                        string.aide_pers_asso,
                        string.cult_sport_asso,
                        string.serv_pub_asso,
                        string.autre_asso -> stringId
                        else -> if (stringId == -1) string.empty_description else stringId
                    }
                )
            )
        }
        return interestsString.toString()
    }

    val isUserTypeAlone: Boolean
        get() = USER_GOAL_ALONE.equals(goal, ignoreCase = true)

    val isUserTypeNeighbour: Boolean
        get() = USER_GOAL_NEIGHBOUR.equals(goal, ignoreCase = true)

    val isUserTypeAsso: Boolean
        get() = USER_GOAL_ASSO.equals(goal, ignoreCase = true)

    // ----------------------------------
    // User Conversation
    // ----------------------------------
    @KSerializable
    class UserConversation : Serializable {
        val uuid: String? = null

        companion object {
            private const val serialVersionUID: Long = -35162825747933L
        }
    }

    // ----------------------------------
    // User Address
    // ----------------------------------
    @KSerializable
    class Address : Serializable {
        var latitude = 0.0
        var longitude = 0.0

        @SerialName("display_address")
        var displayAddress: String = ""

        @SerialName("google_place_id")
        var googlePlaceId: String? = null

        constructor(googlePlaceId: String?) {
            this.googlePlaceId = googlePlaceId
        }

        constructor(latitude: Double, longitude: Double, displayAddress: String?) {
            this.latitude = latitude
            this.longitude = longitude
            this.displayAddress = displayAddress ?: ""
        }

        companion object {
            private const val serialVersionUID: Long = -90018145L
        }
    }

    companion object {
        private const val serialVersionUID: Long = -90000034L

        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val KEY_USER_ID = "social.entourage.android.KEY_USER_ID"

        //const val KEY_USER = "social.entourage.android.KEY_USER"
        //const val TYPE_PUBLIC = "public"
        private const val USER_ROLE_ETHICS_CHARTER_SIGNED = "ethics_charter_signed"
        const val USER_GOAL_NEIGHBOUR = "offer_help"
        const val USER_GOAL_ALONE = "ask_for_help"
        const val USER_GOAL_ASSO = "organization"
        const val USER_GOAL_BOTH = "ask_and_offer_help"
        const val USER_GOAL_NONE = ""

        private fun getStringId(resourceName: String): Int {
            return try {
                val stringId = string::class.java.getDeclaredField(resourceName)
                stringId.getInt(stringId)
            } catch (e: Exception) {
                Timber.e(e, "Resource not found : $resourceName")
                -1
            }
        }
        fun fromGroupMember(member: GroupMember): User {
            val user = User()
            user.id = if (member.id != null) member.id!! else 0
            user.avatarURL = member.avatarUrl
            user.email = "" // facultatif
            user.about = ""
            try {
                val displayNameField = User::class.java.getDeclaredField("displayName")
                displayNameField.isAccessible = true
                displayNameField[user] = member.displayName
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return user
        }
    }
}

fun User.toGroupMember(): GroupMember {
    return GroupMember(
        id = this.id,
        displayName = this.displayName,
        avatarUrl = this.avatarURL
    )
}

fun EntourageUser.toGroupMember(): GroupMember {
    return GroupMember(
        id = this.id.toInt(),
        displayName = this.displayName,
        avatarUrl = this.avatarURLAsString
    )
}

fun List<User>.toGroupMembers(): List<GroupMember> {
    return map { it.toGroupMember() }
}