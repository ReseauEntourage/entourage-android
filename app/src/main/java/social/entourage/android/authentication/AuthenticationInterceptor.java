package social.entourage.android.authentication;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.HttpUrl;

/**
 * Retrofit interceptor that automatically add a params to the url when authenticated
 */
@Singleton
public class AuthenticationInterceptor implements okhttp3.Interceptor {

    private final AuthenticationController authenticationController;

    @Inject
    public AuthenticationInterceptor(final AuthenticationController authenticationController) {
        this.authenticationController = authenticationController;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        okhttp3.Request request = chain.request();
        HttpUrl url;
        if (authenticationController.isAuthenticated()) {
            url = request.url().newBuilder().addQueryParameter("token", authenticationController.getUser().getToken()).build();
        } else {
            url = request.url().newBuilder().build();
        }
        request = request.newBuilder()
                .header("Accept", "application/json")
                .header("X-API-Key", "32e2ced9df89")
                .url(url).build();
        return chain.proceed(request);
    }
}