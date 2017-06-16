package social.entourage.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.HashSet;

import javax.inject.Inject;

import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.login.LoginActivity;

/**
 * Base Activity that only runs if the user is currently logged
 */
public abstract class EntourageSecuredActivity extends EntourageActivity {

    @Inject
    AuthenticationController authenticationController;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!authenticationController.isAuthenticated() || !authenticationController.isTutorialDone(getApplicationContext())) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            EntourageApplication application = (EntourageApplication)getApplication();
            application.finishLoginActivity();
        }
    }

    protected AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    protected void logout() {
        authenticationController.logOutUser();
        EntourageApplication application = EntourageApplication.get(getApplicationContext());
        if (application != null) {
            application.removeAllPushNotifications();
        }
        //FlurryAgent.logEvent(Constants.EVENT_LOGOUT);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}