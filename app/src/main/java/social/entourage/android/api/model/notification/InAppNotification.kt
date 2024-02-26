package social.entourage.android.api.model.notification

import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import social.entourage.android.tools.utils.Utils
import java.util.*

/**
 * Created by Me on 23/11/2022.
 */
class InAppNotification (
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("instance_id")
    val instanceId: Int? = null,
    @SerializedName("post_id")
    val postId: Int? = null,
    @SerializedName("welcome")
    val welcome: Boolean? = false,
    //value = h1, j2, j8, j11
    @SerializedName("stage")
    val stage: String? = null,
    @SerializedName("context")
    val context: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("instance")
    val instanceType: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("created_at")
    val createdAt: Date? = null,
    @SerializedName("completed_at")
    var completedAt: Date? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
) {

    fun isRead() : Boolean {
        return  completedAt != null
    }

    fun dateFormattedString(context: android.content.Context) : String {
        createdAt?.let {
            return Utils.dateAsStringLitteralFromNow(it, context, R.string.action_date_list)
        } ?: return  "-"
    }
}

