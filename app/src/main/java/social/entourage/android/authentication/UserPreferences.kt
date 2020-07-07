package social.entourage.android.authentication

import com.google.gson.annotations.Expose
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.entourage.my.filter.MyEntouragesFilter
import social.entourage.android.map.filter.MapFilter
import java.io.Serializable

/**
 * Contains an user preferences, that can be saved
 * Created by Mihai Ionescu on 20/10/2017.
 */
class UserPreferences : Serializable {
    var isUserToursOnly = false
    var isShowInfoPOIsPopup = true
    var isShowEncounterDisclaimer = false
    @JvmField
    var mapFilter: MapFilter? = null
    @JvmField
    var myEntouragesFilter: MyEntouragesFilter? = null
    @JvmField
    var ongoingTour: Tour? = null
    @Expose(serialize = false)
    var isShowNoEntouragesPopup = true
    @Expose(serialize = false)
    var isShowNoPOIsPopup = true
    @Expose(serialize = false)
    var isIgnoringActionZone = false
}