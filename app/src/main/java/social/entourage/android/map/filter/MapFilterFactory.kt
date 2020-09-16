package social.entourage.android.map.filter

import social.entourage.android.EntourageApplication

/**
 * Created by mihaiionescu on 27/10/16.
 */
object MapFilterFactory {
    val mapFilter: MapFilter
        get() {
            return EntourageApplication.get().entourageComponent.authenticationController.mapFilter
        }
}