package social.entourage.android.login;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module
public class LoginModule {

    private final LoginActivity activity;

    public LoginModule(final LoginActivity activity) {
        this.activity = activity;
    }

    @Provides
    public LoginActivity providesLoginActivity() {
        return activity;
    }
}
