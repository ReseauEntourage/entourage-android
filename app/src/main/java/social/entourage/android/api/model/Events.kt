@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import android.content.Context
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItemAuthor
import social.entourage.android.api.model.notification.Translation
import social.entourage.android.events.EventModel
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

@KSerializable
enum class Status(val value: String = "") {
    @SerialName("open")
    OPEN("open"),

    @SerialName("closed")
    CLOSED("closed"),

    @SerialName("cancelled")
    CANCELLED("cancelled"),
}

@KSerializable
data class GroupEvent(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("name")
    val name: String? = null) : Serializable

@KSerializable
data class Events(

    @SerialName("metadata")
    val metadata: EventMetadata? = null,

    @SerialName("entourage_type")
    val entourageType: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("author")
    val author: FeedItemAuthor? = null,

    @SerialName("created_at")
    val createdAt: Date? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("description_translations")
    val descriptionTranslations: Translation? = null,

    @SerialName("group_type")
    val groupType: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("confirmed_member")
    val confirmedMember : Boolean? = null,

    @SerialName("signable")
    val signable: Boolean? = null,

    @SerialName("confirmed_members_count")
    val confirmedMembersCount: Int? = null,

    @SerialName("title_translations")
    val titleTranslations: Translation? = null,

    @SerialName("uuid")
    val uuid: String? = null,

    @SerialName("uuid_v2")
    var uuid_v2: String? = null,

    @SerialName("event_url")
    val eventUrl: String? = null,

    @SerialName("number_of_people")
    val numberOfPeople: Int? = null,

    @SerialName("public")
    val jsonMemberPublic: Boolean? = null,

    @SerialName("manageable_by_current_user")
    val manageableByCurrentUser: Boolean? = null,

    @SerialName("updated_at")
    val updatedAt: Date? = null,

    @SerialName("share_url")
    val shareUrl: String? = null,

    @SerialName("online")
    val online: Boolean? = null,

    @SerialName("location")
    val location: Address? = null,

    @SerialName("distance")
    val distance: Double? = null,

    @SerialName("id")
    val id: Int? = null,

    @SerialName("members_count")
    val membersCount: Int? = null,

    @SerialName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),

    @SerialName("member")
    var member: Boolean = false,

    @SerialName("interests")
    var interests: MutableList<String> = mutableListOf(),

    @SerialName("number_of_unread_messages")
    val numberOfUnreadMessages: Int? = null,

    @SerialName("postal_code")
    val postalCode: String? = null,

    @SerialName("join_status")
    val joinStatus: String? = null,

    @SerialName("display_report_prompt")
    val displayReportPrompt: Boolean? = null,

    @SerialName("display_category")
    val displayCategory: String? = null,

    @SerialName("status")
    val status: Status? = null,

    @SerialName("address")
    var displayAddress: String? = null,

    @SerialName("recurrency")
    var recurrence: Int? = 0,

    @SerialName("neighborhoods")
    var neighborhoods: MutableList<GroupEvent>? = mutableListOf()) : Serializable

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
        this.metadata?.previousAt,
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
                "bien-être" -> return context.getString(R.string.interest_bien_etre)
                "art & culture" -> return context.getString(R.string.interest_culture)
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
fun Events.formatEventStartTime(): String {
    val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
    return this.metadata?.startsAt?.let { dateFormat.format(it) } ?: "Heure inconnue"
}