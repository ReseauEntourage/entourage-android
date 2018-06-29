package social.entourage.android.api;

import java.util.Date;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.MultipleInvitations;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.TourUser;

public interface EntourageRequest {

    @POST("entourages.json")
    Call<Entourage.EntourageWrapper> createEntourage(
            @Body Entourage.EntourageWrapper entourageWrapper
    );

    @PATCH("entourages/{entourage_id}")
    Call<Entourage.EntourageWrapper> editEntourage(
            @Path("entourage_id") String entourageUUID,
            @Body Entourage.EntourageWrapper entourageWrapper
    );

    @GET("entourages/{entourage_id}")
    Call<Entourage.EntourageWrapper> retrieveEntourageById(
            @Path("entourage_id") String entourageUUID,
            @Query("distance") Integer distance,
            @Query("feed_rank") Integer feedRank
    );

    @GET("entourages/{entourage_id}")
    Call<Entourage.EntourageWrapper> retrieveEntourageByShareURL(
            @Path("entourage_id") String entourageShareURL
    );

    @PUT("entourages/{id}.json")
    Call<Entourage.EntourageWrapper> closeEntourage(
            @Path("id") String entourageUUID,
            @Body Entourage.EntourageWrapper entourageWrapper
    );

    @GET("entourages/{entourage_id}/users")
    Call<TourUser.TourUsersWrapper> retrieveEntourageUsers(
            @Path("entourage_id") String entourageUUID,
            @Query("context") String context
    );

    @POST("entourages/{entourage_id}/users")
    Call<TourUser.TourUserWrapper> requestToJoinEntourage(
            @Path("entourage_id") String entourageUUID,
            @Body Entourage.EntourageJoinInfo info
    );

    @PUT("entourages/{entourage_id}/users/{user_id}")
    Call<ResponseBody> updateUserEntourageStatus(
            @Path("entourage_id") String entourageUUID,
            @Path("user_id") int userId,
            @Body HashMap<String, Object> info
    );

    @DELETE("entourages/{entourage_id}/users/{user_id}")
    Call<TourUser.TourUserWrapper> removeUserFromEntourage(
            @Path("entourage_id") String entourageUUID,
            @Path("user_id") int userId
    );

    @POST("entourages/{entourage_id}/chat_messages.json")
    Call<ChatMessage.ChatMessageWrapper> chatMessage(
            @Path("entourage_id") String entourageUUID,
            @Body ChatMessage.ChatMessageWrapper message
    );

    @GET("entourages/{entourage_id}/chat_messages.json")
    Call<ChatMessage.ChatMessagesWrapper> retrieveEntourageMessages(
            @Path("entourage_id") String entourageUUID
    );

    @GET("entourages/{entourage_id}/chat_messages.json")
    Call<ChatMessage.ChatMessagesWrapper> retrieveEntourageMessages(
            @Path("entourage_id") String entourageUUID,
            @Query("before") Date pagination
    );

    @POST("entourages/{entourage_id}/invitations")
    Call<MultipleInvitations.MultipleInvitationsResponse> inviteBySMS(
            @Path("entourage_id") String entourageUUID,
            @Body MultipleInvitations.MultipleInvitationsWrapper invitations
            );

}
