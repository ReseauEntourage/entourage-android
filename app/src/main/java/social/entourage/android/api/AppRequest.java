package social.entourage.android.api;

import retrofit.ResponseCallback;
import retrofit.http.GET;

public interface AppRequest {

    @GET("/check.json")
    void checkForUpdate(ResponseCallback callback);
}
