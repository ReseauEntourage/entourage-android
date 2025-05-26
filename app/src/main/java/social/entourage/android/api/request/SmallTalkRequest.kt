package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.MembersWrapper
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.SmallTalk
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.api.model.UserSmallTalkRequestWithMatchDataWrapper

/*
 * -------------------------------------------------
 * Wrappers (API Entourage)
 * -------------------------------------------------
 */

class UserSmallTalkRequestWrapper(
    @field:SerializedName("user_smalltalk")
    val request: UserSmallTalkRequest
)

class UserSmallTalkRequestListWrapper(
    @field:SerializedName("user_smalltalks")
    val requests: MutableList<UserSmallTalkRequest>
)

class SmallTalkWrapper(
    @field:SerializedName("smalltalk")
    val smallTalk: SmallTalk
)

class SmallTalkListWrapper(
    @field:SerializedName("smalltalks")
    val smallTalks: MutableList<SmallTalk>
)

class ChatMessageListWrapper(
    @field:SerializedName("chat_messages")
    val messages: MutableList<Post>
)

class SmallTalkMatchResponse(
    @field:SerializedName("match")        val match: Boolean,
    @field:SerializedName("smalltalk_id") val smalltalkId: Int?
)

/*
 * -------------------------------------------------
 * Retrofit interface
 * -------------------------------------------------
 */

interface SmallTalkRequest {

    /* ---------- user_smalltalks ---------- */

    // Liste toutes les demandes (en cours + déjà matchées)
    @GET("user_smalltalks")
    fun listUserSmallTalkRequests(): Call<UserSmallTalkRequestListWrapper>

    // Affiche une demande précise (id ou uuid_v2)
    @GET("user_smalltalks/{id}")
    fun getUserSmallTalkRequest(@Path("id") id: String): Call<UserSmallTalkRequestWrapper>

    // Crée une nouvelle demande (422 si déjà une en cours)
    @POST("user_smalltalks")
    fun createUserSmallTalkRequest(
        @Body wrapper: UserSmallTalkRequestWrapper
    ): Call<UserSmallTalkRequestWrapper>

    // Met à jour la demande (elle doit être « en cours »)
    @PATCH("user_smalltalks/{id}")
    fun updateUserSmallTalkRequest(
        @Path("id") id: String,
        @Body params: ArrayMap<String, Any>
    ): Call<UserSmallTalkRequestWrapper>

    // Tente un match
    @POST("user_smalltalks/{id}/match")
    fun matchUserSmallTalkRequest(
        @Path("id") id: String
    ): Call<SmallTalkMatchResponse>

    // Supprime la demande (si pas encore matchée)
    @DELETE("user_smalltalks")
    fun deleteUserSmallTalkRequest(
    ): Call<ResponseBody>

    /* ---------- smalltalks (salles de chat) ---------- */

    // Liste des smalltalks où l’utilisateur participe
    @GET("smalltalks")
    fun listSmallTalks(
        @Query("page") page: Int? = null,
        @Query("per")  per:  Int? = null
    ): Call<SmallTalkListWrapper>

    // Détails d’un smalltalk
    @GET("smalltalks/{id}")
    fun getSmallTalk(
        @Path("id") id: String
    ): Call<SmallTalkWrapper>

    // Participants
    @GET("smalltalks/{id}/users")
    fun listSmallTalkParticipants(
        @Path("id") id: String
    ): Call<MembersWrapper>

    // Quitter la conversation
    @DELETE("smalltalks/{id}/users")
    fun leaveSmallTalk(
        @Path("id") id: String
    ): Call<ResponseBody>

    /* ---------- chat_messages ---------- */

    // Liste des messages
    @GET("smalltalks/{id}/chat_messages")
    fun listChatMessages(
        @Path("id") id: String,
        @Query("page") page: Int? = null,
        @Query("per")  per:  Int? = null
    ): Call<ChatMessageListWrapper>

    // Ajouter un message
    @POST("smalltalks/{id}/chat_messages")
    fun createChatMessage(
        @Path("id") id: String,
        @Body params: ArrayMap<String, Any>
    ): Call<ChatMessageWrapper>

    // Modifier un message
    @PATCH("smalltalks/{smalltalk_id}/chat_messages/{message_id}")
    fun updateChatMessage(
        @Path("smalltalk_id") smallTalkId: String,
        @Path("message_id")   messageId:  String,
        @Body params: ArrayMap<String, Any>
    ): Call<ChatMessageWrapper>

    // Supprimer un message
    @DELETE("smalltalks/{smalltalk_id}/chat_messages/{message_id}")
    fun deleteChatMessage(
        @Path("smalltalk_id") smallTalkId: String,
        @Path("message_id")   messageId:  String
    ): Call<ResponseBody>

    @GET("user_smalltalks/almost_matches")
    fun listAlmostMatches(
    ): Call<UserSmallTalkRequestWithMatchDataWrapper>

    @POST("smalltalks/{id}/chat_messages/presigned_upload")
    fun prepareAddPost(
        @Path("id") smallTalkId: String,
        @Body request: RequestContent
    ): Call<PrepareAddPostResponse>

    @POST("user_smalltalks/{id}/force_match")
    fun forceMatchUserSmallTalkRequest(
        @Path("id") id: String,
        @Query("smalltalk_id") smallTalkId: Int? = null      // nullable → param optionnel
    ): Call<SmallTalkMatchResponse>
}
