package social.entourage.android.api.model.feed

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.tour.Tour
import timber.log.Timber
import java.lang.reflect.Type

/**
 * Created by mihaiionescu on 05/05/16.
 */
class NewsfeedItem {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    var type: String? = null
    var data: Any? = null

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    val id: Long
        get() {
            var id: Long = 0
            if (data != null) {
                if (data is Tour) {
                    id = (data as Tour).id
                } else if (data is BaseEntourage) {
                    id = (data as BaseEntourage).id
                }
            }
            return id
        }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------
    class NewsfeedItemWrapper {
        @SerializedName("feeds")
        lateinit var newsfeedItems: List<NewsfeedItem>

    }

    class NewsfeedItemJsonAdapter : JsonDeserializer<NewsfeedItem> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): NewsfeedItem {
            val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .create()
            val newsfeed = NewsfeedItem()
            val jsonObject = json.asJsonObject
            try {
                newsfeed.type = jsonObject[TYPE].asString
                val jsonData = jsonObject[DATA].asJsonObject
                if (newsfeed.type != null && jsonData !=null) {
                    val newsfeedClass = getClassFromString(newsfeed.type!!,
                            jsonData["group_type"]?.asString,
                            jsonData["entourage_type"]?.asString)
                    if (newsfeedClass != null) {
                        newsfeed.data = gson.fromJson<Any>(jsonData, newsfeedClass)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
            return newsfeed
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val STATUS_ALL = "all"
        const val STATUS_ACTIVE = "active"
        const val STATUS_CLOSED = "closed"
        const val TYPE = "type"
        const val DATA = "data"

        fun getClassFromString(type: String, groupType:String?, actionGroupType: String?): Class<*>? {
            return when (type) {
                Tour.NEWSFEED_TYPE ->
                    Tour::class.java
                BaseEntourage.NEWSFEED_TYPE ->
                    BaseEntourage.getClassFromString(groupType, actionGroupType)
                Announcement.NEWSFEED_TYPE ->
                    Announcement::class.java
                else ->
                    null
            }
        }
    }
}