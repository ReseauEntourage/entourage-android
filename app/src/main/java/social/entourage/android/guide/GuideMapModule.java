package social.entourage.android.guide;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to GuideMapEntourageFragment
 * @see GuideMapEntourageFragment
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
