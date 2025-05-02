package social.entourage.android.api.model

import android.content.Context
import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.ActionMetadata
import social.entourage.android.api.model.notification.Translation
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.utils.Utils
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Action(
    @field:SerializedName("id")
    var id: Int? = null,

    @field:SerializedName("uuid")
    var uuid: String? = null,

    @field:SerializedName("uuid_v2")
    var uuid_v2: String? = null,

    @field:SerializedName("status")
    private var status: String? = null,

    @field:SerializedName("title")
    var title: String? = null,

    @SerializedName("title_translations")
    val titleTranslations: Translation? = null,

    @field:SerializedName("description")
    var description: String? = null,

    @SerializedName("description_translations")
    val descriptionTranslations: Translation? = null,

    @field:SerializedName("image_url")
    var imageUrl: String? = null,

    @field:SerializedName("author")
    val author: AuthorAction? = null,

    @field:SerializedName("section")
    var sectionName: String? = null,

    @field:SerializedName("location")
    var location: Address? = null,

    @field:SerializedName("distance")
    var distance: Double? = null,

    @field:SerializedName("metadata")
    var metadata: ActionMetadata? = null,

    @field:SerializedName("action_type")
    var actionType: String? = null,

    @field:SerializedName("auto_post_at_create")
    var autoPostAtCreate: Boolean? = null,

    @field:SerializedName("created_at")
    val createdAt: Date? = null,
    @field:SerializedName("status_changed_at")
    val statusChangedAt: Date? = null,
    @field:SerializedName("updated_at")
    val updatedAt: Date? = null,

    @field:SerializedName("recipient_consent_obtained")
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
                context?.getString(R.string.feed_event_date),
                locale
            ).format(
                it
            )
            //val _str = Utils.dateAsStringLitteralFromNow(it,context,null,false)
            return context.getString(R.string.action_created_by, _str)
        } ?: return  context.getString(R.string.action_created_by_)
    }
}

class AuthorAction (
    @field:SerializedName("avatar_url") var avatarURLAsString: String?,
    @field:SerializedName("id") var userID: Int,
    @field:SerializedName("display_name") var userName: String?,
    @field:SerializedName("partner") var partner: Partner?,
    @field:SerializedName("created_at") val creation_date:Date? = null) : Serializable {

    companion object {
        private const val serialVersionUID = 3412733374231780458L
    }

}

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

data class ActionCancel (
    @field:SerializedName("outcome")
    val outcome: Boolean,

    @field:SerializedName("close_message")
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

