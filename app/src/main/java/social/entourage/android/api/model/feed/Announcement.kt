package social.entourage.android.api.model.feed

import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.TimestampedObject
import java.util.*

/**
 * Announcement received from the server
 * Created by Mihai Ionescu on 02/11/2017.
 */
class Announcement : TimestampedObject() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    override val id: Long = 0
    val title: String? = null
    val body: String? = null
    val action: String? = null
    val url: String? = null

    @SerializedName("icon_url")
    val iconUrl: String? = null
    val author: FeedItemAuthor? = null

    @SerializedName("image_url")
    val imageUrl: String? = null

    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------
    override val timestamp: Date?
        get() = null

    override fun hashString(): String {
        return HASH_STRING_HEAD + id
    }

    override val type: Int
        get() = ANNOUNCEMENT_CARD

    override fun equals(other: Any?): Boolean {
        return if (other !is Announcement) false else id == other.id
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val NEWSFEED_TYPE = "Announcement"
        private const val HASH_STRING_HEAD = "Announcement-"
    }
}