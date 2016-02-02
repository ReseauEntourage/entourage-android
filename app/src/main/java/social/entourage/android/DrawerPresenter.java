package social.entourage.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.message.push.RegisterGCMService;

/**
 * Presenter controlling the DrawerActivity
 * @see DrawerActivity
 */
public class DrawerPresenter {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String LOG_TAG = "UPDATE_USER_INFO";
    private final static String KEY_EMAIL = "email";
    private final static String KEY_SMS_COE= "sms_code";
    private final static String KEY_PHONE= "phone";
    private final static String KEY_DEVICE_ID = "device_id";
    private final static String KEY_DEVICE_TYPE = "device_type";
    private final static String KEY_DEVICE_LOCATION = "device_location";
    private final static String ANDROID_DEVICE = "android";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final DrawerActivity activity;
    private final UserRequest userRequest;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Inject
    public DrawerPresenter(final DrawerActivity activity, final UserRequest userRequest) {
        this.activity = activity;
        this.userRequest = userRequest;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void updateUser(String email, String smsCode, String phone, Location location) {
        if (activity != null) {

            String deviceId = activity.getApplicationContext()
                    .getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE)
                    .getString(RegisterGCMService.KEY_REGISTRATION_ID, null);

            if (deviceId != null) {

                ArrayMap<String, Object> user = new ArrayMap<>();
                if (email != null) {
                    user.put(KEY_EMAIL, email);
                }
                if (smsCode != null) {
                    user.put(KEY_SMS_COE, smsCode);
                }
                if (phone != null) {
                    user.put(KEY_PHONE, phone);
                }
                if (location != null) {
                    user.put(KEY_DEVICE_LOCATION, location);
                }
                user.put(KEY_DEVICE_ID, deviceId);
                user.put(KEY_DEVICE_TYPE, ANDROID_DEVICE);

                userRequest.updateUser(user, new Callback<UserResponse>() {
                    @Override
                    public void success(UserResponse userResponse, Response response) {
                        Log.d(LOG_TAG, "success");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d(LOG_TAG, error.getLocalizedMessage());
                    }
                });
            }
        }
    }
}
