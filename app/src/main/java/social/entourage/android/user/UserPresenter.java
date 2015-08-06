package social.entourage.android.user;

import javax.inject.Inject;

/**
 * Presenter controlling the UserActivity
 * @see UserActivity
 */
public class UserPresenter {

    private final UserActivity activity;

    @Inject
    public UserPresenter(final UserActivity activity) {
        this.activity = activity;
    }
}
