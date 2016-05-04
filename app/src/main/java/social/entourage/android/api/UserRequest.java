package social.entourage.android.api;

import android.support.v4.util.ArrayMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface UserRequest {

    @PATCH("users/me.json")
    Call<UserResponse> updateUser(@Body ArrayMap<String, Object> user);

    @PATCH("users/me/code.json")
    Call<UserResponse> regenerateSecretCode(@Body ArrayMap<String, Object> userInfo);

    @GET("users/{user_id}")
    Call<UserResponse> getUser(@Path("user_id") int userId);

    @DELETE("users/me.json")
    Call<UserResponse> deleteUser();
}