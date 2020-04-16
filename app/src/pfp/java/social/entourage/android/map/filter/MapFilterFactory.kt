package social.entourage.android.map.filter

import social.entourage.android.EntourageApplication

/**
 * Created by mihaiionescu on 27/10/16.
 */
object MapFilterFactory {
    @JvmStatic
    fun getMapFilter(isProUser: Boolean): MapFilter {
        return MapFilter()
    }

    @JvmStatic
    val mapFilter: MapFilter
        get() {
            return EntourageApplication.get()?.entourageComponent?.authenticationController?.mapFilter ?: MapFilter()
        }
}