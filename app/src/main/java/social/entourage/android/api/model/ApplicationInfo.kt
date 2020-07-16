package social.entourage.android.api.model

import android.os.Build
import com.google.gson.annotations.SerializedName
import social.entourage.android.BuildConfig

/**
 * Created by mihaiionescu on 09/03/16.
 */
class ApplicationInfo(@field:SerializedName("push_token") private val pushToken: String) {

    @SerializedName("device_os")
    private val deviceOS: String

    @SerializedName("device_family")
    private val deviceFamily = "ANDROID"

    @SerializedName("notifications_permissions")
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