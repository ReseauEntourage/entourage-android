package social.entourage.android.authentication

import com.google.gson.annotations.Expose
import social.entourage.android.old_v7.entourage.my.filter.MyEntouragesFilter
import social.entourage.android.base.map.filter.MapFilter
import java.io.Serializable

/**
 * Contains an user preferences, that can be saved
 * Created by Mihai Ionescu on 20/10/2017.
 */
class UserPreferences : Serializable {

    var isShowInfoPOIsPopup = true
    var mapFilter: MapFilter? = null
    var myEntouragesFilter: MyEntouragesFilter? = null

    @Expose(serialize = false)
    var isShowNoEntouragesPopup = true
    @Expose(serialize = false)
    var isShowNoPOIsPopup = true
    @Expose(serialize = false)
    var isIgnoringActionZone = false
    @Expose(serialize = false)
    var isEntourageDisclaimerShown = false
    @Expose(serialize = false)
    var isOnboardingUser = false
    @Expose(serialize = false)
    var isEditActionZoneShown = false
}