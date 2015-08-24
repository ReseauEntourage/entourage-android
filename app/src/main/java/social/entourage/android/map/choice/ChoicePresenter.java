package social.entourage.android.map.choice;

import javax.inject.Inject;

/**
 * Presenter controlling the ChoiceFragment
 * @see ChoiceFragment
 */
public class ChoicePresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final ChoiceFragment fragment;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public ChoicePresenter(final ChoiceFragment fragment) {
        this.fragment = fragment;
    }
}
