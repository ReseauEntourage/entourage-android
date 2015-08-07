package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface LoginRequest {

    @FormUrlEncoded
    @POST("/login.json")
    void login(@Field("phone") String phone,
               @Field("sms_code") String smsCode,
               @Field("device_type") String type,
               @Field("device_id") String id, Callback<LoginResponse> callback);
}
