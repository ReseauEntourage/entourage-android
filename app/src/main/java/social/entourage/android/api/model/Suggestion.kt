package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SuggestionReason(
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("text")
    val text: String? = null
) : Serializable

data class SuggestionMetadata(
    @SerializedName("starts_at")
    val startsAt: String? = null,
    @SerializedName("location")
    val location: String? = null
) : Serializable

data class Suggestion(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("distance")
    val distance: Double? = null,
    @SerializedName("metadata")
    val metadata: SuggestionMetadata? = null,
    @SerializedName("cta")
    val cta: String? = null,
    @SerializedName("score")
    val score: Double? = null,
    @SerializedName("reasons")
    val reasons: List<SuggestionReason> = emptyList()
) : Serializable

data class SuggestionsMeta(
    @SerializedName("current_page")
    val currentPage: Int = 1,
    @SerializedName("total_pages")
    val totalPages: Int = 1,
    @SerializedName("total_count")
    val totalCount: Int = 0
)

data class SuggestionsResponse(
    @SerializedName("lifecycle_segment")
    val lifecycleSegment: String? = null,
    @SerializedName("suggestions")
    val suggestions: List<Suggestion> = emptyList(),
    @SerializedName("meta")
    val meta: SuggestionsMeta? = null
)
