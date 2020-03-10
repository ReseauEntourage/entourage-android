package social.entourage.android.map.filter

import social.entourage.android.EntourageApplication

/**
 * Created by mihaiionescu on 27/10/16.
 */
object MapFilterFactory {
    @JvmStatic
    val mapFilter: MapFilter
        get() {
            val app = EntourageApplication.get()
            if (app != null && app.entourageComponent != null) {
                val authenticationController = app.entourageComponent.authenticationController
                if (authenticationController != null) {
                    return authenticationController.mapFilter
                }
            }
            return MapFilter()
        }
}