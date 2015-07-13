package social.entourage.android.guide;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module
public final class GuideMapModule {
    private final GuideMapActivity activity;

    public GuideMapModule(final GuideMapActivity activity) {
        this.activity = activity;
    }

    @Provides
    public GuideMapActivity providesMapActivity() {
        return activity;
    }
}
