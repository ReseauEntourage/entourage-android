package social.entourage.android;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.PermissionChecker;

import javax.inject.Inject;

import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.login.LoginActivity;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

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
        //EntourageEvents.logEvent(Constants.EVENT_LOGOUT);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public String getLink(String linkId) {
        if (authenticationController != null && authenticationController.getUser() != null) {
            String link = getString(R.string.redirect_link_format, BuildConfig.ENTOURAGE_URL, linkId, authenticationController.getUser().getToken());
            return link;
        }
        return super.getLink(linkId);
    }

    public boolean isGeolocationGranted() {
        return (PermissionChecker.checkSelfPermission(this, getUserLocationAccess()) == PackageManager.PERMISSION_GRANTED);
    }

    public String getUserLocationAccess() {
        User user = authenticationController.getUser();
        return user != null ? user.getLocationAccessString() : ACCESS_COARSE_LOCATION;
    }
}