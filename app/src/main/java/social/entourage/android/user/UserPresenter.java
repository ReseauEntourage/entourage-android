package social.entourage.android.user;

import android.support.v4.util.ArrayMap;

import java.util.HashMap;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    public User getAuthentificatedUser() {
        return authenticationController.getUser();
    }

    public void getUser(int userId) {

    }

    public boolean isUserToursOnly() {
        return authenticationController.isUserToursOnly();
    }

    public void saveUserToursOnly(boolean choice) {
        authenticationController.saveUserToursOnly(choice);
    }

    public void updateUser(String email, String code) {
        if (fragment != null) {
            ArrayMap<String, Object> user = new ArrayMap<>();
            if (email != null) {
                user.put("email", email);
            }
            if (code != null) {
                user.put("sms_code", code);
            }
            Call<UserResponse> call = userRequest.updateUser(user);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccess()) {
                        fragment.displayToast(fragment.getString(R.string.user_text_update_ok));
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    fragment.displayToast(fragment.getString(R.string.user_text_update_ko));
                }
            });
        }
    }
}
