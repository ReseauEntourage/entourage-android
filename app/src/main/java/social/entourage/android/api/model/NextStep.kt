package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName

data class NextStep(
    @SerializedName("id") val id: Int,
    @SerializedName("suggestion_type") val suggestionType: String,
    @SerializedName("title") val title: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("cta_label") val ctaLabel: String,
    @SerializedName("cta_action") val ctaAction: String?,
    @SerializedName("expires_at") val expiresAt: String?
)

data class NextStepResponse(
    @SerializedName("next_step") val nextStep: NextStep?
)

data class OnboardingOption(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String
)

data class OnboardingQuestion(
    @SerializedName("key") val key: String,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String, // "cards" or "chips"
    @SerializedName("options") val options: List<OnboardingOption>,
    @SerializedName("current_value") val currentValue: String?
)

data class OnboardingQuestionsResponse(
    @SerializedName("questions") val questions: List<OnboardingQuestion>
)
