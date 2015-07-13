package social.entourage.android.map;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to MapActivity
 * @see MapActivity
 */
@Module
final class MapModule {
    private final MapActivity activity;

    public MapModule(final MapActivity activity) {
        this.activity = activity;
    }

    @Provides
    public MapActivity providesMapActivity() {
        return activity;
    }
}
