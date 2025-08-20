package social.entourage.android.api.model.guide

class ClusterPoiResponse(
    var clusters: List<ClusterPoi>
)

class ClusterPoi(
    var id: Int?,
    var uuid: String?,
    var type: String,
    var count: Int,
    var name: String?,
    var category_id: Int?,
    var latitude: Double,
    var longitude: Double
)
