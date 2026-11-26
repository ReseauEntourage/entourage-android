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

    // Événements « Prochain Pas » (EN-9892).
    const val PARAM_KIND = "kind"
    const val PARAM_TARGET_ID = "target_id"
    const val PARAM_POSITION = "position"
    const val PARAM_EXPLORER_KIND = "explorer_kind"
    const val PARAM_EXPLORER_TARGET_ID = "explorer_target_id"
    const val PARAM_REALISATION_TRIGGER = "realisation_trigger"
    const val PARAM_REASON = "reason"

    const val TRIGGER_TAP = "tap"
    const val TRIGGER_AUTO = "auto"

    const val REASON_NO_CANDIDATES = "no_candidates"
    const val REASON_ALL_EXCLUDED = "all_excluded"
    const val REASON_ERROR = "error"

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

    /**
     * `kind` du moteur et `target_id` (Long) de la suggestion principale, lus dans l'objet en mémoire.
     * `target_id` est absent pour les types sans cible propre (Bonnes ondes, mini-parcours sans id, « devenir Animateur »).
     */
    fun nextStepParams(s: NextStepSuggestion): Map<String, Any> = buildMap {
        put(PARAM_KIND, s.typeKey)
        s.targetId?.let { put(PARAM_TARGET_ID, it.toLong()) }
    }

    /** `kind` et `target_id` de la suggestion principale, s'il y en a une ; vide sinon. */
    fun optionalNextStepParams(s: NextStepSuggestion?): Map<String, Any> = s?.let { nextStepParams(it) }.orEmpty()

    /** Tap sur une ligne de l'Explorer : la suggestion principale (si elle existe), le rang (1 à 3) et la piste tapée. */
    fun explorerTappedParams(hero: NextStepSuggestion?, access: ExplorerAccess, position: Int?): Map<String, Any> =
        buildMap {
            putAll(optionalNextStepParams(hero))
            position?.let { put(PARAM_POSITION, it) }
            put(PARAM_EXPLORER_KIND, access.typeKey)
            access.suggestion?.targetId?.let { put(PARAM_EXPLORER_TARGET_ID, it.toLong()) }
        }

    /** `next_step_null` : `reason` ∈ `no_candidates`, `all_excluded`, `error`. */
    fun nullParams(reason: String): Map<String, Any> = mapOf(PARAM_REASON to reason)

    /**
     * Réalisation par un tap direct : seuls `poi` et `become_ambassador` ne passent par aucune action que le back
     * puisse détecter ; et seulement pour la suggestion principale (id du moteur), pas pour une autre piste.
     */
    fun isRealisedByTap(s: NextStepSuggestion): Boolean =
        s.engineId != null && s.type.let { it == SuggestionType.PLACE || it == SuggestionType.AMBASSADOR }

    fun realisedByTapParams(s: NextStepSuggestion): Map<String, Any> =
        nextStepParams(s) + (PARAM_REALISATION_TRIGGER to TRIGGER_TAP)
}
