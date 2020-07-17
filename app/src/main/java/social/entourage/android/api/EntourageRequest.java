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
import social.entourage.android.api.model.EntourageReport;
import social.entourage.android.api.model.MultipleInvitations;
import social.entourage.android.api.model.BaseEntourage;
import social.entourage.android.api.model.EntourageUser;

public interface EntourageRequest {

    @POST("entourages.json")
    Call<BaseEntourage.EntourageWrapper> createEntourage(
            @Body BaseEntourage.EntourageWrapper entourageWrapper
    );

    @PATCH("entourages/{entourage_id}")
    Call<BaseEntourage.EntourageWrapper> editEntourage(
            @Path("entourage_id") String entourageUUID,
            @Body BaseEntourage.EntourageWrapper entourageWrapper
    );

    @GET("entourages/{entourage_id}")
    Call<BaseEntourage.EntourageWrapper> retrieveEntourageById(
            @Path("entourage_id") String entourageUUID,
            @Query("distance") Integer distance,
            @Query("feed_rank") Integer feedRank
    );

    @GET("entourages/{entourage_id}")
    Call<BaseEntourage.EntourageWrapper> retrieveEntourageByShareURL(
            @Path("entourage_id") String entourageShareURL
    );

    @PUT("entourages/{id}")
    Call<BaseEntourage.EntourageWrapper> closeEntourage(
            @Path("id") String entourageUUID,
            @Body BaseEntourage.EntourageWrapper entourageWrapper
    );

    @GET("entourages/{entourage_id}/users")
    Call<EntourageUser.EntourageUserListResponse> retrieveEntourageUsers(
            @Path("entourage_id") String entourageUUID,
            @Query("context") String context
    );

    @POST("entourages/{entourage_id}/users")
    Call<EntourageUser.EntourageUserWrapper> requestToJoinEntourage(
            @Path("entourage_id") String entourageUUID,
            @Body BaseEntourage.EntourageJoinInfo info
    );

    @PUT("entourages/{entourage_id}/users/{user_id}")
    Call<ResponseBody> updateUserEntourageStatus(
            @Path("entourage_id") String entourageUUID,
            @Path("user_id") int userId,
            @Body HashMap<String, Object> info
    );

    @DELETE("entourages/{entourage_id}/users/{user_id}")
    Call<EntourageUser.EntourageUserWrapper> removeUserFromEntourage(
            @Path("entourage_id") String entourageUUID,
            @Path("user_id") int userId
    );

    @POST("entourages/{entourage_id}/chat_messages.json")
    Call<ChatMessage.ChatMessageWrapper> chatMessage(
            @Path("entourage_id") String entourageUUID,
            @Body ChatMessage.ChatMessageWrapper message
    );

    @GET("entourages/{entourage_id}/chat_messages.json")
    Call<ChatMessage.ChatMessageListResponse> retrieveEntourageMessages(
            @Path("entourage_id") String entourageUUID
    );

    @GET("entourages/{entourage_id}/chat_messages.json")
    Call<ChatMessage.ChatMessageListResponse> retrieveEntourageMessages(
            @Path("entourage_id") String entourageUUID,
            @Query("before") Date pagination
    );

    @POST("entourages/{entourage_id}/invitations")
    Call<MultipleInvitations.MultipleInvitationsResponse> inviteBySMS(
            @Path("entourage_id") String entourageUUID,
            @Body MultipleInvitations.MultipleInvitationsWrapper invitations
            );

    @POST("entourages/{entourage_id}/report")
    Call<ResponseBody> reportEntourage(@Path("entourage_id") int userId, @Body EntourageReport.EntourageReportWrapper entourageReportWrapper);
}
