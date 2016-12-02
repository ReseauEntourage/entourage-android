package social.entourage.android.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import social.entourage.android.api.model.ApplicationInfo;

public interface AppRequest {

    @GET("check.json")
    Call<ResponseBody> checkForUpdate();

    @PUT("applications.json")
    Call<ResponseBody> updateApplicationInfo(
            @Body ApplicationInfo.ApplicationWrapper applicationWrapper
    );
}
