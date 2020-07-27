package social.entourage.android.api.model

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mihaiionescu on 01/06/16.
 * Class that handles the Date formating in retrofit requests (not in json body)
 */
class EntourageRequestDate(private val date: Date) {
    override fun toString(): String {
        return dateFormatter.get()!!.format(date)
    }

    companion object {
        private val dateFormatter: ThreadLocal<DateFormat> = object : ThreadLocal<DateFormat>() {
            public override fun initialValue(): DateFormat {
                return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.FRANCE)
            }
        }
    }

}