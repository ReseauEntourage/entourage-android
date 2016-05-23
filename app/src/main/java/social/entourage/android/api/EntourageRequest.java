package social.entourage.android.api;

import com.squareup.okhttp.ResponseBody;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @PUT("entourages/{id}.json")
    Call<Entourage.EntourageWrapper> closeEntourage(@Path("id") long tourId, @Body Entourage.EntourageWrapper entourageWrapper);

    @POST("entourages/{entourage_id}/users")
    Call<TourUser.TourUserWrapper> requestToJoinEntourage(
            @Path("entourage_id") long entourageId
    );

    @PUT("entourages/{entourage_id}/users/{user_id}")
    Call<ResponseBody> updateUserEntourageStatus(
            @Path("entourage_id") long entourageId,
            @Path("user_id") int userId,
            @Body HashMap<String, Object> user
    );

    @DELETE("entourages/{entourage_id}/users/{user_id}")
    Call<TourUser.TourUserWrapper> removeUserFromEntourage(
            @Path("entourage_id") long entourageId,
            @Path("user_id") int userId
    );

}
