package social.entourage.android.authentification.login;


import javax.inject.Inject;

import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.login.BaseLoginPresenter;
import social.entourage.android.authentication.login.LoginActivity;

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
public class LoginPresenter extends BaseLoginPresenter {

    @Inject
    public LoginPresenter(
            final LoginActivity activity,
            final LoginRequest loginRequest,
            final UserRequest userRequest,
            final AuthenticationController authenticationController) {
        super(activity, loginRequest, userRequest, authenticationController);
    }

    protected boolean shouldShowChoosePhotoAfterLogin() {
        User me = authenticationController.getUser();
        return (me != null && (me.getAvatarURL() == null || me.getAvatarURL().length() == 0));
    }
}
