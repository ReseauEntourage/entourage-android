package social.entourage.android;

import dagger.Module;
import dagger.Provides;
import social.entourage.android.user.AvatarUpdatePresenter;
import social.entourage.android.user.AvatarUploadView;

/**
 * Module related to MainActivity
 */
@Module
public class MainModule {

    private final MainActivity activity;

    public MainModule(final MainActivity activity) {
        this.activity = activity;
    }

    @Provides
    public MainActivity providesActivity() {
        return activity;
    }

    @Provides
    public AvatarUploadView providesAvatarUploadView() { return activity; }

    @Provides
    public AvatarUpdatePresenter providesAvatarUpdatePresenter(MainPresenter presenter) { return presenter; }
}
