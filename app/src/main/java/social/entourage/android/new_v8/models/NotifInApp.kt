package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import social.entourage.android.old_v7.tools.UtilsV7
import java.util.Date

/**
 * Created by Me on 23/11/2022.
 */
class NotifInApp (
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("instance_id")
    val instanceId: Int? = null,
    @SerializedName("instance")
    val instanceString: String? = null,
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
            return UtilsV7.dateAsStringLitteralFromNow(it, context, R.string.action_date_list)
        } ?: return  "-"
    }
}

class NotifInAppPermission(
    @SerializedName("neighborhood")
    var neighborhood: Boolean = false,
    @SerializedName("outing")
    var outing: Boolean = false,
    @SerializedName("chat_message")
    var chat_message: Boolean = false,
    @SerializedName("action")
    var action: Boolean = false,
) {
    fun isAllChecked() : Boolean {
        return neighborhood && outing && chat_message && action
    }
}