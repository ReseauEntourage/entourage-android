package social.entourage.android.api.request

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import social.entourage.android.api.model.ChatMessageSurvey
import social.entourage.android.api.model.SurveyResponsesListWrapper
import social.entourage.android.api.model.SurveyResponsesWrapper

interface SurveyRequest {
    @POST("neighborhoods/{neighborhood_id}/chat_messages/{chat_message_id}/survey_responses")
    fun postSurveyResponseGroup(
        @Path("neighborhood_id") neighborhoodId: Int,
        @Path("chat_message_id") chatMessageId: Int,
        @Body surveyResponse: SurveyResponsesWrapper
    ): Call<ResponseBody>

    @GET("neighborhoods/{neighborhood_id}/chat_messages/{chat_message_id}/survey_responses")
    fun getSurveyResponsesForGroup(
        @Path("neighborhood_id") neighborhoodId: Int,
        @Path("chat_message_id") chatMessageId: Int
    ): Call<SurveyResponsesListWrapper>

    @DELETE("neighborhoods/{neighborhood_id}/chat_messages/{chat_message_id}/survey_responses")
    fun deleteSurveyResponseForGroup(
        @Path("neighborhood_id") neighborhoodId: Int,
        @Path("chat_message_id") chatMessageId: Int
    ): Call<ResponseBody>


    @POST("outings/{event_id}/chat_messages/{chat_message_id}/survey_responses")
    fun postSurveyResponseEvent(
        @Path("event_id") eventId: Int,
        @Path("chat_message_id") chatMessageId: Int,
        @Body surveyResponse: SurveyResponsesWrapper
    ): Call<ResponseBody>

    @POST("neighborhoods/{neighborhood_id}/chat_messages")
    fun createSurveyInGroup(
        @Path("neighborhood_id") neighborhoodId: Int,
        @Body chatMessage: ChatMessageSurvey
    ): Call<ResponseBody>


    @GET("outings/{outing_id}/chat_messages/{chat_message_id}/survey_responses")
    fun getSurveyResponsesForEvent(
        @Path("outing_id") outingId: Int,
        @Path("chat_message_id") chatMessageId: Int
    ): Call<SurveyResponsesListWrapper> // Utilisez un wrapper adapté ici également

    @DELETE("outings/{outing_id}/chat_messages/{chat_message_id}/survey_responses")
    fun deleteSurveyResponseForEvent(
        @Path("outing_id") outingId: Int,
        @Path("chat_message_id") chatMessageId: Int
    ): Call<ResponseBody>
    @POST("outings/{outing_id}/chat_messages")
    fun createSurveyInEvent(
        @Path("outing_id") outingId: Int,
        @Body chatMessage: ChatMessageSurvey
    ): Call<ResponseBody>





}