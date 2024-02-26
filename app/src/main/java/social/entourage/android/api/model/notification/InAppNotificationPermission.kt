package social.entourage.android.api.model.notification

import com.google.gson.annotations.SerializedName

class InAppNotificationPermission(
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