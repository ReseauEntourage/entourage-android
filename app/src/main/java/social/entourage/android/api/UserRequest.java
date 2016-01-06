package social.entourage.android.api;

import android.support.v4.util.ArrayMap;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.PATCH;

public interface UserRequest {

    //@Headers({"Accept: application/json"})
    @PATCH("/users/update_me.json")
    void updateUser(@Body HashMap<String, String> user, Callback<UserResponse> callback);

    //@Headers({"Accept: application/json"})
    @PATCH("/users/me/code.json")
    void regenerateSecretCode(@Body ArrayMap<String, Object> userInfo, Callback<UserResponse> callback);

}
