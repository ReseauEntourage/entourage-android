package social.entourage.android.user.edit;

import android.support.v4.util.ArrayMap;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Created by mihaiionescu on 01/11/16.
 */


public class UserEditPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final UserEditFragment fragment;
    private final UserRequest userRequest;
    private final AuthenticationController authenticationController;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Inject
    public UserEditPresenter(final UserEditFragment fragment,
                         final UserRequest userRequest,
                         final AuthenticationController authenticationController) {
        this.fragment = fragment;
        this.userRequest = userRequest;
        this.authenticationController = authenticationController;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

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
                    if (response.isSuccessful()) {
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

}
