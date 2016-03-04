package social.entourage.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.message.push.RegisterGCMService;

/**
 * Base Activity that only runs if the user is currently logged
 */
public abstract class EntourageSecuredActivity extends EntourageActivity {

    @Inject
    AuthenticationController authenticationController;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!authenticationController.isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
            boolean notificationsEnabled = sharedPreferences.getBoolean(RegisterGCMService.KEY_NOTIFICATIONS_ENABLED, false);
            if (notificationsEnabled) {
                startService(new Intent(this, RegisterGCMService.class));
            }
        }
    }

    protected AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    protected void logout() {
        authenticationController.logOutUser();
        FlurryAgent.logEvent(Constants.EVENT_LOGOUT);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}