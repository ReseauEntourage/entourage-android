package social.entourage.android.user;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to UserActivity
 * @see UserActivity
 */
@Module
public class UserModule {
    private final UserActivity activity;

    public UserModule(final UserActivity activity) {
        this.activity = activity;
    }

    @Provides
    public UserActivity providesUserActivity() {
        return activity;
    }
}
