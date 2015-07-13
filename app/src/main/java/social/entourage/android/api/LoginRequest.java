package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface LoginRequest {

    @FormUrlEncoded
    @POST("/login.json")
    void login(@Field("email") String email, Callback<LoginResponse> callback);
}
