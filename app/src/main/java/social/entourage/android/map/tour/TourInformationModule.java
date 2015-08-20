package social.entourage.android.map.tour;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to TourInformationFragment
 * @see TourInformationFragment
 */
@Module
public class TourInformationModule {
    private final TourInformationFragment fragment;

    public TourInformationModule(final TourInformationFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public TourInformationFragment providesTourInformationFragment() {
        return fragment;
    }
}
