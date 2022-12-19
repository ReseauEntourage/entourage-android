package social.entourage.android.old_v7.entourage.information.discussion

import social.entourage.android.api.model.TimestampedObject
import java.util.*

/**
 * Created by mihaiionescu on 14/03/2017.
 */
class DateSeparator(private var date: Date) : TimestampedObject() {
    override val type: Int
        get() = DATE_SEPARATOR

    override val timestamp: Date
        get() = date

    override fun hashString(): String {
        return ""
    }

    override val id: Long
        get() = 0
}