package social.entourage.android.tour;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by NTE on 06/07/15.
 */
@Singleton
public class TourServiceManager {

    private final TourService tourService;

    @Inject
    public TourServiceManager(final TourService tourService) {
        this.tourService = tourService;
    }
}
