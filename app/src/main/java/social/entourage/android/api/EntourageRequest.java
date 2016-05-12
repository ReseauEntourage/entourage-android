package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.TourUser;

/**
 * Created by mihaiionescu on 29/04/16.
 */
public interface EntourageRequest {

    @POST("entourages.json")
    Call<Entourage.EntourageWrapper> entourage(
            @Body Entourage.EntourageWrapper entourageWrapper
    );

    @POST("entourages/{entourage_id}/users")
    Call<TourUser.TourUserWrapper> requestToJoinEntourage(
            @Path("entourage_id") long entourage_id
    );

}
