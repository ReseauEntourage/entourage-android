package social.entourage.android.guide;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to GuideMapFragment
 * @see GuideMapFragment
 */
@Module
final class GuideMapModule {
    private final GuideMapFragment fragment;

    public GuideMapModule(final GuideMapFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public GuideMapFragment providesGuideMapFragment() {
        return fragment;
    }
}
