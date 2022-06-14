package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName

data class Pedago(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("image")
    val imageUrl: String?,

    @SerializedName("title")
    var title: String?
)
