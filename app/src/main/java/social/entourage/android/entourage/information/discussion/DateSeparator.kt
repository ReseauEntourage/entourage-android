package social.entourage.android.entourage.information.discussion

import social.entourage.android.api.model.TimestampedObject
import java.util.*

/**
 * Created by mihaiionescu on 14/03/2017.
 */
class DateSeparator(private var date: Date) : TimestampedObject() {
    override fun getType(): Int {
        return DATE_SEPARATOR
    }

    override fun getTimestamp(): Date {
        return date
    }

    override fun hashString(): String {
        return ""
    }

    override fun getId(): Long {
        return 0
    }
}