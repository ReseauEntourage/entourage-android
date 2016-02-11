package social.entourage.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import javax.inject.Inject;

import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;

/**
 * Presenter controlling the DrawerActivity
 * @see DrawerActivity
 */
public class DrawerPresenter {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String MARKET_PREFIX = "market://details?id=";

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
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_borders_white);
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
                    if (error.getResponse().getStatus() == 426) {
                        if (!BuildConfig.DEBUG) {
                            displayAppUpdateDialog();
                        }
                    }
                }
            });
            defaultSharedPreferences.edit().putBoolean(Constants.UPDATE_DIALOG_DISPLAYED, true).commit();
        }
    }
}