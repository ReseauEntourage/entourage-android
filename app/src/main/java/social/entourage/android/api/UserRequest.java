package social.entourage.android.api;

import android.support.v4.util.ArrayMap;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.PATCH;

public interface UserRequest {

    @PATCH("/users/me.json")
    void updateUser(@Body ArrayMap<String, Object> user, Callback<UserResponse> callback);

    @PATCH("/users/me/code.json")
    void regenerateSecretCode(@Body ArrayMap<String, Object> userInfo, Callback<UserResponse> callback);
}