package social.entourage.android.api;

import android.support.v4.util.ArrayMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;

public interface UserRequest {

    @PATCH("users/me.json")
    Call<UserResponse> updateUser(@Body ArrayMap<String, Object> user);

    @PATCH("users/me/code.json")
    Call<UserResponse> regenerateSecretCode(@Body ArrayMap<String, Object> userInfo);
}