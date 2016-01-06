package social.entourage.android.authentication;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.RequestInterceptor;

/**
 * Retrofit interceptor that automatically add a params to the url when authenticated
 */
@Singleton
public class AuthenticationInterceptor implements RequestInterceptor {

    private final AuthenticationController authenticationController;

    @Inject
    public AuthenticationInterceptor(final AuthenticationController authenticationController) {
        this.authenticationController = authenticationController;
    }

    @Override
    public void intercept(final RequestFacade request) {
        request.addHeader("Accept", "application/json");
        request.addHeader("X-API-Key", "(required, string, `32e2ced9df89`)");
        if (authenticationController.isAuthenticated()) {
            request.addEncodedQueryParam("token", authenticationController.getUser().getToken());
        }
    }
}