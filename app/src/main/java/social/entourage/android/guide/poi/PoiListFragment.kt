package social.entourage.android.guide.poi

import social.entourage.android.api.model.guide.Poi
import social.entourage.android.tools.log.AnalyticsEvents

interface PoiListFragment {
    fun showPoiDetails(poi: Poi, isTxtSearch:Boolean)
}