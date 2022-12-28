package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName

data class Filter(
    @field:SerializedName("id")
    val id: Int? = null,
    @field:SerializedName("label")
    val label: String? = null,
)
