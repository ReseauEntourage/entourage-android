package social.entourage.android.map.tour.my;

import javax.inject.Inject;

import social.entourage.android.api.TourRequest;

/**
 * Created by mihaiionescu on 10/03/16.
 */
public class MyToursPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MyToursFragment fragment;

    @Inject
    TourRequest tourRequest;

    // ----------------------------------
    // Constructor
    // ----------------------------------

    @Inject
    public MyToursPresenter(final MyToursFragment fragment) {
        this.fragment = fragment;
    }

    // ----------------------------------
    // Methods
    // ----------------------------------


}
