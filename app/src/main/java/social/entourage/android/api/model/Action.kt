@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import android.content.Context
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.ActionMetadata
import social.entourage.android.api.model.notification.Translation
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.utils.Utils
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

@KSerializable
data class Action(
    @SerialName("id")
    var id: Int? = null,

    @SerialName("uuid")
    var uuid: String? = null,

    @SerialName("uuid_v2")
    var uuid_v2: String? = null,

    @SerialName("status")
    private var status: String? = null,

    @SerialName("title")
    var title: String? = null,

    @SerialName("title_translations")
    val titleTranslations: Translation? = null,

    @SerialName("description")
    var description: String? = null,

    @SerialName("description_translations")
    val descriptionTranslations: Translation? = null,

    @SerialName("image_url")
    var imageUrl: String? = null,

    @SerialName("author")
    val author: AuthorAction? = null,

    @SerialName("section")
    var sectionName: String? = null,

    @SerialName("location")
    var location: Address? = null,

    @SerialName("distance")
    var distance: Double? = null,

    @SerialName("metadata")
    var metadata: ActionMetadata? = null,

    @SerialName("action_type")
    var actionType: String? = null,

    @SerialName("auto_post_at_create")
    var autoPostAtCreate: Boolean? = null,

    @SerialName("created_at")
    val createdAt: Date? = null,
    @SerialName("status_changed_at")
    val statusChangedAt: Date? = null,
    @SerialName("updated_at")
    val updatedAt: Date? = null,

    @SerialName("recipient_consent_obtained")
    var hasConsent: Boolean? = null

) : Serializable {

    fun title(value: String) = apply {
        title = value
    }

    fun description(value: String) = apply {
        description = value
    }

    fun isCancel() : Boolean {
        return status == "closed"
    }

    fun setCancel() {
        status = "closed"
    }

    fun isDemand() : Boolean {
        return actionType != "contribution"
    }

    fun dateFormattedString(context: android.content.Context) : String {
        createdAt?.let {
            return Utils.dateAsStringLitteralFromNow(it, context, R.string.action_date_list)
        } ?: return  "-"
    }

    fun isMine() :Boolean {
       return author?.userID == EntourageApplication.get().me()?.id
    }

    fun memberSinceDateString(context: android.content.Context) : String {
        author?.creation_date?.let {
            try{
                val locale = Locale.getDefault()
                val dateStr = SimpleDateFormat(context.getString(R.string.action_date_month_year_formatter),
                    locale).format(it)
                return context.getString( R.string.action_member_since,dateStr)
            }catch (e:Exception){
                return  "-"
            }
        } ?: return  "-"
    }

    fun createdDateString(context: android.content.Context) : String {
        createdAt?.let {
            var locale = LanguageManager.getLocaleFromPreferences(context)
            val _str = SimpleDateFormat(
                context.getString(R.string.feed_event_date),
                locale
            ).format(
                it
            )
            //val _str = Utils.dateAsStringLitteralFromNow(it,context,null,false)
            return context.getString(R.string.action_created_by, _str)
        } ?: return  context.getString(R.string.action_created_by_)
    }
}

@KSerializable
class AuthorAction (
    @SerialName("avatar_url") var avatarURLAsString: String? = null,
    @SerialName("id") var userID: Int = 0,
    @SerialName("display_name") var userName: String? = null,
    @SerialName("partner") var partner: Partner? = null,
    @SerialName("created_at") val creation_date:Date? = null) : Serializable {

    companion object {
        private const val serialVersionUID = 3412733374231780458L
    }

}

@KSerializable
data class MetadataActionLocation (
    var streetAddress: String? = "",
    var placeName: String? = "",
    var googlePlaceId: String? = "",
    var latitude: Double? = null,
    var longitude: Double? =null,
    var displayAddress: String = ""

) {
    override fun toString(): String {
        return "EventMetadata(streetAddress=$streetAddress, displayAddress=$displayAddress, googlePlaceId=$googlePlaceId) placename: $placeName - lat: $latitude - long: $longitude"
    }
}

@KSerializable
data class ActionCancel (
    @SerialName("outcome")
    val outcome: Boolean = false,

    @SerialName("close_message")
    val closeMessage: String? = null
)

class ActionUtils {
    companion object {
        fun showTagTranslated(context: Context,section: String):String{
            when(section.lowercase()){
                "social" -> return context.getString(R.string.action_social_name)
                "clothes" -> return context.getString(R.string.action_clothes_name)
                "equipment" -> return context.getString(R.string.action_equipment_name)
                "hygiene" -> return context.getString(R.string.action_hygiene_name)
                "services" -> return context.getString(R.string.action_services_name)
                else -> return context.getString(R.string.interest_other)
            }
        }
        fun showSubTagTranslated(context: Context,section: String):String{
            when(section){
                "social" -> return context.getString(R.string.action_social_subname)
                "clothes" -> return context.getString(R.string.action_clothes_subname)
                "equipment" -> return context.getString(R.string.action_equipment_subname)
                "hygiene" -> return context.getString(R.string.action_hygiene_subname)
                "services" -> return context.getString(R.string.action_services_subname)
                else -> return context.getString(R.string.interest_other)
            }
        }
    }
}

