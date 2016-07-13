package social.entourage.android.api.model;

import android.os.Build;

import com.google.gson.annotations.SerializedName;

import social.entourage.android.BuildConfig;

/**
 * Created by mihaiionescu on 09/03/16.
 */
public class ApplicationInfo {

    private static final String DEVICE_TYPE = "android ";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("push_token")
    private String pushToken;

    @SerializedName("device_os")
    private String deviceOS;

    @SerializedName("device_family")
    private final String deviceFamily = "ANDROID";

    private String version;

    public ApplicationInfo(final String pushToken) {
        this.pushToken = pushToken;
        this.deviceOS = DEVICE_TYPE + Build.VERSION.RELEASE;
        this.version = BuildConfig.VERSION_NAME;
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

    }
}
