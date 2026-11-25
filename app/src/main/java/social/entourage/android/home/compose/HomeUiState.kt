package social.entourage.android.home.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Suggestion
import social.entourage.android.home.WelcomeJourneyState
import social.entourage.android.home.WelcomeJourneyStep

/**
 * État affiché par [HomeScreen]. Remplace les adapters de l'ancienne home : chaque champ correspond à l'état
 * qu'un adapter (liste, visibilité, textes d'en-tête et de bouton) portait.
 */
class HomeUiState {
    // En-tête
    var avatarUrl by mutableStateOf<String?>(null)
    /** Comme le XML : pastille visible avec « 9+ » tant que le compteur n'est pas arrivé. */
    var notifBadgeVisible by mutableStateOf(true)
    var notifBadgeText by mutableStateOf("9+")
    var titleVisible by mutableStateOf(true)
    /** `topMargin` du bloc d'en-tête, en pixels comme `DEFAULT_MARGIN`/`NEW_MARGIN` de l'ancien fragment (0 jusqu'au premier défilement). */
    var headerTopMarginPx by mutableIntStateOf(0)

    // Chargement : squelette tant que les 4 réponses initiales ne sont pas arrivées
    var contentShown by mutableStateOf(false)

    // Parcours de bienvenue (visible par défaut, comme `HomeWelcomeJourneyAdapter`)
    private var journeyVisibleBacking by mutableStateOf(true)
    var journeyVisible: Boolean
        get() = journeyVisibleBacking
        set(value) {
            if (journeyVisibleBacking != value) {
                journeyVisibleBacking = value
                onJourneyVisibilityChanged?.invoke(value)
            }
        }
    var onJourneyVisibilityChanged: ((Boolean) -> Unit)? = null
    var journeyStates by mutableStateOf<List<WelcomeJourneyState>>(
        List(WelcomeJourneyStep.entries.size) { WelcomeJourneyState.TODO }
    )

    // Initial pedago (« Sensibilisation »)
    var initialPedagos by mutableStateOf<List<Pedago>>(emptyList())
    var initialPedagoVisible by mutableStateOf(false)

    // Actions (entraides / contributions)
    var actions by mutableStateOf<List<Action>>(emptyList())
    var actionsVisible by mutableStateOf(false)
    var isContribution by mutableStateOf(false)
    var actionTitle by mutableStateOf("")
    var actionSubtitle by mutableStateOf<String?>(null)
    var actionButtonText by mutableStateOf("")
    var onActionButtonClick: () -> Unit = {}

    // Événements
    var events by mutableStateOf<List<Events>>(emptyList())
    var eventsVisible by mutableStateOf(false)

    // Suggestions héritées (carte « connexion » et « prochain pas » du back, hors moteur)
    var connectionSuggestion by mutableStateOf<Suggestion?>(null)
    var nextStepSuggestion by mutableStateOf<Suggestion?>(null)

    // Contenus pédagogiques (bas de page)
    var pedagos by mutableStateOf<List<Pedago>>(emptyList())
    var pedagoTitleVisible by mutableStateOf(false)

    /** Demande de défilement (debug du parcours) : index d'item à atteindre, consommé par l'écran. */
    var scrollToJourneyRequest by mutableIntStateOf(0)
}

/** Callbacks de [HomeScreen] : toute la navigation, les analytics et l'API restent dans le fragment. */
class HomeActions(
    val onAvatarClick: () -> Unit,
    val onNotifClick: () -> Unit,
    val onLogoClick: (() -> Unit)?,
    val onJourneyStep: (Int) -> Unit,
    val onJourneySkip: (Int) -> Unit,
    val onJourneyHide: () -> Unit,
    val onEventsAll: () -> Unit,
    val onToolsMap: () -> Unit,
    val onToolsPedago: () -> Unit,
    val onToolsCharter: () -> Unit,
    val onToolsDonate: () -> Unit,
    val onPedagoClick: (Pedago) -> Unit,
    val onConnectionAction: (Suggestion) -> Unit,
    val onConnectionDismiss: (Suggestion) -> Unit,
    val onNextStepAction: (Suggestion) -> Unit,
    val onNextStepDismiss: (Suggestion) -> Unit,
)
