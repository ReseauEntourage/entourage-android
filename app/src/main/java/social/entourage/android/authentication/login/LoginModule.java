package social.entourage.android.authentication.login;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to LoginActivity
 * @see LoginActivity
 */
@Module
class LoginModule {

    private final LoginActivity activity;

    public LoginModule(final LoginActivity activity) {
        this.activity = activity;
    }

    @Provides
    public LoginActivity providesLoginActivity() {
        return activity;
    }
}
