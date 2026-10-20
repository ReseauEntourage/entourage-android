package social.entourage.android.authentication

import social.entourage.android.base.map.filter.MapFilter
import java.io.Serializable
import kotlinx.serialization.Serializable as KSerializable

/**
 * Contains an user preferences, that can be saved
 * Created by Mihai Ionescu on 20/10/2017.
 */
@KSerializable
class UserPreferences : Serializable {

    var isShowInfoPOIsPopup = true
    var mapFilter: MapFilter? = null

    var isShowNoEntouragesPopup = true
    var isShowNoPOIsPopup = true
    var isIgnoringActionZone = false
    var isEntourageDisclaimerShown = false
    var isOnboardingUser = false
    var isEditActionZoneShown = false
}