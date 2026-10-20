package social.entourage.android.api.model

import android.content.Context
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.notification.Translation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
data class Group(
    @SerialName("id")
    var id: Int? = null,
    @SerialName("uuid_v2")
    var uuid_v2: String? = null,
    @SerialName("zone")
    var zone: String? = null,
    @SerialName("name")
    var name: String? = null,
    @SerialName("name_translations")
    val nameTranslations: Translation? = null,
    @SerialName("description")
    var description: String? = null,
    @SerialName("description_translations")
    val descriptionTranslations: Translation? = null,
    @SerialName("welcome_message")
    var welcomeMessage: String? = null,
    @SerialName("ethics")
    var ethics: String? = null,
    @SerialName("address")
    var address: Address? = null,
    @SerialName("neighborhood_image_id")
    var neighborhoodImageId: Int? = null,
    @SerialName("other_interest")
    var otherInterest: String? = null,
    @SerialName("interests")
    var interests: MutableList<String> = mutableListOf(),
    @SerialName("image_url")
    var imageUrl: String? = null,
    @SerialName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),
    @SerialName("future_outings_count")
    var futureOutingsCount: Int? = null,
    @SerialName("user")
    var admin: GroupMember? = null,
    @SerialName("members_count")
    var members_count: Int? = null,
    @SerialName("member")
    var member: Boolean = false,
    @SerialName("national")
    var national: Boolean = false,
    @SerialName("future_outings")
    var futureEvents: MutableList<Events>? = mutableListOf(),
    @SerialName("latitude")
    var latitude: Double? = null,
    @SerialName("longitude")
    var longitude: Double? = null,
    @SerialName("radius")
    var radius: Int? = null,
    @SerialName("created_at")
    var createdAt: String? = null,
    @SerialName("updated_at")
    var updatedAt: String? = null,
    @SerialName("slug")
    var slug: String? = null,
    @SerialName("default_group")
    var defaultGroup: Boolean = false,
    @SerialName("group_type")
    var groupType: String? = null,
    @SerialName("parent_id")
    var parentId: Int? = null,
    @SerialName("children")
    var children: MutableList<Group>? = mutableListOf(),
    @SerialName("ancestors")
    var ancestors: MutableList<Group>? = mutableListOf(),
    @SerialName("display_address")
    var displayAddress: String? = null,
    @SerialName("unread_posts_count")
    var unreadPostsCount: Int? = null,
    var isSelected: Boolean = false
) {
    // Extension property for easier access to joined status
    val isJoined: Boolean
        get() = member

    fun name(value: String) = apply {
        name = value
    }

    fun description(value: String) = apply {
        description = value
    }

    fun welcomeMessage(value: String) = apply {
        welcomeMessage = value
    }

    fun ethics(value: String) = apply {
        ethics = value
    }

    fun address(value: Address) = apply {
        address = value
    }

    fun neighborhoodImageId(value: Int?) = apply {
        neighborhoodImageId = value
    }

    fun otherInterest(value: String) = apply {
        otherInterest = value
    }

    fun interests(value: MutableList<String>) = apply {
        interests = value
    }

    fun latitude(value: Double) = apply {
        latitude = value
    }

    fun longitude(value: Double) = apply {
        longitude = value
    }

    override fun toString(): String {
        return "Group(id=$id, name=$name, description=$description, welcomeMessage=$welcomeMessage, ethics=$ethics, address=$address, neighborhoodImageId=$neighborhoodImageId, otherInterest=$otherInterest, interests=$interests, imageUrl=$imageUrl, members=$members, futureOutingsCount=$futureOutingsCount, admin=$admin, members_count=$members_count)"
    }

}
class GroupUtils {
    companion object {
        fun showTagTranslated(context: Context, section: String): String {
            when(section.lowercase()){
                "activites" -> return context.getString(R.string.interest_activites)
                "animaux" -> return context.getString(R.string.interest_animaux)
                "bien-etre" -> return context.getString(R.string.interest_bien_etre)
                "cuisine" -> return context.getString(R.string.interest_cuisine)
                "culture" -> return context.getString(R.string.interest_culture)
                "jeux" -> return context.getString(R.string.interest_jeux)
                "nature" -> return context.getString(R.string.interest_nature)
                "sport" -> return context.getString(R.string.interest_sport)
                "marauding" -> return context.getString(R.string.interest_marauding)
                "other" -> return context.getString(R.string.interest_other)
                else -> return context.getString(R.string.interest_other)
            }
        }
    }
}