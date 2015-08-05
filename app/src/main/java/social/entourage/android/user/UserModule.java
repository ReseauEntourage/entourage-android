package social.entourage.android.user;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to UserEntourageFragment
 * @see UserEntourageFragment
 */
@Module
public class UserModule {
    private final UserEntourageFragment fragment;

    public UserModule(final UserEntourageFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public UserEntourageFragment providesUserEntourageFragment() {
        return fragment;
    }
}
