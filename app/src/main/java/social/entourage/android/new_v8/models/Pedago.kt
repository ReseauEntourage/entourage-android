package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import social.entourage.android.R

enum class Category(val id: Int) {
    @SerializedName("all")
    ALL(R.string.all),

    @SerializedName("act")
    ACT(R.string.act),

    @SerializedName("inspire")
    INSPIRE(R.string.inspire),

    @SerializedName("understand")
    UNDERSTAND(R.string.understand);

    override fun toString(): String {
        return "Category(value='$id')"
    }
}

data class Pedago(

    @field:SerializedName("duration")
    val duration: Long? = null,

    @field:SerializedName("is_video")
    val isVideo: Boolean? = null,

    @field:SerializedName("watched")
    val watched: Boolean? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("html")
    val html: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("category")
    val category: Category? = null,

    @field:SerializedName("url")
    val url: String? = null


) {
    override fun toString(): String {
        return "Pedago(duration=$duration, isVideo=$isVideo, watched=$watched, imageUrl=$imageUrl, name=$name, description=$description, html=$html, id=$id, category=$category, url=$url)"
    }
}
