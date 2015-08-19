package social.entourage.android.user;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.R;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Presenter controlling the UserActivity
 * @see UserActivity
 */
public class UserPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final UserActivity activity;
    private final UserRequest userRequest;
    private final AuthenticationController authenticationController;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Inject
    public UserPresenter(final UserActivity activity,
                         final UserRequest userRequest,
                         final AuthenticationController authenticationController) {
        this.activity = activity;
        this.userRequest = userRequest;
        this.authenticationController = authenticationController;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void updateUser(final String email, final String code) {
        if (activity != null) {
            activity.startLoader();
            HashMap<String, String> user = new HashMap<>();
            if (email != null) {
                user.put("email", email);
            }
            if (code != null) {
                user.put("sms_code", code);
            }
            userRequest.updateUser(user, new Callback<UserResponse>() {
                @Override
                public void success(UserResponse userResponse, Response response) {
                    activity.displayToast(activity.getString(R.string.user_text_update_ok));
                    activity.updateView(userResponse.getUser().getEmail());
                }

                @Override
                public void failure(RetrofitError error) {
                    activity.displayToast(activity.getString(R.string.user_text_update_ko));
                    activity.resetLoginButton();
                }
            });
        }
    }
}
