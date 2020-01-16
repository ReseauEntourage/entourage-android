package social.entourage.android.api.model;

import android.os.Build;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

import social.entourage.android.BuildConfig;

/**
 * Created by mihaiionescu on 09/03/16.
 */
public class ApplicationInfo {

    private static final String DEVICE_TYPE = "android ";

    public static final String NOTIF_PERMISSION_AUTHORIZED = "authorized";
    public static final String NOTIF_PERMISSION_DENIED = "denied";
    private static final String NOTIF_PERMISSION_NOTDETERMINED = "not_determined";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("push_token")
    private String pushToken;

    @SerializedName("device_os")
    private String deviceOS;

    @SerializedName("device_family")
    private final String deviceFamily = "ANDROID";

    @SerializedName("notifications_permissions")
    private String notificationsPermissions;

    private String version;

    public ApplicationInfo(final String pushToken) {
        this.pushToken = pushToken;
        this.deviceOS = DEVICE_TYPE + Build.VERSION.RELEASE;
        this.version = BuildConfig.VERSION_NAME;
        notificationsPermissions = NOTIF_PERMISSION_NOTDETERMINED;
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class ApplicationWrapper {

        @SerializedName("application")
        private ApplicationInfo applicationInfo;

        public ApplicationInfo getApplicationInfo() {
            return applicationInfo;
        }

        public void setApplicationInfo(final ApplicationInfo applicationInfo) {
            this.applicationInfo = applicationInfo;
        }

        public void setNotificationStatus(final String notifStatus) {
            this.applicationInfo.notificationsPermissions = notifStatus;
        }

    }
}
