package social.entourage.android.guide.poi

import social.entourage.android.api.model.guide.Poi

interface PoiListFragment {
    fun showPoiDetails(poi: Poi, isTxtSearch:Boolean)
}