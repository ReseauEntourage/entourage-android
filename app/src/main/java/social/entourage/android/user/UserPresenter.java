package social.entourage.android.user;

import javax.inject.Inject;

/**
 * Presenter controlling the UserEntourageFragment
 * @see UserEntourageFragment
 */
public class UserPresenter {

    private final UserEntourageFragment activity;

    @Inject
    public UserPresenter(final UserEntourageFragment activity) {
        this.activity = activity;
    }
}
