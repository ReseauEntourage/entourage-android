package social.entourage.android.api.model

import com.google.gson.annotations.Expose
import java.util.*

/**
 * Created by mihaiionescu on 26/02/16.
 */
abstract class TimestampedObject {
    @Expose(serialize = false)
    private var hashCode = 0
    abstract val timestamp: Date?
    abstract fun hashString(): String
    override fun hashCode(): Int {
        if (hashCode == 0) {
            hashCode = hashString().hashCode()
        }
        return hashCode
    }

    abstract val type: Int
    abstract val id: Long

    /**
     * Copies the local fields (the ones not retrieved from the server) from other object
     * @param other The object to copy from
     */
    open fun copyLocalFields(other: TimestampedObject) {}

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    class TimestampedObjectComparatorOldToNew : Comparator<TimestampedObject> {
        override fun compare(lhs: TimestampedObject, rhs: TimestampedObject): Int {
            return if (rhs.timestamp != null) {
                lhs.timestamp?.compareTo(rhs.timestamp) ?: 0
            } else {
                0
            }
        }
    }

    companion object {
        const val SEPARATOR = 0
        const val CHAT_MESSAGE_ME = 1
        const val CHAT_MESSAGE_OTHER = 2
        const val USER_JOIN = 3
        const val ENTOURAGE_CARD = 7
        const val FEED_MEMBER_CARD = 8
        const val INVITATION_CARD = 9
        const val DATE_SEPARATOR = 10
        const val GUIDE_POI = 11
        const val INVITATION_LIST = 12
        const val ANNOUNCEMENT_CARD = 13
        const val CHAT_MESSAGE_OUTING = 14
        const val STATUS_UPDATE_CARD = 15
        const val LOADER_CARD = 16
        const val TOP_VIEW = 998
        const val BOTTOM_VIEW = 999
    }
}