package social.entourage.android.authentication.login;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to LoginEntourageFragment
 * @see LoginEntourageFragment
 */
@Module
class LoginModule {

    private final LoginEntourageFragment fragment;

    public LoginModule(final LoginEntourageFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public LoginEntourageFragment providesLoginEntourageFragment() {
        return fragment;
    }
}
