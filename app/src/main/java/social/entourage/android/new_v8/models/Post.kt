package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import java.text.SimpleDateFormat
import java.util.*

class Post(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("user")
    var user: EntourageUser? = null,
    @SerializedName("created_at")
    var createdTime: Date? = null,
    @SerializedName("message_type")
    val messageType: String? = null,
    @SerializedName("post_id")
    val postId: Int? = null,
    @SerializedName("has_comments")
    val hasComments: Boolean? = null,
    @SerializedName("comments_count")
    val commentsCount: Int? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("read")
    val read: Boolean? = null,
    val idInternal: UUID? = null,
) {
    var datePostText = ""
    var isDatePostOnly = false

    fun getFormatedStr() : String {
        return createdTime?.let { SimpleDateFormat("EEEE dd MMMM yyyy",Locale.FRANCE).format(it) } ?: ""
    }

    override fun toString(): String {
        return "Post(id=$id, content=$content, user=$user, createdTime=$createdTime, messageType=$messageType, postId=$postId, hasComments=$hasComments, commentsCount=$commentsCount, imageUrl=$imageUrl, read=$read)"
    }
}