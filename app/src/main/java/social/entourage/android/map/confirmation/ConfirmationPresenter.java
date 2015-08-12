package social.entourage.android.map.confirmation;

import javax.inject.Inject;

/**
 * Presenter conrolling the ConfirmationActivity
 * @see ConfirmationActivity
 */
public class ConfirmationPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final ConfirmationActivity activity;

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @Inject
    public ConfirmationPresenter(final ConfirmationActivity activity) {
        this.activity = activity;
    }

}
