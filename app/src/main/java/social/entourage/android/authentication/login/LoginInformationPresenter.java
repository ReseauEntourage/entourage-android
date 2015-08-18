package social.entourage.android.authentication.login;

import javax.inject.Inject;

import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.model.Newsletter;

/**
 * Presenter controlling the LoginInformationFragment
 * @see LoginInformationFragment
 */
public class LoginInformationPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final LoginInformationFragment fragment;
    private final LoginRequest loginRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public LoginInformationPresenter(final LoginInformationFragment fragment,
                                     final LoginRequest loginRequest) {
        this.fragment = fragment;
        this.loginRequest = loginRequest;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void subscribeToNewsletter(final String email, final boolean active) {
        if (active && email != null) {
            Newsletter newsletter = new Newsletter(email, active);
            Newsletter.NewsletterWrapper newsletterWrapper = new Newsletter.NewsletterWrapper(newsletter);
            loginRequest.subscribeToNewsletter(newsletterWrapper, new ResponseCallback() {
                @Override
                public void success(Response response) {
                    fragment.newsletterResult(true);
                }

                @Override
                public void failure(RetrofitError error) {
                    fragment.newsletterResult(false);
                }
            });
        }
    }
}
