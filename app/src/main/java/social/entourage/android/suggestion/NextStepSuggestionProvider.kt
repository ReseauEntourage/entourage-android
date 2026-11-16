package social.entourage.android.suggestion

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.NextStepInteractionBody
import social.entourage.android.api.request.NextStepInteractionWrapper
import social.entourage.android.api.request.NextStepRequest

/** Geste de l'utilisateur transmis au moteur (`POST /next_steps/interactions`). */
data class SuggestionInteraction(
    /** `displayed`, `why_opened` ou `tapped`. */
    val kind: String,
    /** Prochain pas du moteur : renseigné pour la card, nul pour une autre piste. */
    val nextStepId: Int? = null,
    /** Autre piste : `kind` du moteur et identifiant de la cible. */
    val itemKind: String? = null,
    val targetId: Int? = null,
) {
    companion object {
        const val DISPLAYED = "displayed"
        const val WHY_OPENED = "why_opened"
        const val TAPPED = "tapped"

        /** Types sans cible propre : l'interaction d'une autre piste de ces types omet `target_id`. */
        private val WITHOUT_TARGET = setOf(
            SuggestionType.CONTENT_PATH.key, SuggestionType.SMALL_TALK.key, SuggestionType.AMBASSADOR.key,
        )

        fun of(kind: String, s: NextStepSuggestion) =
            if (s.engineId != null) SuggestionInteraction(kind, nextStepId = s.engineId)
            else SuggestionInteraction(kind, itemKind = s.typeKey, targetId = s.targetId.takeIf { s.typeKey !in WITHOUT_TARGET })
    }
}

/**
 * Suggestions du moteur « Votre prochain pas » (EN-9473) : `current` alimente la card, `others` la
 * zone « Pour vous aujourd'hui ». Appeler [fetch] uniquement quand la card va s'afficher : une
 * suggestion servie court pour sa durée de validité.
 */
class NextStepSuggestionProvider(
    private val request: NextStepRequest = EntourageApplication.get().apiModule.nextStepRequest,
) : SuggestionProvider {

    /** Une erreur de `current` fait échouer l'appel (l'écran garde son dernier contenu) ; une erreur de `others` masque seulement la zone. */
    override suspend fun fetch(): SuggestionPayload = coroutineScope {
        val others = async { runCatching { request.others() }.getOrNull() }
        val hero = request.current().nextStep
        SuggestionPayload(
            hero = hero?.let { NextStepMapper.toSuggestion(it) },
            explorer = others.await()?.let { NextStepMapper.toExplorer(it.nextSteps) }.orEmpty(),
        ).sanitized()
    }

    override suspend fun dismiss(suggestion: NextStepSuggestion): NextStepSuggestion? {
        val id = suggestion.engineId ?: return null
        val next = request.ignore(id).nextStep ?: return null
        return NextStepMapper.toSuggestion(next)
    }

    override suspend fun report(interaction: SuggestionInteraction) {
        if (interaction.nextStepId == null && interaction.itemKind == null) return
        val body = NextStepInteractionBody(
            placement = if (interaction.nextStepId != null) "hero" else "others",
            kind = interaction.kind,
            nextStepId = interaction.nextStepId,
            itemKind = interaction.itemKind,
            targetId = interaction.targetId,
        )
        // Une mesure ne doit jamais gêner le parcours : toute erreur est ignorée.
        runCatching { request.interactions(NextStepInteractionWrapper(body)) }
    }
}
