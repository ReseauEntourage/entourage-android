package social.entourage.android.authentication.login;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to LoginInformationFragment
 * @see LoginInformationFragment
 */
@Module
public class LoginInformationModule {
    private final LoginInformationFragment fragment;

    public LoginInformationModule(final LoginInformationFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public LoginInformationFragment providesLoginInformationFragment() {
        return fragment;
    }
}
