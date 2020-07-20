package social.entourage.android.base;

import android.content.Intent;
import android.os.Bundle;

import javax.inject.Inject;

import social.entourage.android.BuildConfig;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.base.EntourageActivity;
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity;

/**
 * Base Activity that only runs if the user is currently logged in
 */
public abstract class EntourageSecuredActivity extends EntourageActivity {

    @Inject
    AuthenticationController authenticationController;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!authenticationController.isAuthenticated() || !authenticationController.isTutorialDone()) {
            startActivity(new Intent(this, PreOnboardingStartActivity.class));
            finish();
        } else {
            EntourageApplication application = (EntourageApplication)getApplication();
            application.finishLoginActivity();
        }
    }

    public AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    protected void logout() {
        authenticationController.logOutUser();
        EntourageApplication.get(getApplicationContext()).removeAllPushNotifications();
        //EntourageEvents.logEvent(EntourageEvents.EVENT_LOGOUT);
        startActivity(new Intent(this, PreOnboardingStartActivity.class));
        finish();
    }

    @Override
    public String getLink(String linkId) {
        if (authenticationController != null && authenticationController.getMe() != null) {
            return getString(R.string.redirect_link_format, BuildConfig.ENTOURAGE_URL, linkId, authenticationController.getMe().token);
        }
        return super.getLink(linkId);
    }
}