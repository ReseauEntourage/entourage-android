package social.entourage.android.api;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.PATCH;

public interface UserRequest {

    @Headers({"Accept: application/json"})
    @PATCH("/users/update_me.json")
    void updateUser(@Body HashMap<String, String> user, Callback<UserResponse> callback);

}
