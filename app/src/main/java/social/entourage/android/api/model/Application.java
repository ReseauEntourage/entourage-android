package social.entourage.android.api.model;

import android.content.pm.PackageInfo;
import android.os.Build;

import com.google.gson.annotations.SerializedName;

import social.entourage.android.BuildConfig;
import social.entourage.android.EntourageApplication;

/**
 * Created by mihaiionescu on 09/03/16.
 */
public class Application {

    private static final String DEVICE_TYPE = "android";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("push_token")
    private String pushToken;

    @SerializedName("device_os")
    private String deviceOS;

    private String version;

    public Application(final String pushToken) {
        this.pushToken = pushToken;
        this.deviceOS = DEVICE_TYPE+ Build.VERSION.SDK_INT;
        this.version = BuildConfig.VERSION_NAME;
    }
}
