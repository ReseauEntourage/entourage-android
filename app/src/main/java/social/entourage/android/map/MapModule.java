package social.entourage.android.map;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module
public final class MapModule {
    private final MapActivity activity;

    public MapModule(final MapActivity activity) {
        this.activity = activity;
    }

    @Provides
    public MapActivity providesMapActivity() {
        return activity;
    }
}
