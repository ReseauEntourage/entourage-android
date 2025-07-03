package social.entourage.android.api.model

import android.content.Context
import androidx.collection.ArrayMap
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.R.string
import social.entourage.android.api.model.feed.FeedItemAuthor
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Date

class User : Serializable {
    // ----------------------------------
    // ATTRIBUTES (Serialized)
    // ----------------------------------
    var id: Int
    var email: String? = null

    @SerializedName("first_name")
    var firstName: String? = null

    @SerializedName("last_name")
    var lastName: String? = null

    @SerializedName("birthday")
    var birthday: String? = null
    @SerializedName("gender")
    var gender: String? = null

    @SerializedName("interests")
    var interests: ArrayList<String> = ArrayList()

    @SerializedName("involvements")
    var involvements: ArrayList<String> = ArrayList()

    @SerializedName("concerns")
    var concerns: ArrayList<String> = ArrayList()

    @SerializedName("display_name")
    val displayName: String?
    var partner: Partner?

    @SerializedName("avatar_url")
    var avatarURL: String?

    @SerializedName("availability")
    var availability: ArrayMap<String, List<String>> = ArrayMap()

    @SerializedName("firebase_properties")
    val firebaseProperties: UserFirebaseProperties? = null
    var about: String? = ""
        get() = field ?: ""
    val roles: ArrayList<String>? = null
    val conversation: UserConversation? = null
    var address: Address? = null

    @SerializedName("address_2")
    var addressSecondary: Address? = null
    var goal: String? = null
    var stats: Stats? = null
    var organization: Organization? = null

    @SerializedName("engaged")
    var isEngaged: Boolean = false

    @SerializedName("unread_count")
    var unreadCount: Int? = null

    @SerializedName("permissions")
    private var permissions: UserPermissions? = null

    @SerializedName("travel_distance")
    var travelDistance: Int? = null

    @SerializedName("willing_to_engage_locally")
    var willingToEngageLocally: Boolean? = null

    @SerializedName("created_at")
    var createdAt: Date? = null

    // ----------------------------------
    // ATTRIBUTES (Not Serialized)
    // ----------------------------------
    @Expose(serialize = false)
    var phone: String? = null

    @Expose(serialize = false)
    var smsCode: String? = null

    @Expose(serialize = false)
    val token: String?

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------
    constructor() {
        id = 0
        email = ""
        displayName = ""
        about = ""
        stats = null
        organization = null
        partner = null
        token = null
        avatarURL = null
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
        this.displayName = displayName
        this.stats = stats
        this.organization = organization
        about = ""
        partner = null
        this.token = token
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
        return permissions?.isEventCreationActive() ?: false
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
    class UserConversation : Serializable {
        val uuid: String? = null

        companion object {
            private const val serialVersionUID: Long = -35162825747933L
        }
    }

    // ----------------------------------
    // User Firebase Properties
    // ----------------------------------
    class UserFirebaseProperties : HashMap<String, String>(), Serializable {
        companion object {
            private const val serialVersionUID: Long = -90000095L
        }
    }

    // ----------------------------------
    // User Address
    // ----------------------------------
    class Address : Serializable {
        var latitude = 0.0
        var longitude = 0.0

        @SerializedName("display_address")
        var displayAddress: String = ""

        @SerializedName("google_place_id")
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