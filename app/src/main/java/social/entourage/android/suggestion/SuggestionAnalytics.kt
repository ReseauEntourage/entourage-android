package social.entourage.android.suggestion

/**
 * Paramètres Firebase des événements de suggestion, au même format que sur iOS (EN-9473) :
 * `type` (le `kind` du moteur, ex. `outing`) et `id`, identifiant de la card pour la mesure.
 *
 * Cet `id` n'est pas celui de l'interface ([NextStepSuggestion.id] : `next-step-<id>`, `other-<kind>-…`) :
 * c'est l'id du prochain pas en texte pour le héros, `others-<kind>-<id de la cible ou 0>` pour une autre piste.
 */
object SuggestionAnalytics {
    const val PARAM_TYPE = "type"
    const val PARAM_ID = "id"
    const val PARAM_COUNT = "count"

    /** Id de la card pour Firebase (iOS : `SuggestionCard.id`). */
    fun cardId(s: NextStepSuggestion): String {
        val engineId = s.engineId
        if (engineId != null) return engineId.toString()
        // Même règle que `target_id` des interactions : les types sans cible propre comptent pour 0.
        val targetId = SuggestionInteraction.of(SuggestionInteraction.DISPLAYED, s).targetId ?: 0
        return "others-${s.typeKey}-$targetId"
    }

    /** Id de la ligne Explorer pour Firebase : celui de sa piste (une ligne sans piste n'a pas de cible). */
    fun accessId(access: ExplorerAccess): String =
        access.suggestion?.let(::cardId) ?: "others-${access.typeKey}-0"

    /** Héros (vu, CTA, corps, « ? », « Plus tard », annulation, « Pourquoi » vu). */
    fun cardParams(s: NextStepSuggestion): Map<String, Any> =
        mapOf(PARAM_TYPE to s.typeKey, PARAM_ID to cardId(s))

    /** Tap sur une ligne de l'Explorer. */
    fun accessParams(access: ExplorerAccess): Map<String, Any> =
        mapOf(PARAM_TYPE to access.typeKey, PARAM_ID to accessId(access))

    /** Explorer vu : nombre de lignes affichées. */
    fun explorerViewParams(accesses: List<ExplorerAccess>): Map<String, Any> =
        mapOf(PARAM_COUNT to accesses.size)
}
