package social.entourage.android.user.edit;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to UserEditFragment
 * Created by mihaiionescu on 01/11/16.
 */

@Module
public class UserEditModule {
    private final UserEditFragment fragment;

    public UserEditModule(final UserEditFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public UserEditFragment providesUserEditFragment() {
        return fragment;
    }
}
