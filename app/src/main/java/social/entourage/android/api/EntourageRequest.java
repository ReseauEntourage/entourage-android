package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import social.entourage.android.api.model.map.Entourage;

/**
 * Created by mihaiionescu on 29/04/16.
 */
public interface EntourageRequest {

    @POST("entourages.json")
    Call<Entourage.EntourageWrapper> entourage(@Body Entourage.EntourageWrapper entourageWrapper);

}
