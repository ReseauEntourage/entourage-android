package social.entourage.android.tour

import dagger.Module
import dagger.Provides
import social.entourage.android.tour.TourInformationFragment

/**
 * Module related to TourInformationFragment
 * @see TourInformationFragment
 */
@Module
class TourInformationModule(private val fragment: TourInformationFragment) {
    @Provides
    fun providesTourInformationFragment(): TourInformationFragment {
        return fragment
    }

}