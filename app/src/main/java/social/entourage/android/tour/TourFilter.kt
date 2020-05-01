package social.entourage.android.tour

import social.entourage.android.api.model.tour.TourType
import java.io.Serializable

object TourFilter : Serializable {
    var tourTypeMedical = true
    var tourTypeSocial = true
    var tourTypeDistributive = true
    var timeframe:Int

    // ----------------------------------
    // MapFilter implementation
    // ----------------------------------
    @JvmStatic
    fun getTypes(): String {
        val entourageTypes = StringBuilder()
        if (tourTypeMedical) {
            entourageTypes.append(TourType.MEDICAL.key)
        }
        if (tourTypeSocial) {
            if (entourageTypes.isNotEmpty()) entourageTypes.append(",")
            entourageTypes.append(TourType.BARE_HANDS.key)
        }
        if (tourTypeDistributive) {
            if (entourageTypes.isNotEmpty()) entourageTypes.append(",")
            entourageTypes.append(TourType.ALIMENTARY.key)
        }
        return entourageTypes.toString()
    }

    @JvmStatic
    fun getTimeFrame(): Int {
        return timeframe
    }

    fun isDefaultFilter(): Boolean{
        return when {
            !tourTypeMedical -> false
            !tourTypeSocial -> false
            !tourTypeDistributive -> false
            //normal filter
            timeframe != DAYS_3 -> false
            else -> true
        }
    }

    fun setDefaultValues() {
        tourTypeMedical = true
        tourTypeSocial = true
        tourTypeDistributive = true
        timeframe = DAYS_3
    }

    private const val serialVersionUID = -2822136342813499636L

    // ----------------------------------
    // Attributes
    // ----------------------------------
    const val DAYS_1 = 24 //hours
    const val DAYS_2 = 8 * 24 //hours
    const val DAYS_3 = 30 * 24 //hours

    init {
        timeframe = DAYS_3
        setDefaultValues()
    }
}