package social.entourage.android.api;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.POST;
import social.entourage.android.api.model.Newsletter;

public interface LoginRequest {

    //@Headers({"Accept: application/json"})
    @FormUrlEncoded
    @POST("/login.json")
    void login(@Field("phone") String phone,
               @Field("sms_code") String smsCode,
               @Field("device_type") String type,
               @Field("device_id") String id, Callback<LoginResponse> callback);

    //@Headers({"Accept: application/json"})
    @POST("/newsletter_subscriptions.json")
    void subscribeToNewsletter(@Body Newsletter.NewsletterWrapper newsletterWrapper, ResponseCallback callback);
}
