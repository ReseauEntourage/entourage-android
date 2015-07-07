package social.entourage.android.tour;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import social.entourage.android.EntourageModule;

/**
 * Created by NTE on 07/07/15.
 */
@Module(
        injects = {
                TourService.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public class TourModule {

    private final TourService service;

    public TourModule(final TourService service) {
        this.service = service;
    }

    @Provides
    @Singleton
    public TourService providesTourService() {
        return service;
    }
}
