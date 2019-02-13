package social.entourage.android.map.permissions;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.authentication.login.LoginActivity;

public class NoLocationPermissionFragment extends DialogFragment {

    public static final String TAG = "fragment_no_location_permission";

    private boolean showingGeolocationSettings = false;
    private boolean enableGeolocation = false;

    public NoLocationPermissionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_location_permission, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                onBackButton();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showingGeolocationSettings) {
            onBackButton();
        }
    }

    @OnClick(R.id.no_location_back_button)
    protected void onBackButton() {
        if ( (getActivity() != null) && (getActivity() instanceof LoginActivity) ) {
            LoginActivity loginActivity = (LoginActivity)getActivity();
            loginActivity.saveGeolocationPreference(enableGeolocation);
            loginActivity.showNotificationPermissionView();
        }
        dismiss();
    }

    @OnClick(R.id.no_location_activate_button)
    protected void onActivateButton() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_GEOLOCATION_ACTIVATE_04_4A);
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        showingGeolocationSettings = true;
        enableGeolocation = true;
    }

    @Optional
    @OnClick(R.id.no_location_ignore_button)
    protected void onIgnoreButton() {
        onBackButton();
    }
}
