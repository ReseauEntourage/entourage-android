package social.entourage.android.tour;

import dagger.Module;
import dagger.Provides;

@Module
public class TourModule {

    private final TourService service;

    public TourModule(final TourService service) {
        this.service = service;
    }

    @Provides
    public TourService providesTourService() {
        return service;
    }
}
