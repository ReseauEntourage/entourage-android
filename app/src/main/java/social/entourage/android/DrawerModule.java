package social.entourage.android;

import dagger.Module;
import dagger.Provides;
import social.entourage.android.user.AvatarUpdatePresenter;
import social.entourage.android.user.AvatarUploadView;

/**
 * Module related to DrawerActivity
 */
@Module
public class DrawerModule {

    private final DrawerActivity activity;

    public DrawerModule(final DrawerActivity activity) {
        this.activity = activity;
    }

    @Provides
    public DrawerActivity providesActivity() {
        return activity;
    }

    @Provides
    public AvatarUploadView providesAvatarUploadView() { return activity; }

    @Provides
    public AvatarUpdatePresenter providesAvatarUpdatePresenter(DrawerPresenter presenter) { return presenter; }
}
