package social.entourage.android.api.model.guide

import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class ClusterPoiResponse(
    var clusters: List<ClusterPoi>
)

@KSerializable
class ClusterPoi(
    var id: Int? = null,
    var uuid: String? = null,
    var type: String = "",
    var count: Int = 0,
    var name: String? = null,
    var category_id: Int? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)
