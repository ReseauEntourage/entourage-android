package social.entourage.android.suggestion

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import social.entourage.android.R
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * Interrupteur de l'affichage des suggestions du moteur (EN-9473). ALLUMÉ : la home affiche les suggestions
 * et appelle le moteur. Pour la couper (livrer sans la fonction) : passer cette valeur à `false`, rien d'autre
 * à toucher, la home retrouve ses sections d'origine (entraides, événements, groupes) et le moteur n'est
 * jamais appelé. L'interrupteur du back (`NEXT_STEPS_ENABLED`) la coupe, lui, sans release mobile.
 */
object SuggestionFeature {
    val ENABLED: Boolean get() = true
}

/**
 * Branche les suggestions (card héros, zone Explorer) sur la home : fournit les adapters à ajouter
 * à la ConcatAdapter et relie l'état du [HomeSuggestionViewModel], la navigation, le snackbar
 * d'annulation et l'instrumentation.
 */
class HomeSuggestionSection(
    private val fragment: Fragment,
    private val viewModel: HomeSuggestionViewModel,
    private val snackbarAnchor: View,
    onSayHello: (userId: Int) -> Unit,
) {
    private val navigator = SuggestionNavigator(
        fragment.requireContext(),
        onSayHello,
        onOpenPoi = { poi ->
            ReadPoiFragment.newInstance(poi, "").show(fragment.childFragmentManager, ReadPoiFragment.TAG)
        },
    )
    private var needsRefresh = false
    private var started = false
    private var suppressed = false

    // Version Compose de la home (LazyColumn) : mêmes états que les adapters, lus directement.
    private var heroUiState by mutableStateOf<HeroUi>(HeroUi.Loading)
    private var explorerUiState by mutableStateOf<ExplorerUi>(ExplorerUi.Loading)
    private var suppressedState by mutableStateOf(false)
    val heroVisibleInCompose: Boolean get() = !suppressedState && heroUiState !is HeroUi.Hidden
    val explorerVisibleInCompose: Boolean get() = !suppressedState && explorerUiState !is ExplorerUi.Hidden

    @Composable
    fun HeroItem() = HeroContent(heroUiState)

    @Composable
    fun ExplorerItem() = ExplorerContent(explorerUiState)

    private val heroAdapter = HomeComposeAdapter<HeroUi>(
        initial = HeroUi.Loading,
        isVisible = { it !is HeroUi.Hidden },
    ) { ui -> HeroContent(ui) }

    @Composable
    private fun HeroContent(ui: HeroUi) {
        SuggestionHeroZone(
            ui = ui,
            onCta = { s ->
                log(AnalyticsEvents.Click_Home_Suggestion_Cta, s)
                viewModel.report(SuggestionInteraction.TAPPED, s)
                open(s)
            },
            onBody = { s ->
                log(AnalyticsEvents.Click_Home_Suggestion_Body, s)
                viewModel.report(SuggestionInteraction.TAPPED, s)
                open(s)
            },
            onLater = { s ->
                log(AnalyticsEvents.Click_Home_Suggestion_Later, s)
                viewModel.later(s)
            },
            // Mesure d'affichage dédoublonnée par le ViewModel (un item détaché puis rattaché rejoue cet appel).
            onShown = { s -> viewModel.heroDisplayed(s) },
            onWhyOpened = { s ->
                log(AnalyticsEvents.Click_Home_Suggestion_Why, s)
                viewModel.report(SuggestionInteraction.WHY_OPENED, s)
            },
            onWhyShown = { s -> log(AnalyticsEvents.View_Home_Suggestion_Why, s) },
        )
    }

    private val explorerAdapter = HomeComposeAdapter<ExplorerUi>(
        initial = ExplorerUi.Loading,
        isVisible = { it !is ExplorerUi.Hidden },
    ) { ui -> ExplorerContent(ui) }

    @Composable
    private fun ExplorerContent(ui: ExplorerUi) {
        SuggestionExplorerZone(
            ui = ui,
            onAccess = { access ->
                AnalyticsEvents.logEvent(AnalyticsEvents.Click_Home_Explorer_Access, SuggestionAnalytics.accessParams(access))
                viewModel.explorerTapped(access)
                val piste = access.suggestion
                if (piste != null) {
                    viewModel.report(SuggestionInteraction.TAPPED, piste)
                    open(piste)
                } else {
                    navigator.open(access)
                }
            },
            onShown = { accesses -> viewModel.explorerDisplayed(accesses) },
        )
    }

    val adapters: List<RecyclerView.Adapter<*>> get() = listOf(heroAdapter, explorerAdapter)

    /** Parcours de bienvenue et suggestions ne s'affichent jamais ensemble. */
    fun setSuppressed(value: Boolean) {
        val wasSuppressed = suppressed
        suppressed = value
        suppressedState = value
        heroAdapter.setSuppressed(value)
        explorerAdapter.setSuppressed(value)
        // La card devient visible (parcours de bienvenue terminé ou masqué) : le moteur est appelé à ce moment.
        if (wasSuppressed && !value) {
            started = true
            needsRefresh = false
            viewModel.refresh()
        }
    }

    /** Événement Firebase d'une card, avec `type` et `id` comme sur iOS. */
    private fun log(event: String, s: NextStepSuggestion) =
        AnalyticsEvents.logEvent(event, SuggestionAnalytics.cardParams(s))

    private fun open(s: NextStepSuggestion) {
        // Au retour sur la home, la card est rafraîchie (le moteur remplace une suggestion traitée).
        needsRefresh = true
        navigator.open(s)
    }

    fun start() {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        heroUiState = state.toHeroUi()
                        explorerUiState = state.toExplorerUi()
                        heroAdapter.submit(state.toHeroUi())
                        explorerAdapter.submit(state.toExplorerUi())
                    }
                }
                launch { viewModel.laterEvents.collect { showLaterSnackbar(it) } }
            }
        }
    }

    fun onResume() {
        // La home se réaffiche : la mesure d'affichage repart (une fois par card et par affichage).
        viewModel.onHomeResumed()
        // Une suggestion servie court pour sa durée de validité : pas d'appel tant que la card n'est pas visible.
        if (suppressed) return
        if (!started || needsRefresh) {
            started = true
            needsRefresh = false
            viewModel.refresh()
        }
    }

    private fun showLaterSnackbar(suggestion: NextStepSuggestion) {
        Snackbar.make(snackbarAnchor, R.string.home_suggestion_later_snackbar, Snackbar.LENGTH_LONG)
            // 5 s pour annuler, comme sur iOS (LENGTH_LONG ne dure qu'environ 2,75 s).
            .setDuration(LATER_SNACKBAR_DURATION_MS)
            .setAction(R.string.home_suggestion_later_undo) {
                log(AnalyticsEvents.Click_Home_Suggestion_Undo_Later, suggestion)
                viewModel.undoLater()
            }
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) viewModel.confirmLater()
                }
            })
            .show()
    }

    private companion object {
        const val LATER_SNACKBAR_DURATION_MS = 5000
    }
}
