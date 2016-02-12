package social.entourage.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.AppRequest;
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
    private final String MARKET_PREFIX = "market://details?id=";
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
    private final AppRequest appRequest;
    private final UserRequest userRequest;

    private SharedPreferences defaultSharedPreferences;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Inject
    public DrawerPresenter(final DrawerActivity activity, final AppRequest appRequest, final UserRequest userRequest) {
        this.activity = activity;
        this.appRequest = appRequest;
        this.userRequest = userRequest;
        initializeSharedPreferences();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void displayAppUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.setView(R.layout.dialog_version_update)
                .setCancelable(false)
                .create();
        dialog.show();
        Button updateButton = (Button) dialog.findViewById(R.id.update_dialog_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(MARKET_PREFIX + activity.getPackageName());
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    Toast.makeText(activity, R.string.error_google_play_store_not_installed, Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            }
        });
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    private void initializeSharedPreferences() {
        defaultSharedPreferences = activity.getApplicationContext().getSharedPreferences(Constants.UPDATE_DIALOG_DISPLAYED, Context.MODE_PRIVATE);
        defaultSharedPreferences.edit().putBoolean(Constants.UPDATE_DIALOG_DISPLAYED, false).commit();
    }

    public void checkForUpdate() {
        if (!defaultSharedPreferences.getBoolean(Constants.UPDATE_DIALOG_DISPLAYED, false)) {
            appRequest.checkForUpdate(new ResponseCallback() {
                @Override
                public void success(Response response) {}

                @Override
                public void failure(RetrofitError error) {
                    Response response = error.getResponse();
                    if (response != null && response.getStatus() == 426) {
                        if (!BuildConfig.DEBUG) {
                            displayAppUpdateDialog();
                        }
                    }
                }
            });
            defaultSharedPreferences.edit().putBoolean(Constants.UPDATE_DIALOG_DISPLAYED, true).commit();
        }
    }

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
