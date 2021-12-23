package social.entourage.android.api.model

import android.content.Context
import social.entourage.android.R
import java.io.Serializable

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
        return context.getString(R.string.entourage_type_format_bis, context.getString(R.string.entourage_type_demand))
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
