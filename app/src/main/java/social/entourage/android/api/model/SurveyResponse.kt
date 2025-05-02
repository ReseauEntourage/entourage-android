package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName


data class SurveyResponsesWrapper(
    @SerializedName("responses")
    val responses: List<Boolean>
)

data class SurveyResponseUser(
    @SerializedName("id") val id: Int,
    @SerializedName("lang") val lang: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("community_roles") val communityRoles: List<String>
)

data class SurveyResponse(
    val users: List<SurveyResponseUser>
)

data class SurveyResponsesListWrapper(
    @SerializedName("survey_responses") val responses: List<List<SurveyResponseUser>>
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
