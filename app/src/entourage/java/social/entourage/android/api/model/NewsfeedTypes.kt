package social.entourage.android.api.model

import social.entourage.android.api.model.map.Announcement
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.api.model.map.Tour

/**
 * Created by Mihai Ionescu on 18/04/2018.
 */
object NewsfeedTypes {
    @JvmStatic
    fun getClassFromString(type: String): Class<*>? {
        return when (type) {
            Tour.NEWSFEED_TYPE -> return Tour::class.java
            Entourage.NEWSFEED_TYPE -> return Entourage::class.java
            Announcement.NEWSFEED_TYPE -> return Announcement::class.java
            else -> null
        }
    }
}