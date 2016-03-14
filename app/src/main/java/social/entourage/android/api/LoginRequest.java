package social.entourage.android.api;

import com.squareup.okhttp.ResponseBody;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import social.entourage.android.api.model.Newsletter;

public interface LoginRequest {

    @POST("login.json")
    Call<LoginResponse> login(@Body HashMap<String, String> user);

    @POST("newsletter_subscriptions.json")
    Call<Newsletter.NewsletterWrapper> subscribeToNewsletter(@Body Newsletter.NewsletterWrapper newsletterWrapper);
}
