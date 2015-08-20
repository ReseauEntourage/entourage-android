package social.entourage.android.map.tour;

import javax.inject.Inject;

/**
 * Presenter controlling the TourInformationFragment
 * @see TourInformationFragment
 */
public class TourInformationPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final TourInformationFragment fragment;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public TourInformationPresenter(final TourInformationFragment fragment) {
        this.fragment = fragment;
    }
}
