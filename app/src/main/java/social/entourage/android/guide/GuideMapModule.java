package social.entourage.android.guide;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to GuideMapActivity
 * @see GuideMapActivity
 */
@Module
final class GuideMapModule {
    private final GuideMapEntourageFragment fragment;

    public GuideMapModule(final GuideMapEntourageFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public GuideMapEntourageFragment providesGuideMapEntourageFragment() {
        return fragment;
    }
}
