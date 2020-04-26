package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.VisitChatMessage;

/**
 * Created by Mihai Ionescu on 06/06/2018.
 */
public interface PrivateCircleRequest {

    @POST("entourages/{entourage_id}/chat_messages.json")
    Call<ChatMessage.ChatMessageWrapper> visitMessage(
            @Path("entourage_id") long entourageId,
            @Body VisitChatMessage.VisitChatMessageWrapper message
    );

}
