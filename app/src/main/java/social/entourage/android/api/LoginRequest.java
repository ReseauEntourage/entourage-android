package social.entourage.android.api;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.http.Body;
import retrofit.http.POST;
import social.entourage.android.api.model.Newsletter;

public interface LoginRequest {

    @POST("/login.json")
    void login(@Body HashMap<String, String> user, Callback<LoginResponse> callback);

    @POST("/newsletter_subscriptions.json")
    void subscribeToNewsletter(@Body Newsletter.NewsletterWrapper newsletterWrapper, ResponseCallback callback);
}
