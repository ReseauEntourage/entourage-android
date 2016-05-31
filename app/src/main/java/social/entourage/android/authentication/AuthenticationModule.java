package social.entourage.android.authentication;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to Application
 * Providing Authentication related dependencies
 */
@Module
public class AuthenticationModule {

    @Provides
    @Singleton
    public AuthenticationController providesAuthenticationController(ComplexPreferences userSharedPref) {
        return new AuthenticationController(userSharedPref).init();
    }

    @Provides
    @Singleton
    public ComplexPreferences providesUserSharedPreferences(Application application) {
        return ComplexPreferences.getComplexPreferences(application, "userPref", Context.MODE_PRIVATE);
    }
}
