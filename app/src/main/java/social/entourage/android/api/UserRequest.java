package social.entourage.android.api;

import android.support.v4.util.ArrayMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.UserReport;

public interface UserRequest {

    @PATCH("users/me.json")
    Call<UserResponse> updateUser(@Body ArrayMap<String, Object> user);

    @PATCH("users/me/code.json")
    Call<UserResponse> regenerateSecretCode(@Body ArrayMap<String, Object> userInfo);

    @GET("users/{user_id}")
    Call<UserResponse> getUser(@Path("user_id") int userId);

    @DELETE("users/me.json")
    Call<UserResponse> deleteUser();

    @POST("users")
    Call<UserResponse> registerUser(@Body ArrayMap<String, Object> userInfo);

    @POST("users/{user_id}/report")
    Call<ResponseBody> reportUser(@Path("user_id") int userId, @Body UserReport.UserReportWrapper userReportWrapper);

    @POST("users/{user_id}/partners")
    Call<Partner.PartnerWrapper> addPartner(@Path("user_id") int userId, @Body Partner.PartnerWrapper partner);

    @DELETE("users/{user_id}/partners/{partner_id}")
    Call<ResponseBody> removePartnerFromUser(@Path("user_id") int userId, @Path("partner_id") long partnerId);

    @PUT("users/{user_id}/partners/{partner_id}")
    Call<Partner.PartnerWrapper> updatePartner(@Path("user_id") int userId, @Path("partner_id") long partnerId, @Body Partner.PartnerWrapper partner);

    @POST("users/me/address")
    Call<User.AddressWrapper> updateAddress(@Body User.AddressWrapper addressWrapper);

}