package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName

data class SuggestionResponse(
    @SerializedName("connection") val connection: Suggestion?,
    @SerializedName("next_step") val nextStep: Suggestion?
)

data class Suggestion(
    @SerializedName("id") val id: Int,
    @SerializedName("suggestion_type") val suggestionType: String,
    @SerializedName("suggested_action") val suggestedAction: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("reason_type") val reasonType: String?,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("suggested_user_info") val suggestedUserInfo: SuggestedUser?,
    @SerializedName("suggested_entourage_info") val suggestedEntourageInfo: SuggestedEntourage?
)

data class SuggestedUser(
    @SerializedName("id") val id: Int,
    @SerializedName("uuid") val uuid: String?,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("postal_code") val postalCode: String?
)

data class SuggestedEntourage(
    @SerializedName("id") val id: Int,
    @SerializedName("uuid") val uuid: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("group_type") val groupType: String?,
    @SerializedName("display_category") val displayCategory: String?,
    @SerializedName("metadata") val metadata: Map<String, Any>?
)
