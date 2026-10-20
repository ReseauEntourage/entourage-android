package social.entourage.android.api.model.notification
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable


@KSerializable
class InAppNotificationPermission(
    @SerialName("neighborhood")
    var neighborhood: Boolean = false,
    @SerialName("outing")
    var outing: Boolean = false,
    @SerialName("chat_message")
    var chat_message: Boolean = false,
    @SerialName("action")
    var action: Boolean = false) {
    fun isAllChecked() : Boolean {
        return neighborhood && outing && chat_message && action
    }
}