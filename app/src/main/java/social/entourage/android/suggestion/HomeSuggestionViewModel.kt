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
    private val provider: SuggestionProvider = MockSuggestionProvider(),
) : ViewModel() {

    private val _state = MutableStateFlow(SuggestionUiState())
    val state: StateFlow<SuggestionUiState> = _state.asStateFlow()

    /** Émis quand « Plus tard » retire la card : la vue propose d'annuler (snackbar). */
    private val _laterEvents = MutableSharedFlow<NextStepSuggestion>(extraBufferCapacity = 1)
    val laterEvents: SharedFlow<NextStepSuggestion> = _laterEvents.asSharedFlow()

    private var pendingLater: NextStepSuggestion? = null

    /** Rafraîchit sans vider l'écran : le dernier payload reste affiché (pas de saut de contenu). */
    fun refresh() {
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
            try {
                provider.dismiss(suggestion.id)
            } catch (_: Exception) {
            }
            refresh()
        }
    }
}
