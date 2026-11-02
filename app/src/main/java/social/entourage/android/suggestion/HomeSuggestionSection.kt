package social.entourage.android.suggestion

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.tools.log.AnalyticsEvents

/** Interrupteur des suggestions mockées : seulement en debug tant que le moteur (EN-9473) n'est pas livré. */
object SuggestionFeature {
    val ENABLED: Boolean get() = BuildConfig.DEBUG
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
    private val navigator = SuggestionNavigator(fragment.requireContext(), onSayHello)
    private var needsRefresh = false
    private var started = false

    private val heroAdapter = HomeComposeAdapter<HeroUi>(
        initial = HeroUi.Loading,
        isVisible = { it !is HeroUi.Hidden },
    ) { ui ->
        SuggestionHeroZone(
            ui = ui,
            onCta = { s ->
                AnalyticsEvents.logEvent(AnalyticsEvents.Click_Home_Suggestion_Cta)
                open(s)
            },
            onBody = { s ->
                AnalyticsEvents.logEvent(AnalyticsEvents.Click_Home_Suggestion_Body)
                open(s)
            },
            onLater = { s ->
                AnalyticsEvents.logEvent(AnalyticsEvents.Click_Home_Suggestion_Later)
                viewModel.later(s)
            },
            onShown = { AnalyticsEvents.logEvent(AnalyticsEvents.View_Home_Suggestion_Hero) },
            onWhyOpened = { AnalyticsEvents.logEvent(AnalyticsEvents.Click_Home_Suggestion_Why) },
        )
    }

    private val explorerAdapter = HomeComposeAdapter<ExplorerUi>(
        initial = ExplorerUi.Loading,
        isVisible = { it !is ExplorerUi.Hidden },
    ) { ui ->
        SuggestionExplorerZone(
            ui = ui,
            onAccess = { access ->
                AnalyticsEvents.logEvent(AnalyticsEvents.Click_Home_Explorer_Access)
                navigator.open(access)
            },
            onShown = { AnalyticsEvents.logEvent(AnalyticsEvents.View_Home_Explorer) },
        )
    }

    val adapters: List<RecyclerView.Adapter<*>> get() = listOf(heroAdapter, explorerAdapter)

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
                        heroAdapter.submit(state.toHeroUi())
                        explorerAdapter.submit(state.toExplorerUi())
                    }
                }
                launch { viewModel.laterEvents.collect { showLaterSnackbar() } }
            }
        }
    }

    fun onResume() {
        if (!started || needsRefresh) {
            started = true
            needsRefresh = false
            viewModel.refresh()
        }
    }

    private fun showLaterSnackbar() {
        Snackbar.make(snackbarAnchor, R.string.home_suggestion_later_snackbar, Snackbar.LENGTH_LONG)
            .setAction(R.string.home_suggestion_later_undo) { viewModel.undoLater() }
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) viewModel.confirmLater()
                }
            })
            .show()
    }
}
