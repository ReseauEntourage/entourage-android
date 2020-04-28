package social.entourage.android.api

import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.model.map.Category
import social.entourage.android.api.model.map.Encounter

class PoiResponse {
    var encounters: List<Encounter>? = null
    var pois: List<Poi>? = null
    var categories: List<Category>? = null

}