package social.entourage.android.authentication;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class AuthenticationModule {

    @Provides
    @Singleton
    public AuthenticationController providesAuthenticationController(SharedPreferences userSharedPref) {
        return new AuthenticationController(userSharedPref).init();
    }

    @Provides
    @Singleton
    public SharedPreferences providesUserSharedPreferences(Application application) {
        return application.getSharedPreferences("userPref", Context.MODE_PRIVATE);
    }
}
