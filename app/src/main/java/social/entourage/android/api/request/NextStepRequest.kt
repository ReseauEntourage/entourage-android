package social.entourage.android.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.Serializable as KSerializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/*
 * « Votre prochain pas » (moteur EN-9473, change add-next-step-suggestions du back, décision 8).
 * Le contrat est figé dans design.md (décision 12) de connect-next-step-suggestions-mobile.
 * Distinct de SuggestionRequest, qui appelle l'ancienne route `suggestions`.
 */

@KSerializable
class NextStepReasonDto(
    @SerialName("code") val code: String? = null,
    @SerialName("text") val text: String? = null,
)

/** Une suggestion du moteur : prochain pas (id renseigné) ou autre piste (id nul). */
@KSerializable
class NextStepDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("kind") val kind: String? = null,
    @SerialName("mode") val mode: String? = null,
    @SerialName("placement") val placement: String? = null,
    /** Objet du sérialiseur v1 du type (événement, groupe, entraide, lieu) ; nul pour les autres types. */
    @SerialName("target") val target: JsonObject? = null,
    /** Les trois contenus d'un mini-parcours (`content_path`). */
    @SerialName("targets") val targets: List<JsonObject>? = null,
    /** Adresse du formulaire « devenir Animateur » (`become_ambassador`). */
    @SerialName("link") val link: String? = null,
    @SerialName("reasons") val reasons: List<NextStepReasonDto> = emptyList(),
    @SerialName("expires_at") val expiresAt: String? = null,
)

@KSerializable
class NextStepEnvelope(
    @SerialName("next_step") val nextStep: NextStepDto? = null,
)

@KSerializable
class NextStepsEnvelope(
    @SerialName("next_steps") val nextSteps: List<NextStepDto> = emptyList(),
)

@KSerializable
class NextStepInteractionBody(
    /** `hero` (avec `next_step_id`) ou `others` (avec `item_kind`). */
    @SerialName("placement") val placement: String,
    /** `displayed`, `why_opened` ou `tapped`. */
    @SerialName("kind") val kind: String,
    @SerialName("next_step_id") val nextStepId: Int? = null,
    @SerialName("item_kind") val itemKind: String? = null,
    @SerialName("target_id") val targetId: Int? = null,
)

@KSerializable
class NextStepInteractionWrapper(
    @SerialName("interaction") val interaction: NextStepInteractionBody,
)

interface NextStepRequest {

    @GET("next_steps/current")
    suspend fun current(): NextStepEnvelope

    @GET("next_steps/others")
    suspend fun others(): NextStepsEnvelope

    /** Ignore le prochain pas et renvoie le suivant. */
    @POST("next_steps/{id}/ignore")
    suspend fun ignore(@Path("id") id: Int): NextStepEnvelope

    /** 204 sans corps ; 404 ou 400 si le moteur refuse l'interaction. */
    @POST("next_steps/interactions")
    suspend fun interactions(@Body body: NextStepInteractionWrapper): Response<Void>
}
