package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName


data class SurveyResponsesWrapper(
    @SerializedName("responses")
    val responses: List<Boolean>
)

data class SurveyResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("response") val response: Boolean
)

data class SurveyResponsesListWrapper(
    @SerializedName("survey_responses") val responses: List<SurveyResponse>
)

data class ChatMessageSurvey(
    val content: String,
    @SerializedName("survey_attributes")
    val surveyAttributes: SurveyAttributes? = null
)

data class SurveyAttributes(
    @SerializedName("choices")
    val choices: List<String>,
    @SerializedName("multiple")
    val multiple: Boolean
)
