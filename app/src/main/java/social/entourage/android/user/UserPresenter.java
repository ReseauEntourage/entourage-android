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

    public User getAuthenticatedUser() {
        return authenticationController.getUser();
    }

    public void getUser(int userId) {
        Call<UserResponse> call = userRequest.getUser(userId);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(final Call<UserResponse> call, final Response<UserResponse> response) {
                if (response.isSuccess()) {
                    fragment.onUserReceived(response.body().getUser());
                }
                else {
                    fragment.onUserReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<UserResponse> call, final Throwable t) {
                fragment.onUserReceived(null);
            }
        });
    }

    public boolean isUserToursOnly() {
        return authenticationController.isUserToursOnly();
    }

    public void saveUserToursOnly(boolean choice) {
        authenticationController.saveUserToursOnly(choice);
    }

    public void updateUser(final User user) {
        if (fragment != null) {
            ArrayMap<String, Object> userMap = new ArrayMap<>();
            userMap.put("first_name", user.getFirstName());
            userMap.put("last_name", user.getLastName());
            if (user.getEmail() != null) {
                userMap.put("email", user.getEmail());
            }
            if (user.getSmsCode() != null) {
                userMap.put("sms_code", user.getSmsCode());
            }
            Call<UserResponse> call = userRequest.updateUser(userMap);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccess()) {
                        //update the logged user
                        authenticationController.saveUser(response.body().getUser());
                        authenticationController.saveUserPhoneAndCode(user.getPhone(), user.getSmsCode());
                        //inform the fragment
                        fragment.onUserUpdated(response.body().getUser());
                    }
                    else {
                        fragment.onUserUpdated(null);
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    fragment.onUserUpdated(null);
                }
            });
        }
    }

    public void deleteAccount() {
        if (fragment != null) {
            fragment.onDeletedAccount(false);
        }
    }
}
