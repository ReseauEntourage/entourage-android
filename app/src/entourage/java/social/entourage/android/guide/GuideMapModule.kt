package social.entourage.android.guide

import dagger.Module
import dagger.Provides

/**
 * Module related to GuideMapFragment
 * @see GuideMapFragment
 */
@Module
internal class GuideMapModule(private val fragment: GuideMapFragment) {
    @Provides
    fun providesGuideMapFragment(): GuideMapFragment {
        return fragment
    }

}