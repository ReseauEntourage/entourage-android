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

/** Porte l'état des suggestions de la home (card « Votre prochain pas » + zone Explorer). */
class HomeSuggestionViewModel(
    private val provider: SuggestionProvider = NextStepSuggestionProvider(),
) : ViewModel() {

    private val _state = MutableStateFlow(SuggestionUiState())
    val state: StateFlow<SuggestionUiState> = _state.asStateFlow()

    /** Émis quand « Plus tard » retire la card : la vue propose d'annuler (snackbar). */
    private val _laterEvents = MutableSharedFlow<NextStepSuggestion>(extraBufferCapacity = 1)
    val laterEvents: SharedFlow<NextStepSuggestion> = _laterEvents.asSharedFlow()

    private var pendingLater: NextStepSuggestion? = null

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
            // Erreur réseau : on garde le dernier payload connu.
            _state.update { SuggestionUiState(loading = false, payload = payload ?: it.payload) }
        }
    }

    fun later(suggestion: NextStepSuggestion) {
        pendingLater = suggestion
        _state.update { it.copy(payload = it.payload?.copy(hero = null)) }
        _laterEvents.tryEmit(suggestion)
    }

    /** Annulation depuis le snackbar : la card revient telle quelle. */
    fun undoLater() {
        val suggestion = pendingLater ?: return
        pendingLater = null
        _state.update { it.copy(payload = (it.payload ?: SuggestionPayload()).copy(hero = suggestion)) }
    }

    /** Le snackbar a disparu sans annulation : « Plus tard » est acté, le moteur propose la suivante. */
    fun confirmLater() {
        val suggestion = pendingLater ?: return
        pendingLater = null
        viewModelScope.launch {
            // La réponse du moteur contient la suggestion suivante : pas de second appel.
            val next = try {
                provider.dismiss(suggestion)
            } catch (_: Exception) {
                null
            }
            if (next != null) {
                _state.update { SuggestionUiState(loading = false, payload = (it.payload ?: SuggestionPayload()).copy(hero = next)) }
            } else {
                refresh()
            }
        }
    }

    /** Transmet un geste au moteur, sans attendre et sans jamais échouer. */
    fun report(kind: String, suggestion: NextStepSuggestion) {
        viewModelScope.launch { runCatching { provider.report(SuggestionInteraction.of(kind, suggestion)) } }
    }

    /** Affichage de la zone Explorer : une interaction par piste montrée. */
    fun reportShown(accesses: List<ExplorerAccess>) {
        accesses.mapNotNull { it.suggestion }.forEach { report(SuggestionInteraction.DISPLAYED, it) }
    }
}
