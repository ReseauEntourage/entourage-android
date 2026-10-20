@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model.notification

import social.entourage.android.R
import social.entourage.android.tools.utils.Utils
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

/**
 * Created by Me on 23/11/2022.
 */
@KSerializable
class InAppNotification (
    @SerialName("id")
    val id: Int? = null,
    @SerialName("instance_id")
    val instanceId: Int? = null,
    @SerialName("post_id")
    val postId: Int? = null,
    @SerialName("chat_message_id")
    val chatMessageId: Int? = null,
    @SerialName("welcome")
    val welcome: Boolean? = false,
    //value = h1, j2, j8, j11
    @SerialName("stage")
    val stage: String? = null,
    @SerialName("badge")
    val badge: String? = null,
    @SerialName("context")
    val context: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("instance")
    val instanceType: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("created_at")
    val createdAt: Date? = null,
    @SerialName("completed_at")
    var completedAt: Date? = null,
    @SerialName("image_url")
    val imageUrl: String? = null) {

    fun isRead() : Boolean {
        return  completedAt != null
    }

    fun dateFormattedString(context: android.content.Context) : String {
        createdAt?.let {
            return Utils.dateAsStringLitteralFromNow(it, context, R.string.action_date_list)
        } ?: return  "-"
    }
}

