package social.entourage.android.api.model

import android.content.Context
import android.util.Log
import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItemAuthor
import social.entourage.android.api.model.notification.Translation
import social.entourage.android.events.EventModel
import java.io.Serializable
import java.util.*

enum class Status(val value: String) {
    @SerializedName("open")
    OPEN("open"),

    @SerializedName("closed")
    CLOSED("closed"),
}

data class GroupEvent(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,
) : Serializable

data class Events(

    @field:SerializedName("metadata")
    val metadata: EventMetadata? = null,

    @field:SerializedName("entourage_type")
    val entourageType: String? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("author")
    val author: FeedItemAuthor? = null,

    @field:SerializedName("created_at")
    val createdAt: Date? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @SerializedName("description_translations")
    val descriptionTranslations: Translation? = null,

    @field:SerializedName("group_type")
    val groupType: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("confirmed_member")
    val confirmedMember : Boolean? = null,

    @field:SerializedName("confirmed_members_count")
    val confirmedMembersCount: Int? = null,

    @SerializedName("title_translations")
    val titleTranslations: Translation? = null,

    @field:SerializedName("uuid")
    val uuid: String? = null,

    @field:SerializedName("uuid_v2")
    var uuid_v2: String? = null,

    @field:SerializedName("event_url")
    val eventUrl: String? = null,

    @field:SerializedName("number_of_people")
    val numberOfPeople: Int? = null,

    @field:SerializedName("public")
    val jsonMemberPublic: Boolean? = null,

    @field:SerializedName("updated_at")
    val updatedAt: Date? = null,

    @field:SerializedName("share_url")
    val shareUrl: String? = null,

    @field:SerializedName("online")
    val online: Boolean? = null,

    @field:SerializedName("location")
    val location: Address? = null,

    @field:SerializedName("distance")
    val distance: Double? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("members_count")
    val membersCount: Int? = null,

    @SerializedName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),

    @SerializedName("member")
    var member: Boolean = false,

    @SerializedName("interests")
    var interests: MutableList<String> = mutableListOf(),

    @field:SerializedName("number_of_unread_messages")
    val numberOfUnreadMessages: Int? = null,

    @field:SerializedName("postal_code")
    val postalCode: String? = null,

    @field:SerializedName("join_status")
    val joinStatus: String? = null,

    @field:SerializedName("display_report_prompt")
    val displayReportPrompt: Boolean? = null,

    @field:SerializedName("display_category")
    val displayCategory: String? = null,

    @field:SerializedName("status")
    val status: Status? = null,

    @field:SerializedName("address")
    var displayAddress: String? = null,

    @SerializedName("recurrency")
    var recurrence: Int? = 0,

    @SerializedName("neighborhoods")
    var neighborhoods: MutableList<GroupEvent>? = mutableListOf(),
) : Serializable

fun Events.toEventUi(context: Context): EventModel {
    return EventModel(
        this.id,
        this.title,
        this.author,
        this.membersCount,
        this.displayAddress,
        this.interests,
        this.description,
        this.members,
        this.member,
        EntourageApplication.me(context)?.id == this.author?.userID,
        this.online,
        this.metadata,
        this.eventUrl,
        this.createdAt,
        this.updatedAt,
        this.recurrence,
        this.neighborhoods,
        this.location,
        this.distance,
        this.status,
        this.metadata?.previousAt
    )
}

class EventUtils {
    companion object {
        fun showTagTranslated(context: Context,section: String): String {
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
                "activités manuelles"  -> return context.getString(R.string.interest_activites)
                "animaux" -> return context.getString(R.string.interest_animaux)
                "bien-être" -> return context.getString(R.string.interest_bien_etre)
                "cuisine" -> return context.getString(R.string.interest_cuisine)
                "art & culture" -> return context.getString(R.string.interest_culture)
                "jeux" -> return context.getString(R.string.interest_jeux)
                "nature" -> return context.getString(R.string.interest_nature)
                "sport" -> return context.getString(R.string.interest_sport)
                "rencontres nomades" -> return context.getString(R.string.interest_marauding)
                "temps de partage" -> return context.getString(R.string.action_social_name)
                "service" -> return context.getString(R.string.action_services_name)
                "vêtement" -> return context.getString(R.string.action_clothes_name)
                "équipement" -> return context.getString(R.string.action_equipment_name)
                "produit d'hygiène" -> return context.getString(R.string.action_hygiene_name)

                "social" -> return context.getString(R.string.action_social_name)
                "services" -> return context.getString(R.string.action_services_name)
                "clothes" -> return context.getString(R.string.action_clothes_name)
                "equipment" -> return context.getString(R.string.action_equipment_name)
                "hygiene" -> return context.getString(R.string.action_hygiene_name)


                else -> return context.getString(R.string.interest_other)
            }
        }
        fun showSubTagTranslated(context: Context,section: String): String {
            when(section.lowercase()){
                "temps de partage" -> return context.getString(R.string.action_social_subname)
                "service" -> return context.getString(R.string.action_services_subname)
                "vêtement" -> return context.getString(R.string.action_clothes_subname)
                "équipement" -> return context.getString(R.string.action_equipment_subname)
                "produit d'hygiène" -> return context.getString(R.string.action_hygiene_subname)
                "social" -> return context.getString(R.string.action_social_subname)
                "services" -> return context.getString(R.string.action_services_subname)
                "clothes" -> return context.getString(R.string.action_clothes_subname)
                "equipment" -> return context.getString(R.string.action_equipment_subname)
                "hygiene" -> return context.getString(R.string.action_hygiene_subname)
                else -> return context.getString(R.string.interest_other)
            }
        }
    }
}