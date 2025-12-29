package social.entourage.android.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val user: UserMetadata,
    val tags: TagsMetadata,
    @SerialName("poi_categories")
    val poiCategories: List<PoiCategory>
)

@Serializable
data class UserMetadata(
    val genders: Map<String, String>,
    @SerialName("discovery_sources")
    val discoverySources: Map<String, String>
)

@Serializable
data class TagsMetadata(
    val sections: List<SectionTag>,
    val interests: List<InterestTag>,
    val involvements: List<InvolvementTag>,
    val concerns: List<ConcernTag>,
    val signals: List<SignalTag>
)

@Serializable
data class SectionTag(
    val id: String,
    val name: String,
    val subname: String? = null
)

@Serializable
data class InterestTag(
    val id: String,
    val name: String
)

@Serializable
data class InvolvementTag(
    val id: String,
    val name: String
)

@Serializable
data class ConcernTag(
    val id: String,
    val name: String
)

@Serializable
data class SignalTag(
    val id: String,
    val name: String
)


@Serializable
data class PoiCategory(
    val id: Int,
    val name: String
)
