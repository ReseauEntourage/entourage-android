package social.entourage.android.authentication.login;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
        if (active && email != null && !email.equals("")) {
            fragment.startLoader();
            Newsletter newsletter = new Newsletter(email, active);
            Newsletter.NewsletterWrapper newsletterWrapper = new Newsletter.NewsletterWrapper(newsletter);
            Call<Newsletter.NewsletterWrapper> call = loginRequest.subscribeToNewsletter(newsletterWrapper);
            call.enqueue(new Callback<Newsletter.NewsletterWrapper>() {
                @Override
                public void onResponse(Call<Newsletter.NewsletterWrapper> call, Response<Newsletter.NewsletterWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.newsletterResult(true);
                    } else {
                        fragment.newsletterResult(false);
                    }
                }

                @Override
                public void onFailure(Call<Newsletter.NewsletterWrapper> call, Throwable t) {
                    fragment.newsletterResult(false);
                }
            });
        }
    }
}
