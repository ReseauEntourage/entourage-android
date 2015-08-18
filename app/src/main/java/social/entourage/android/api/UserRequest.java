package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.Headers;
import retrofit.http.PATCH;
import social.entourage.android.api.wrapper.UserWrapper;

public interface UserRequest {

    @Headers({"Accept: application/json"})
    @PATCH("/users/update_me.json")
    void updateUser(@Body UserWrapper userWrapper, Callback<UserResponse> callback);

}
