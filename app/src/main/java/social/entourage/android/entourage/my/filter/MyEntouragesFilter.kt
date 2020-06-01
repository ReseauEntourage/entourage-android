package social.entourage.android.entourage.my.filter

import android.content.Context
import com.google.gson.annotations.Expose
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.tour.TourType
import social.entourage.android.api.model.BaseEntourage
import java.io.Serializable

/**
 * Created by mihaiionescu on 10/08/16.
 */
class MyEntouragesFilter  : Serializable {
    var isClosedEntourages = true

    // in 5.0+ we ignore this setting
    var showOwnEntouragesOnly = false
        get() = false // in 5.0+ we ignore this setting

    // in 5.0+ we ignore this setting
    var showJoinedEntourages = false
        get() = false // in 5.0+ we ignore this setting
    var isShowUnreadOnly = false

    // in 5.0+ we ignore this setting
    var showPartnerEntourages = false
        get() = false // in 5.0+ we ignore this setting
    var isEntourageTypeDemand = true
    var isEntourageTypeContribution = true
    var isShowTours = true

    @Expose(serialize = false, deserialize = false)
    private var allTourTypes: String? = null// in 5.0+ force to show all entourages

    // ----------------------------------
    // Methods
    // ----------------------------------
    val actionGroupTypes: String
        get() {
            val actionGroupTypes = StringBuilder()
            isEntourageTypeContribution = true
            isEntourageTypeDemand = true // in 5.0+ force to show all entourages
            if (isEntourageTypeDemand) {
                actionGroupTypes.append(BaseEntourage.GROUPTYPE_ACTION_DEMAND)
            }
            if (isEntourageTypeContribution) {
                if (actionGroupTypes.isNotEmpty()) actionGroupTypes.append(",")
                actionGroupTypes.append(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
            }
            return actionGroupTypes.toString()
        }

    // in 5.0+ force to show closed entourages
    val status: String
        get() {
            isClosedEntourages = true // in 5.0+ force to show closed entourages
            return if (isClosedEntourages) "all" else "active"
        }

    // in 5.0+ force to show all the tours
    val tourTypes: String
        get() {
            isShowTours = true // in 5.0+ force to show all the tours
            if (isShowTours) {
                if (allTourTypes == null) {
                    allTourTypes = (TourType.MEDICAL.typeName
                            + ','
                            + TourType.BARE_HANDS.typeName
                            + ','
                            + TourType.ALIMENTARY.typeName)
                }
                return allTourTypes ?: ""
            }
            return ""
        }

    companion object {
        private const val serialVersionUID = 8192790767027490636L

        //MyEntouragesFilterFactory
        fun get(context: Context?): MyEntouragesFilter {
            return EntourageApplication.get(context).entourageComponent.authenticationController?.myEntouragesFilter ?: MyEntouragesFilter()
        }

        fun save(myEntouragesFilter: MyEntouragesFilter?, context: Context?) {
            EntourageApplication.get(context).entourageComponent.authenticationController?.saveMyEntouragesFilter()
        }
    }
}