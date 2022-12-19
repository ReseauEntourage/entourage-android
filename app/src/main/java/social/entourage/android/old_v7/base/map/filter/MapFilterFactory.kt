package social.entourage.android.old_v7.base.map.filter

import social.entourage.android.EntourageApplication
import social.entourage.android.base.map.filter.MapFilter

/**
 * Created by mihaiionescu on 27/10/16.
 */
object MapFilterFactory {
    val mapFilter: MapFilter
        get() {
            return EntourageApplication.get().authenticationController.mapFilter
        }
}