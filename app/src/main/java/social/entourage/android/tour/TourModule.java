package social.entourage.android.tour;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to TourService
 * @see TourService
 */
@Module
class TourModule {

    private final TourService service;

    public TourModule(final TourService service) {
        this.service = service;
    }

    @Provides
    public TourService providesTourService() {
        return service;
    }
}
