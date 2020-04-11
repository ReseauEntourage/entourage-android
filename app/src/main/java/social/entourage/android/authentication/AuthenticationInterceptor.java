package social.entourage.android.authentication;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.HttpUrl;
import okhttp3.Response;
import social.entourage.android.BuildConfig;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

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

    @NonNull
    @Override
    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
        okhttp3.Request request = chain.request();
        HttpUrl url;

        if (!request.url().toString().startsWith(BuildConfig.ENTOURAGE_URL)) {
            return chain.proceed(request);
        }

        if (authenticationController.isAuthenticated()) {
            url = request.url().newBuilder().addQueryParameter("token", authenticationController.getUser().getToken()).build();
        } else {
            url = request.url().newBuilder().build();
        }

        request = request.newBuilder()
                .header("Accept", "application/json")
                .header("X-API-KEY", BuildConfig.API_KEY)
                .url(url).build();
        Response response = chain.proceed(request);

        if (response.code() == 401) {
            if (response.message() != null && response.message().equalsIgnoreCase("Unauthorized")) {
                BusProvider.INSTANCE.getInstance().post(new Events.OnUnauthorizedEvent());
            }
        }

        return response;
    }
}