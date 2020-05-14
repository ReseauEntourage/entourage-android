package social.entourage.android.api;

import java.util.Date;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.tour.Encounter;
import social.entourage.android.api.model.tour.Tour;
import social.entourage.android.api.model.tour.TourJoinMessage;
import social.entourage.android.api.model.LocationPoint;
import social.entourage.android.api.model.EntourageUser;

public interface TourRequest {

    @POST("tours.json")
    Call<Tour.TourWrapper> tour(
            @Body Tour.TourWrapper tourWrapper
    );

    @POST("tours/{tour_id}/tour_points.json")
    Call<Tour.TourWrapper> tourPoints(
            @Path("tour_id") String tourUUID,
            @Body LocationPoint.TourPointWrapper points
    );

    @PUT("tours/{id}.json")
    Call<Tour.TourWrapper> closeTour(
            @Path("id") String tourUUID,
            @Body Tour.TourWrapper tourWrapper
    );

    @GET("tours.json")
    Call<Tour.ToursWrapper> retrieveToursNearby(
            @Query("limit") int limit,
            @Query("type") String type,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("distance") double distance,
            @Query("status") String status
    );

    @GET("users/{user_id}/tours.json")
    Call<Tour.ToursWrapper> retrieveToursByUserId(
            @Path("user_id") int userId,
            @Query("page") int page,
            @Query("per") int per
    );

    @GET("users/{user_id}/tours.json")
    Call<Tour.ToursWrapper> retrieveToursByUserIdAndStatus(
            @Path("user_id") int userId,
            @Query("page") int page,
            @Query("per") int per,
            @Query("status") String status
    );

    @GET("users/{user_id}/tours.json")
    Call<Tour.ToursWrapper> retrieveToursByUserIdAndPoint(
            @Path("user_id") int userId,
            @Query("page") int page,
            @Query("per") int per,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("distance") double distance
    );

    @GET("tours/{tour_id}")
    Call<Tour.TourWrapper> retrieveTourById(
            @Path("tour_id") String tourUUID
    );

    @GET("tours/{tour_id}/users.json")
    Call<EntourageUser.EntourageUsersWrapper> retrieveTourUsers(
            @Path("tour_id") String tourUUID
    );

    @GET("tours/{tour_id}/users.json")
    Call<EntourageUser.EntourageUsersWrapper> retrieveTourUsers(
            @Path("tour_id") String tourUUID,
            @Query("page") int page,
            @Query("per") int per
    );

    @POST("tours/{tour_id}/chat_messages.json")
    Call<ChatMessage.ChatMessageWrapper> chatMessage(
            @Path("tour_id") String tourUUID,
            @Body ChatMessage.ChatMessageWrapper message
    );

    @GET("tours/{tour_id}/chat_messages.json")
    Call<ChatMessage.ChatMessagesWrapper> retrieveTourMessages(
            @Path("tour_id") String tourUUID
    );

    @GET("tours/{tour_id}/chat_messages.json")
    Call<ChatMessage.ChatMessagesWrapper> retrieveTourMessages(
            @Path("tour_id") String tourUUID,
            @Query("before") Date pagination
    );

    @GET("tours/{tour_id}/encounters.json")
    Call<Encounter.EncountersWrapper> retrieveTourEncounters(
            @Path("tour_id") String tourUUID
    );

    @POST("tours/{tour_id}/users")
    Call<EntourageUser.EntourageUserWrapper> requestToJoinTour(
            @Path("tour_id") String tourUUID
    );

    @PUT("tours/{tour_id}/users/{user_id}")
    Call<EntourageUser.EntourageUserWrapper> updateJoinTourMessage(
            @Path("tour_id") String tourUUID,
            @Path("user_id") int userId,
            @Body TourJoinMessage.TourJoinMessageWrapper messageWrapper
    );

    @PUT("tours/{tour_id}/users/{user_id}")
    Call<ResponseBody> updateUserTourStatus(
            @Path("tour_id") String tourUUID,
            @Path("user_id") int userId,
            @Body HashMap<String, Object> user
    );

    @DELETE("tours/{tour_id}/users/{user_id}")
    Call<EntourageUser.EntourageUserWrapper> removeUserFromTour(
            @Path("tour_id") String tourUUID,
            @Path("user_id") int userId
    );
}