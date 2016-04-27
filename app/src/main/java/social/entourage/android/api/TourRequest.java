package social.entourage.android.api;

import android.util.ArrayMap;

import com.squareup.okhttp.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourJoinMessage;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;

public interface TourRequest {

    @POST("tours.json")
    Call<Tour.TourWrapper> tour(@Body Tour.TourWrapper tourWrapper);

    @POST("tours/{tour_id}/tour_points.json")
    Call<Tour.TourWrapper> tourPoints(@Path("tour_id") long tourId, @Body TourPoint.TourPointWrapper points);

    @PUT("tours/{id}.json")
    Call<Tour.TourWrapper> closeTour(@Path("id") long tourId, @Body Tour.TourWrapper tourWrapper);

    @GET("tours.json")
    Call<Tour.ToursWrapper> retrieveToursNearby(@Query("limit") int limit,
                             @Query("type") String type,
                             @Query("vehicle_type") String vehicleType,
                             @Query("latitude") double latitude,
                             @Query("longitude") double longitude,
                             @Query("distance") double distance);

    @GET("users/{user_id}/tours.json")
    Call<Tour.ToursWrapper> retrieveToursByUserId(@Path("user_id") int userId,
                                                  @Query("page") int page,
                                                  @Query("per") int per);

    @GET("users/{user_id}/tours.json")
    Call<Tour.ToursWrapper> retrieveToursByUserIdAndStatus(@Path("user_id") int userId,
                               @Query("page") int page,
                               @Query("per") int per,
                               @Query("status") String status);

    @GET("users/{user_id}/tours.json")
    Call<Tour.ToursWrapper> retrieveToursByUserIdAndPoint(@Path("user_id") int userId,
                                       @Query("page") int page,
                                       @Query("per") int per,
                                       @Query("latitude") double latitude,
                                       @Query("longitude") double longitude,
                                       @Query("distance") double distance);

    @GET("tours/{tour_id}")
    Call<Tour.TourWrapper> retrieveTourById(
            @Path("tour_id") long tourId
    );

    @GET("tours/{tour_id}/users.json")
    Call<TourUser.TourUsersWrapper> retrieveTourUsers(
            @Path("tour_id") long tourId
    );

    @GET("tours/{tour_id}/users.json")
    Call<TourUser.TourUsersWrapper> retrieveTourUsers(
            @Path("tour_id") long tourId,
            @Query("page") int page,
            @Query("per") int per
    );

    @POST("tours/{tour_id}/chat_messages.json")
    Call<ChatMessage.ChatMessageWrapper> chatMessage(
            @Path("tour_id") long tourId,
            @Body ChatMessage.ChatMessageWrapper message
    );

    @GET("tours/{tour_id}/chat_messages.json")
    Call<ChatMessage.ChatMessagesWrapper> retrieveTourMessages(
            @Path("tour_id") long tourId
    );

    @GET("tours/{tour_id}/chat_messages.json")
    Call<ChatMessage.ChatMessagesWrapper> retrieveTourMessages(
            @Path("tour_id") long tourId,
            @Query("before") Date pagination
    );

    @GET("tours/{tour_id}/encounters.json")
    Call<Encounter.EncountersWrapper> retrieveTourEncounters(
            @Path("tour_id") long tourId
    );

    @POST("tours/{tour_id}/users")
    Call<TourUser.TourUserWrapper> requestToJoinTour(
            @Path("tour_id") long tourId
    );

    @PUT("tours/{tour_id}/users")
    Call<TourUser.TourUserWrapper> updateJoinTourMessage(
            @Path("tour_id") long tourId,
            @Body TourJoinMessage.TourJoinMessageWrapper messageWrapper
    );

    @PUT("tours/{tour_id}/users/{user_id}")
    Call<ResponseBody> updateUserTourStatus(
            @Path("tour_id") long tourId,
            @Path("user_id") int userId,
            @Body HashMap<String, Object> user
    );

    @DELETE("tours/{tour_id}/users/{user_id}")
    Call<TourUser.TourUserWrapper> removeUserFromTour(
            @Path("tour_id") long tourId,
            @Path("user_id") int userId
    );
}