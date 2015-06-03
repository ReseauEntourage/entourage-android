package social.entourage.android.guide;

/**
 * Created by FPE on 25/04/15.
 */

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import social.entourage.android.EntourageModule;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                GuideMapActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class GuideMapModule {
    private final GuideMapActivity activity;

    public GuideMapModule(final GuideMapActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public GuideMapActivity providesMapActivity() {
        return activity;
    }
}
