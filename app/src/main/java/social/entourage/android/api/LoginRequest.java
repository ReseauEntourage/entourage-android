package social.entourage.android.api;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginRequest {

    @POST("login.json")
    Call<LoginResponse> login(@Body HashMap<String, String> user);
}
