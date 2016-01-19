package social.entourage.android.user;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to UserFragment
 * @see UserFragment
 */
@Module
public class UserModule {
    private final UserFragment fragment;

    public UserModule(final UserFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public UserFragment providesUserFragment() {
        return fragment;
    }
}
