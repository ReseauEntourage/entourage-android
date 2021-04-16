package social.entourage.android.api.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import social.entourage.android.R
import java.io.Serializable
import java.util.*

/**
 * Created by Mihai Ionescu on 28/04/16.
 */
class EntourageEvent : BaseEntourage, Serializable {
    companion object {
        const val serialVersionUID = -16560L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_OUTING, GROUPTYPE_OUTING)

    constructor(category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_OUTING, GROUPTYPE_OUTING, category, title, description, location)


    override fun getFeedTypeLong(context: Context): String {
        metadata?.let{
            if(it.startDate != null && it.endDate != null) {
                //check if start and end dates are on the same day
                val startCalendar = Calendar.getInstance()
                startCalendar.time = it.startDate ?: return ""
                val endCalendar = Calendar.getInstance()
                endCalendar.time = it.endDate ?: return ""
                return if (startCalendar[Calendar.DAY_OF_YEAR] == endCalendar[Calendar.DAY_OF_YEAR]) {
                    String.format("%1\$s %2\$s", context.getString(R.string.entourage_type_outing), it.getStartDateAsString(context))
                } else {
                    String.format("%1\$s %2\$s", context.getString(R.string.entourage_type_outing), it.getStartEndDatesAsString(context))
                }
            }
        }
        return ""
    }

    override fun getFeedTypeColor(): Int {
        return R.color.action_type_outing
    }

    override fun getIconDrawable(context: Context): Drawable? {
        return AppCompatResources.getDrawable(context, R.drawable.ic_event_accent_24dp)
    }

    override fun showHeatmapAsOverlay(): Boolean {
        return false
    }

    override fun getHeatmapResourceId(): Int {
        return R.drawable.ic_event_pin
    }

    @StringRes
    override fun getClosedCTAText(): Int {
        return super.getClosedCTAText()
    }

    @ColorRes
    override fun getClosedCTAColor(): Int {
        return super.getClosedCTAColor()
    }

    override fun getClosingLoaderMessage(): Int {
        return R.string.loader_title_outing_finish
    }

    override fun getClosedToastMessage(): Int {
        return R.string.outing_info_text_close
    }

    override fun isEvent() : Boolean {
        return true
    }

    @StringRes
    override fun getInviteSourceDescription():Int {
        return R.string.invite_source_description_outing
    }

    fun getEventDateFormated(context: Context): String {
        metadata?.let{
            if(it.startDate != null && it.endDate != null) {
                //check if start and end dates are on the same day
                val startCalendar = Calendar.getInstance()
                startCalendar.time = it.startDate ?: return ""
                val endCalendar = Calendar.getInstance()
                endCalendar.time = it.endDate ?: return ""
                return if (startCalendar[Calendar.DAY_OF_YEAR] == endCalendar[Calendar.DAY_OF_YEAR]) {
                    String.format("%1\$s", it.getStartDateAsString(context))
                } else {
                    String.format("%1\$s", it.getStartEndDatesAsString(context))
                }
            }
        }
        return ""
    }
}