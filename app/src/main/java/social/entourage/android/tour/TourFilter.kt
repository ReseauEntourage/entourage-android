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
    fun getTypes(): String {
        val tourTypes = StringBuilder()
        if (tourTypeMedical) {
            tourTypes.append(TourType.MEDICAL.key)
        }
        if (tourTypeSocial) {
            if (tourTypes.isNotEmpty()) tourTypes.append(",")
            tourTypes.append(TourType.BARE_HANDS.key)
        }
        if (tourTypeDistributive) {
            if (tourTypes.isNotEmpty()) tourTypes.append(",")
            tourTypes.append(TourType.ALIMENTARY.key)
        }
        return tourTypes.toString()
    }

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