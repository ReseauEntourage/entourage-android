package social.entourage.android.tour;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import social.entourage.android.api.model.map.Tour;

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

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void sendTour(Tour tour) {
        System.out.println("----- envoi de la maraude au webservice -----");
        //for (LatLng coord : tour.getCoordinates())
            //System.out.println("coord : " + coord.latitude + ", " + coord.longitude);
        // ici envoi
    }
}
