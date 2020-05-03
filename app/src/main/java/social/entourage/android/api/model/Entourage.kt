package social.entourage.android.api.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import social.entourage.android.R
import timber.log.Timber
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
        if (metadata != null && metadata!!.startDate != null && metadata!!.endDate != null) {
            //check si les dates de début et fin sont le même jour ou pas
            val startCalendar = Calendar.getInstance()
            startCalendar.time = metadata!!.startDate!!
            val endCalendar = Calendar.getInstance()
            endCalendar.time = metadata!!.endDate!!
            return if (startCalendar[Calendar.DAY_OF_YEAR] == endCalendar[Calendar.DAY_OF_YEAR]) {
                String.format("%1\$s %2\$s", context.getString(R.string.entourage_type_outing), metadata!!.getStartDateAsString(context))
            } else {
                String.format("%1\$s %2\$s", context.getString(R.string.entourage_type_outing), metadata!!.getStartEndDatesAsString(context))
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
    override fun getJoinRequestTitle(): Int {
        return R.string.tour_info_request_join_title_outing
    }

    @StringRes
    override fun getFreezedCTAText(): Int {
        return super.getFreezedCTAText()
    }

    @ColorRes
    override fun getFreezedCTAColor(): Int {
        return super.getFreezedCTAColor()
    }

    override fun getClosingLoaderMessage(): Int {
        return R.string.loader_title_outing_finish
    }

    override fun getClosedToastMessage(): Int {
        return R.string.outing_info_text_close
    }
}

class EntourageNeighborhood : BaseEntourage, Serializable {
    companion object {
        const val serialVersionUID = -113L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_NEIGHBORHOOD, GROUPTYPE_NEIGHBORHOOD)

    constructor(category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_NEIGHBORHOOD, GROUPTYPE_NEIGHBORHOOD, category, title, description, location)

    override fun getFeedTypeLong(context: Context): String {
        return context.getString(R.string.entourage_type_neighborhood)
    }

    override fun getFeedTypeColor(): Int {
        return R.color.action_type_neighborhood
    }

    override fun getIconDrawable(context: Context): Drawable? {
        return AppCompatResources.getDrawable(context, R.drawable.ic_neighborhood)
    }

    override fun showHeatmapAsOverlay(): Boolean {
        return false
    }

    override fun getHeatmapResourceId(): Int {
        return R.drawable.ic_neighborhood_marker
    }

    override fun canBeClosed(): Boolean {
        return false
    }

    override fun showAuthor(): Boolean {
        return false
    }
}

class EntourageConversation : BaseEntourage, Serializable {
    companion object {
        const val serialVersionUID = -967689727L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_CONVERSATION, GROUPTYPE_CONVERSATION)

    constructor(category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_CONVERSATION, GROUPTYPE_CONVERSATION, category, title, description, location)

    override fun getIconDrawable(context: Context): Drawable? {
        return null
    }

    override fun getIconURL(): String? {
        return author?.avatarURLAsString ?: super.getIconURL()
    }
}

class EntouragePrivateCircle : BaseEntourage, Serializable {
    companion object {
        const val serialVersionUID = -23482L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_PRIVATE_CIRCLE, GROUPTYPE_PRIVATE_CIRCLE)

    constructor(category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_PRIVATE_CIRCLE, GROUPTYPE_PRIVATE_CIRCLE, category, title, description, location)

    override fun getFeedTypeLong(context: Context): String {
        return context.getString(R.string.entourage_type_private_circle)
    }
}

abstract class EntourageAction : BaseEntourage, Serializable {
    companion object {
        const val serialVersionUID = -1803751L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_ACTION, GROUPTYPE_ACTION_CONTRIBUTION)

    constructor(actionGroupType: String) : super(GROUPTYPE_ACTION, actionGroupType) {}

    constructor(actionGroupType: String, category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_ACTION, actionGroupType, category, title, description, location)

}

class EntourageDemand : EntourageAction, Serializable {
    companion object {
        const val serialVersionUID = -19L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_ACTION_DEMAND)

    constructor(category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_ACTION_DEMAND, category, title, description, location)
    override fun getFeedTypeLong(context: Context): String {
        return context.getString(R.string.entourage_type_format, context.getString(R.string.entourage_type_demand))
    }
}

class EntourageContribution : EntourageAction, Serializable {
    companion object {
        const val serialVersionUID = -16548L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_ACTION_CONTRIBUTION)

    constructor(category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_ACTION_CONTRIBUTION, category, title, description, location)

    override fun getFeedTypeLong(context: Context): String {
        return context.getString(R.string.entourage_type_format, context.getString(R.string.entourage_type_contribution))
    }
}
