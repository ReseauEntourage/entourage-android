package social.entourage.android.api.model

import social.entourage.android.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
enum class Category(val id: Int = 0) {
    @SerialName("all")
    ALL(R.string.all),

    @SerialName("act")
    ACT(R.string.act),

    @SerialName("inspire")
    INSPIRE(R.string.inspire),

    @SerialName("understand")
    UNDERSTAND(R.string.understand);

    override fun toString(): String {
        return "Category(value='$name')"
    }
}

@KSerializable
data class Pedago(

    @SerialName("duration")
    val duration: Long? = null,

    @SerialName("is_video")
    val isVideo: Boolean? = null,

    @SerialName("watched")
    val watched: Boolean? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("html")
    val html: String? = null,

    @SerialName("id")
    val id: Int? = null,

    @SerialName("uuid_v2")
    var uuid_v2: String? = null,

    @SerialName("category")
    val category: Category? = null,

    @SerialName("url")
    val url: String? = null

) {
    override fun toString(): String {
        return "Pedago(duration=$duration, isVideo=$isVideo, watched=$watched, imageUrl=$imageUrl, name=$name, description=$description, html=$html, id=$id, category=$category, url=$url)"
    }
}
