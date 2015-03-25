package social.entourage.android.login;


import social.entourage.android.EntourageModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                LoginActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public class LoginModule {

    private final LoginActivity activity;

    public LoginModule(final LoginActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public LoginActivity providesLoginActivity() {
        return activity;
    }
}
