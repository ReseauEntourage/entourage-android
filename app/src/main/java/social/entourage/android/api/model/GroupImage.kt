package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName

class GroupImage {
    @SerializedName("id")
    var id: String? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("image_url")
    var imageUrl: String? = null

    override fun toString(): String {
        return "GroupImage(id=$id, title=$title, imageUrl=$imageUrl)"
    }
}