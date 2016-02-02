package social.entourage.android.user;

import android.location.Location;
import android.support.v4.util.ArrayMap;

import java.util.HashMap;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.R;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Presenter controlling the UserFragment
 * @see UserFragment
 */
public class UserPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final UserFragment fragment;
    private final UserRequest userRequest;
    private final AuthenticationController authenticationController;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Inject
    public UserPresenter(final UserFragment fragment,
                         final UserRequest userRequest,
                         final AuthenticationController authenticationController) {
        this.fragment = fragment;
        this.userRequest = userRequest;
        this.authenticationController = authenticationController;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public User getUser() {
        return authenticationController.getUser();
    }

    public boolean isUserToursOnly() {
        return authenticationController.isUserToursOnly();
    }

    public void saveUserToursOnly(boolean choice) {
        authenticationController.saveUserToursOnly(choice);
    }
}
