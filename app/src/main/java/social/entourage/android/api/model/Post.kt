package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.notification.Translation
import java.text.SimpleDateFormat
import java.util.*

class Post(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("content_html")
    val contentHtml: String? = null,
    @SerializedName("content_translations")
    val contentTranslations: Translation? = null,
    @SerializedName("content_translations_html")
    val contentTranslationsHtml: Translation? = null,
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
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("reactions")
    var reactions: MutableList<Reaction>? = mutableListOf(),
    @SerializedName("read")
    val read: Boolean? = null,
    @SerializedName("reaction_id")
    var reactionId: Int? = null,
    val idInternal: UUID? = null,
    @SerializedName("survey")
    val survey: Survey? = null,
    @SerializedName("survey_response")
    var surveyResponse: MutableList<Boolean>? = mutableListOf(),
    @SerializedName("auto_post_from")
    val autoPostFrom: AutoPostFrom? = null,
) {
    var datePostText = ""
    var isDatePostOnly = false

    fun getFormatedStr() : String {
        val locale = Locale.getDefault()
        return createdTime?.let { SimpleDateFormat("EEEE dd MMMM yyyy",locale).format(it) } ?: ""
    }

    override fun toString(): String {
        return "Post(id=$id, content=$content, user=$user, createdTime=$createdTime, messageType=$messageType, postId=$postId, hasComments=$hasComments, commentsCount=$commentsCount, imageUrl=$imageUrl, read=$read)"
    }
}


data class Survey(
    @SerializedName("choices")
    var choices: List<String>,
    @SerializedName("multiple")
    var multiple: Boolean,
    @SerializedName("summary")
    var summary: MutableList<Int>
)

data class AutoPostFrom(
    @SerializedName("instance_type")
    val instanceType: String, // ou Entourage, selon ce que tu as besoin de représenter
    @SerializedName("instance_id")
    val instanceId: Int
)
