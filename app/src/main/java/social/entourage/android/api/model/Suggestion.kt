package social.entourage.android.api.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.json.JsonObject


@KSerializable
data class SuggestionResponse(
    @SerialName("connection") val connection: Suggestion? = null,
    @SerialName("next_step") val nextStep: Suggestion? = null
)

@KSerializable
data class Suggestion(
    @SerialName("id") val id: Int = 0,
    @SerialName("suggestion_type") val suggestionType: String = "",
    @SerialName("suggested_action") val suggestedAction: String = "",
    @SerialName("reason") val reason: String? = null,
    @SerialName("reason_type") val reasonType: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("suggested_user_info") val suggestedUserInfo: SuggestedUser? = null,
    @SerialName("suggested_entourage_info") val suggestedEntourageInfo: SuggestedEntourage? = null
)

@KSerializable
data class SuggestedUser(
    @SerialName("id") val id: Int = 0,
    @SerialName("uuid") val uuid: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("postal_code") val postalCode: String? = null
)

@KSerializable
data class SuggestedEntourage(
    @SerialName("id") val id: Int = 0,
    @SerialName("uuid") val uuid: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("group_type") val groupType: String? = null,
    @SerialName("display_category") val displayCategory: String? = null,
    @SerialName("metadata") val metadata: JsonObject? = null
)
