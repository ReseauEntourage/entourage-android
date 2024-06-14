package social.entourage.android.api.model

import android.content.Context
import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.notification.Translation

data class Group(
    @SerializedName("id")
    var id: Int? = null,
    @field:SerializedName("uuid_v2")
    var uuid_v2: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("name_translations")
    val nameTranslations: Translation? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("description_translations")
    val descriptionTranslations: Translation? = null,
    @SerializedName("welcome_message")
    var welcomeMessage: String? = null,
    @SerializedName("ethics")
    var ethics: String? = null,
    @SerializedName("address")
    var address: Address? = null,
    @SerializedName("neighborhood_image_id")
    var neighborhoodImageId: Int? = null,
    @SerializedName("other_interest")
    var otherInterest: String? = null,
    @SerializedName("interests")
    var interests: MutableList<String> = mutableListOf(),
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),
    @SerializedName("future_outings_count")
    var futureOutingsCount: Int? = null,
    @SerializedName("user")
    var admin: GroupMember? = null,
    @SerializedName("members_count")
    var members_count: Int? = null,
    @SerializedName("member")
    var member: Boolean = false,
    @SerializedName("future_outings")
    var futureEvents: MutableList<Events>? = mutableListOf(),
    @SerializedName("latitude")
    var latitude: Double = 0.0,
    @SerializedName("longitude")
    var longitude: Double = 0.0,
    @SerializedName("display_address")
    var displayAddress: String = "",
    @SerializedName("unread_posts_count")
    var unreadPostsCount: Int = 0,
    var isSelected: Boolean = false
) {

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