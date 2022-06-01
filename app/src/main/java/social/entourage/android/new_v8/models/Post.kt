package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.EntourageUser
import java.util.*

class Post(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("user")
    var user: EntourageUser,
    @SerializedName("created_at")
    var createdTime: Date = Date(),
    @SerializedName("message_type")
    val messageType: String?,
    @SerializedName("post_id")
    val postId: Int?,
    @SerializedName("has_comments")
    val hasComments: Boolean?,
    @SerializedName("comments_count")
    val commentsCount: Int?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("read")
    val read: Boolean?,
) {
    override fun toString(): String {
        return "Post(id=$id, content=$content, user=$user, createdTime=$createdTime, messageType=$messageType, postId=$postId, hasComments=$hasComments, commentsCount=$commentsCount, imageUrl=$imageUrl, read=$read)"
    }
}