package social.entourage.android.map;

/**
 * Created by RPR on 25/03/15.
 */

import social.entourage.android.EntourageModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                MapActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class MapModule {
    private final MapActivity activity;

    public MapModule(final MapActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public MapActivity providesMapActivity() {
        return activity;
    }
}
