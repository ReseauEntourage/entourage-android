package social.entourage.android.api.model

import android.content.Context
import androidx.collection.ArrayMap
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.R.string
import social.entourage.android.api.model.UserMembership.UserMembershipList
import social.entourage.android.api.model.feed.FeedItemAuthor
import timber.log.Timber
import java.io.*
import kotlin.collections.ArrayList

class User : Serializable {
    // ----------------------------------
    // ATTRIBUTES (Serialized)
    // ----------------------------------
    @JvmField
    val id: Int
    var email: String? = null
    @SerializedName("first_name")
    var firstName: String? = null
    @SerializedName("last_name")
    var lastName: String? = null
    @SerializedName("display_name")
    val displayName: String?
    @JvmField
    var partner: Partner?
    @JvmField @SerializedName("avatar_url")
    var avatarURL: String?
    @SerializedName("user_type")
    private val type = TYPE_PRO
    @SerializedName("firebase_properties")
    val firebaseProperties: UserFirebaseProperties? = null
    var about: String = ""
    val roles: ArrayList<String>? = null
    private val memberships: ArrayList<UserMembershipList>? = null
    val conversation: UserConversation? = null
    @JvmField
    var address: Address? = null
    @SerializedName("address_2")
    var addressSecondary: Address? = null
    var goal:String? = null
    val interests: ArrayList<String> = ArrayList()
    var stats: Stats? = null
    var organization: Organization? = null

    // ----------------------------------
    // ATTRIBUTES (Not Serialized)
    // ----------------------------------
    @JvmField @Expose(serialize = false)
    var phone: String? = null
    @Expose(serialize = false)
    var smsCode: String? = null
    @JvmField @Expose(serialize = false)
    val token: String?

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------
    constructor() {
        id = 0
        email = ""
        displayName = ""
        stats = null
        organization = null
        partner = null
        token = null
        avatarURL = null
    }

    private constructor(id: Int, email: String, displayName: String, stats: Stats, organization: Organization, token: String, avatarURL: String?) {
        this.id = id
        this.email = email
        this.displayName = displayName
        this.stats = stats
        this.organization = organization
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
    fun getMemberships(type: String?): ArrayList<UserMembership> {
        type?.let {
            memberships?.forEach { membershipList ->
                if (it.equals(membershipList.type, ignoreCase = true)) {
                    return membershipList.list
                }
            }
        }
        return ArrayList()
    }

    fun incrementTours() {
        stats?.let { stat ->
            stat.tourCount += 1
        }
    }

    fun incrementEncouters() {
        stats?.let { stat ->
            stat.encounterCount += 1
        }
    }

    val isPro: Boolean
        get() = TYPE_PRO == type

    fun asTourAuthor(): FeedItemAuthor {
        return FeedItemAuthor(avatarURL, id, displayName, partner)
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
            if(isFirst) {
                isFirst = false
            } else {
                interestsString.append(", ")
            }
            interestsString.append(context.getString(getStringId(interest)))
        }
        return interestsString.toString()
    }

    val isUserTypeAlone: Boolean
        get() = USER_GOAL_ALONE.equals(goal, ignoreCase = true)

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
    class UserFirebaseProperties : Serializable {
        @SerializedName("ActionZoneDep")
        val actionZoneDep = ""

        @SerializedName("ActionZoneCP")
        val actionZoneCP = ""

        companion object {
            private const val serialVersionUID: Long = -90000095L
            const val actionZoneDepName = "ActionZoneDep"
            const val actionZoneCPName = "ActionZoneCP"
        }
    }

    // ----------------------------------
    // User Address
    // ----------------------------------
    class Address : Serializable {
        var latitude = 0.0
        var longitude = 0.0

        @JvmField @SerializedName("display_address")
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
        const val TYPE_PRO = "pro"
        private const val USER_ROLE_ETHICS_CHARTER_SIGNED = "ethics_charter_signed"
        const val USER_GOAL_NEIGHBOUR = "offer_help"
        const val USER_GOAL_ALONE = "ask_for_help"
        const val USER_GOAL_ASSO = "organization"
        const val USER_GOAL_NONE = ""

        private fun getStringId(resourceName: String): Int {
            return try {
                val stringId = string::class.java.getDeclaredField(resourceName)
                stringId.getInt(stringId)
            } catch (e: Exception) {
                Timber.d("Resource not found : $resourceName")
                -1
            }
        }
    }
}