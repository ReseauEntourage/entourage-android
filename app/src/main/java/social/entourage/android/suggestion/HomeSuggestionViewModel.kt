package social.entourage.android.suggestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * Porte l'état des suggestions de la home (card « Votre prochain pas » + zone Explorer) et la mesure
 * de leur affichage : un seul « affiché » par card et par affichage de la home (voir [onHomeResumed]).
 */
class HomeSuggestionViewModel(
    private val provider: SuggestionProvider = NextStepSuggestionProvider(),
    /** Événements Firebase (nom, paramètres) ; remplacé par un faux dans les tests JVM. */
    private val analytics: (event: String, params: Map<String, Any>) -> Unit =
        { event, params -> AnalyticsEvents.logEvent(event, params) },
) : ViewModel() {

    private val _state = MutableStateFlow(SuggestionUiState())
    val state: StateFlow<SuggestionUiState> = _state.asStateFlow()

    /** Émis quand « Plus tard » retire la card : la vue propose d'annuler (snackbar). */
    private val _laterEvents = MutableSharedFlow<NextStepSuggestion>(extraBufferCapacity = 1)
    val laterEvents: SharedFlow<NextStepSuggestion> = _laterEvents.asSharedFlow()

    private var pendingLater: NextStepSuggestion? = null

    /**
     * Cards écartées par « Plus tard » depuis le lancement de l'app : le moteur peut, selon sa réponse (ignore sans
     * suivante, erreur réseau, `current` servi avant la prise en compte), renvoyer la même card quelques secondes
     * après. Elle ne revient pas dans la session ; au prochain lancement, le moteur décide.
     */
    private val laterIds = mutableSetOf<String>()

    private fun SuggestionPayload.withoutLater() =
        if (hero != null && hero.id in laterIds) copy(hero = null) else this

    // Ids déjà mesurés depuis le dernier affichage de la home (comme `SuggestionCoordinator` d'iOS).
    private val loggedHeroIds = mutableSetOf<String>()
    private val loggedAccessIds = mutableSetOf<String>()
    private var hasLoggedExplorerView = false
    private val loggedIgnoredIds = mutableSetOf<String>()

    /** Tap sur une ligne de l'Explorer : `position` est son rang (1 à 3) dans la zone affichée. */
    fun explorerTapped(access: ExplorerAccess) {
        val payload = _state.value.payload
        val index = payload?.explorer?.indexOfFirst { it.id == access.id } ?: -1
        analytics(
            AnalyticsEvents.Next_Step_Explorer_Tapped,
            SuggestionAnalytics.explorerTappedParams(payload?.hero, access, if (index >= 0) index + 1 else null),
        )
    }

    /** Rafraîchit sans vider l'écran : le dernier payload reste affiché (pas de saut de contenu). */
    fun refresh() {
        // Pendant le message « Annuler », la suggestion retirée ne doit pas revenir.
        if (pendingLater != null) return
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val payload = try {
                provider.fetch()
            } catch (e: Exception) {
                null
            }
            logNullIfEmpty(payload)
            // Erreur réseau : on garde le dernier payload connu.
            _state.update { SuggestionUiState(loading = false, payload = (payload ?: it.payload)?.withoutLater()) }
        }
    }

    /**
     * `next_step_null` : le moteur ne propose rien alors que la fonction est active (le view-model n'est sollicité ni
     * quand elle est coupée ni pendant le parcours de bienvenue). `error` : appel en échec ; `all_excluded` : le moteur
     * a resservi une card déjà écartée par « Plus tard » ; sinon `no_candidates` (le back ne distingue pas les deux cas).
     */
    private fun logNullIfEmpty(payload: SuggestionPayload?) {
        val reason = when {
            payload == null -> SuggestionAnalytics.REASON_ERROR
            payload.hero == null -> SuggestionAnalytics.REASON_NO_CANDIDATES
            payload.hero.id in laterIds -> SuggestionAnalytics.REASON_ALL_EXCLUDED
            else -> return
        }
        analytics(AnalyticsEvents.Next_Step_Null, SuggestionAnalytics.nullParams(reason))
    }

    fun later(suggestion: NextStepSuggestion) {
        analytics(AnalyticsEvents.Next_Step_Later_Tapped, SuggestionAnalytics.nextStepParams(suggestion))
        pendingLater = suggestion
        _state.update { it.copy(payload = it.payload?.copy(hero = null)) }
        _laterEvents.tryEmit(suggestion)
    }

    /** Annulation depuis le snackbar : la card revient telle quelle. */
    fun undoLater() {
        val suggestion = pendingLater ?: return
        pendingLater = null
        laterIds -= suggestion.id
        analytics(AnalyticsEvents.Next_Step_Later_Cancelled, SuggestionAnalytics.nextStepParams(suggestion))
        _state.update { it.copy(payload = (it.payload ?: SuggestionPayload()).copy(hero = suggestion)) }
    }

    /** Le snackbar a disparu sans annulation : « Plus tard » est acté, le moteur propose la suivante. */
    fun confirmLater() {
        val suggestion = pendingLater ?: return
        pendingLater = null
        laterIds += suggestion.id
        viewModelScope.launch {
            // La réponse du moteur contient la suggestion suivante : pas de second appel.
            var acknowledged = false
            val next = try {
                provider.dismiss(suggestion).also { acknowledged = true }
            } catch (_: Exception) {
                null
            }
            // Loguer après la réponse du moteur seulement, une fois par card (Firebase ne compte pas une erreur réseau).
            if (acknowledged && suggestion.engineId != null && loggedIgnoredIds.add(suggestion.id)) {
                analytics(AnalyticsEvents.Next_Step_Ignored, SuggestionAnalytics.nextStepParams(suggestion))
            }
            if (next != null) {
                _state.update {
                    SuggestionUiState(loading = false, payload = (it.payload ?: SuggestionPayload()).copy(hero = next).withoutLater())
                }
            } else {
                refresh()
            }
        }
    }

    /** Transmet un geste au moteur, sans attendre et sans jamais échouer. */
    fun report(kind: String, suggestion: NextStepSuggestion) {
        viewModelScope.launch { runCatching { provider.report(SuggestionInteraction.of(kind, suggestion)) } }
    }

    /**
     * La home s'affiche (ou se réaffiche) : la mesure d'affichage repart, une fois par card. Sans cela, un item
     * détaché puis rattaché (défilement, vue recréée) renverrait « affiché » à chaque fois.
     */
    fun onHomeResumed() {
        loggedHeroIds.clear()
        loggedAccessIds.clear()
        hasLoggedExplorerView = false
    }

    /**
     * La card « Votre prochain pas » s'affiche : un seul événement Firebase et un seul `displayed` par id de card
     * et par affichage de la home. La card restaurée par « Annuler » garde son id : elle n'est pas remesurée.
     */
    fun heroDisplayed(suggestion: NextStepSuggestion) {
        if (!loggedHeroIds.add(suggestion.id)) return
        analytics(AnalyticsEvents.View_Home_Suggestion_Hero, SuggestionAnalytics.cardParams(suggestion))
        report(SuggestionInteraction.DISPLAYED, suggestion)
    }

    /**
     * La zone Explorer s'affiche : « Explorer vu » une fois par affichage de la home, et un seul `displayed`
     * par id de piste.
     */
    fun explorerDisplayed(accesses: List<ExplorerAccess>) {
        if (!hasLoggedExplorerView) {
            hasLoggedExplorerView = true
            analytics(AnalyticsEvents.View_Home_Explorer, SuggestionAnalytics.explorerViewParams(accesses))
        }
        accesses.filter { loggedAccessIds.add(it.id) }
            .mapNotNull { it.suggestion }
            .forEach { report(SuggestionInteraction.DISPLAYED, it) }
    }
}
