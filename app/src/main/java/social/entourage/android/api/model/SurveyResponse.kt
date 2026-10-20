package social.entourage.android.api.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable



@KSerializable
data class SurveyResponsesWrapper(
    @SerialName("responses")
    val responses: List<Boolean> = emptyList()
)

@KSerializable
data class SurveyResponseUser(
    @SerialName("id") val id: Int = 0,
    @SerialName("lang") val lang: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("community_roles") val communityRoles: List<String> = emptyList()
)

@KSerializable
data class SurveyResponse(
    val users: List<SurveyResponseUser> = emptyList()
)

@KSerializable
data class SurveyResponsesListWrapper(
    @SerialName("survey_responses") val responses: List<List<SurveyResponseUser>> = emptyList()
)

@KSerializable
data class ChatMessageSurvey(
    val content: String = "",
    @SerialName("survey_attributes")
    val surveyAttributes: SurveyAttributes? = null
)

@KSerializable
data class SurveyAttributes(
    @SerialName("choices")
    val choices: List<String> = emptyList(),
    @SerialName("multiple")
    val multiple: Boolean = false
)
