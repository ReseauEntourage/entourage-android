package social.entourage.android.user;

import javax.inject.Inject;

import social.entourage.android.api.UserRequest;

/**
 * Presenter controlling the UserActivity
 * @see UserActivity
 */
public class UserPresenter {

    private final UserActivity activity;

    private final UserRequest userRequest;

    @Inject
    public UserPresenter(final UserActivity activity, final UserRequest userRequest) {
        this.activity = activity;
        this.userRequest = userRequest;
    }
}
