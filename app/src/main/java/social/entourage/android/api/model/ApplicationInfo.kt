package social.entourage.android.api.model

import android.os.Build
import social.entourage.android.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by mihaiionescu on 09/03/16.
 */
@KSerializable
class ApplicationInfo(@SerialName("push_token") private val pushToken: String = "") {

    @SerialName("device_os")
    private val deviceOS: String

    @SerialName("device_family")
    private val deviceFamily = "ANDROID"

    @SerialName("notifications_permissions")
    var notificationsPermissions: String

    private val version: String

    companion object {
        private const val DEVICE_TYPE = "android "
        const val NOTIF_PERMISSION_AUTHORIZED = "authorized"
        const val NOTIF_PERMISSION_DENIED = "denied"
        private const val NOTIF_PERMISSION_NOTDETERMINED = "not_determined"
    }

    init {
        deviceOS = DEVICE_TYPE + Build.VERSION.RELEASE
        version = BuildConfig.VERSION_FULL_NAME
        notificationsPermissions = NOTIF_PERMISSION_NOTDETERMINED
    }
}