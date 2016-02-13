package social.entourage.android.api;

import com.squareup.okhttp.ResponseBody;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AppRequest {

    @GET("check.json")
    Call<ResponseBody> checkForUpdate();
}
