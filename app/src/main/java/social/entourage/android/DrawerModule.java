package social.entourage.android;

import dagger.Module;
import dagger.Provides;

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
}
