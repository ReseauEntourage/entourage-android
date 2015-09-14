package social.entourage.android;

import android.content.Intent;
import android.os.Bundle;

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

        if(!authenticationController.isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    protected AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    protected void logout() {
        authenticationController.logOutUser();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
