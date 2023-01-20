package social.entourage.android.api.model.feed

import com.google.gson.*
import social.entourage.android.api.model.BaseEntourage
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
            return (data as? BaseEntourage)?.id ?: 0
        }

    class NewsfeedItemJsonAdapter : JsonDeserializer<NewsfeedItem> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): NewsfeedItem {
            val newsfeed = NewsfeedItem()
            val jsonObject = json.asJsonObject
            jsonObject[TYPE].asString?.let {
                newsfeed.type = it
                val jsonData = jsonObject[DATA].asJsonObject
                if (jsonData !=null) {
                    try {
                        getClassFromString(it,
                                jsonData["group_type"]?.asString,
                                jsonData["entourage_type"]?.asString)?.let { newsfeedClass ->
                            val gson = GsonBuilder()
                                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                                    .create()
                            newsfeed.data = gson.fromJson<Any>(jsonData, newsfeedClass)
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
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