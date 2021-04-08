package social.entourage.android.api.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import social.entourage.android.R
import java.io.Serializable

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

    constructor(actionGroupType: String) : super(GROUPTYPE_ACTION, actionGroupType)

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
        return context.getString(R.string.entourage_type_format_bis, context.getString(R.string.entourage_type_contribution))
    }
}
