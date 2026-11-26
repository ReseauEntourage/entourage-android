package social.entourage.android.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.MainPresenter
import social.entourage.android.R
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSectionFilters
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Summary
import social.entourage.android.api.model.SummaryAction
import social.entourage.android.api.model.User
import social.entourage.android.home.compose.HomeActions
import social.entourage.android.home.compose.HomeScreen
import social.entourage.android.home.compose.HomeUiState
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.chatbot.ChatBotBottomSheet
import social.entourage.android.home.pedago.OnItemClick
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.notifications.InAppNotificationsActivity
import social.entourage.android.notifications.NotificationActionManager
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.onboarding.onboard.OnboardingStartActivity
import social.entourage.android.onboarding.onboard.OnboardingZoneChoiceActivity
import social.entourage.android.profile.MyProfileFullActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.suggestion.HomeSuggestionSection
import social.entourage.android.suggestion.HomeSuggestionViewModel
import social.entourage.android.suggestion.SuggestionFeature
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.fcmTokenTask
import social.entourage.android.tools.utils.overrideTransitionCompat
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.user.UserPresenter
import timber.log.Timber

/**
 * Home entièrement en Compose (branche `homeCompose`). Reprend la logique de [HomeFragment] (observateurs,
 * navigation, analytics, parcours de bienvenue, popups d'entrée) ; seule la couche de vue change : le XML
 * `fragment_home` + `RecyclerView` + adapters devient [HomeScreen] qui lit un [HomeUiState].
 * Choisie à la place de [HomeFragment] par [HomeFragmentFactory] (interrupteur [social.entourage.android.home.compose.HomeComposeFeature]).
 */
class HomeComposeFragment : Fragment(), OnHomeChangeLocationUpdate {

    private val ui = HomeUiState()
    private var composeRoot: ComposeView? = null
    private lateinit var homePresenter: HomePresenter
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private lateinit var mainPresenter: MainPresenter
    private var pageEvent = 0
    private var nbOfItemForHozrizontalList = 10
    private var nbOfItemForVerticalList = 3
    private var currentFilters = EventActionLocationFilters()
    private var currentSectionsFilters = ActionSectionFilters()
    private var user: User? = null
    private var pedagoItemForCreateEvent: Pedago? = null
    private var pedagoItemForCreateGroup: Pedago? = null
    private var totalchecksum = 0
    private var isActionEmpty = false
    private var isContribution = false
    private lateinit var actionsPresenter: ActionsPresenter

    private val nationalGroupsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                markWelcomeJourneyStepCompleted(2)
                showGroupsSnackbar()
                homePresenter.getSummary()
            }
        }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    // Variables pour la vidéo
    private var welcomeVideoUrl: String? = null
    private var welcomeVideoHtml: String? = null
    private var currentVideoWebView: android.webkit.WebView? = null // Permet d'injecter la vidéo une fois reçue

    // Welcome Journey
    private var completedJourneySteps = mutableSetOf<Int>()
    private var lastSummary: Summary? = null
    private var inactivityCheckedForUserId: Int? = null
    private var debugJourneyForced = false
    private var debugJourneyPanel: View? = null
    private var debugJourneyTable: TextView? = null
    private var debugLastAction: String? = null

    // Suggestions
    private var suggestionSection: HomeSuggestionSection? = null

    // Gère uniquement le cycle de vie du fragment pour ne pas lancer plusieurs popups en même temps
    private var hasRunEntryGating = false

    private val userObserver = Observer<User> {
        updateUser(it)
        runHomeEntryGatingIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                requireActivity().finish()
            }
        }
    }

    private fun showCongratDialog(summary: Summary) {
        if (!isAdded) return
        val dialog = HomeCongratPopFragment.newInstance()
        dialog.show(parentFragmentManager, HomeCongratPopFragment.TAG)
    }

    internal fun handleWelcomeJourneyState(summary: Summary) {
        lastSummary = summary

        val currentUser = EntourageApplication.me(requireContext())

        // Sécurité pour réinitialiser l'état local si l'utilisateur s'est déconnecté/reconnecté avec un autre compte
        if (currentUser?.id != currentUserIdForJourney) {
            hasInitiallyCompletedAll = null
            currentUserIdForJourney = currentUser?.id
        }

        // Mode debug (appui long sur le logo) : on affiche le parcours quoi qu'il arrive
        val forced = BuildConfig.DEBUG && debugJourneyForced

        // Récupération des informations de validation via le WS uniquement
        val events = (if (BuildConfig.DEBUG) summaryEventsOverride else null) ?: summary.events
        // Un faux summary injecté (debug / tests) l'emporte sur l'état du back
        val journey = if (BuildConfig.DEBUG && summaryEventsOverride != null) null else summary.onboardingJourney
        // Le back porte l'état du parcours (retrait, étapes) : il fait foi, les règles locales ne servent qu'en repli
        val serverDriven = WelcomeJourneyServerState.isServerDriven(journey)
        val states = WelcomeJourneyStep.statesOf(events, journey)
        // Parcours "résolu" : chaque étape est faite ou passée
        val allResolved = states.all { it != WelcomeJourneyState.TODO }

        if (!serverDriven && !forced && currentUser?.id != null && currentUser.partner == null &&
            !isWelcomeJourneyHidden(currentUser.id) && inactivityCheckedForUserId != currentUser.id
        ) {
            inactivityCheckedForUserId = currentUser.id
            if (autoSkipAfterInactivity(currentUser.id, states)) return
        }

        if (!forced) {
            // Règle 1: Masquer si l'utilisateur est partenaire
            if (currentUser?.partner != null) {
                ui.journeyVisible = false
                return
            }

            // Règle serveur : parcours retiré par le back (terminé, masqué, inactivité, plafond), définitif
            if (serverDriven && WelcomeJourneyServerState.isRemoved(journey)) {
                if (WelcomeJourneyServerState.shouldCongratulateOnRemoval(journey, states, journeyShownThisSession)) {
                    showCongratDialog(summary)
                }
                journeyShownThisSession = false
                ui.journeyVisible = false
                return
            }

            // Règle 1 bis: Masqué manuellement par l'utilisateur ("Masquer le parcours")
            if (isWelcomeJourneyHidden(currentUser?.id)) {
                // Le back ne le sait pas encore (appel échoué) : on le lui redit une fois par session
                if (serverDriven && currentUser?.id != null && hideSyncedForUserId != currentUser.id) {
                    hideSyncedForUserId = currentUser.id
                    WelcomeJourneyApi.hideHandler { }
                }
                ui.journeyVisible = false
                return
            }

            // Règle 2: Initialisation du companion object au premier passage
            if (hasInitiallyCompletedAll == null) {
                hasInitiallyCompletedAll = allResolved
            }

            // Règle 3: Si tout était fait ou passé dès le lancement de l'application, on masque tout complètement
            if (hasInitiallyCompletedAll == true) {
                ui.journeyVisible = false
                return
            }

            // Règle 4: Plus aucune étape à faire mais au moins une passée : pas de félicitations, on masque tout
            if (allResolved && states.any { it == WelcomeJourneyState.SKIPPED }) {
                ui.journeyVisible = false
                return
            }
        }

        // Sinon on affiche le composant et on met à jour les données
        journeyShownThisSession = true
        ui.journeyVisible = true
        ui.journeyStates = states

        // Détection du moment où les 4 étapes sont faites (une étape passée ne déclenche pas la célébration)
        val previouslyResolvedSize = completedJourneySteps.size
        completedJourneySteps.clear()
        states.forEachIndexed { i, state -> if (state == WelcomeJourneyState.DONE) completedJourneySteps.add(i + 1) }

        // Si on vient juste de finir les 4 étapes (n'était pas à 4 avant)
        if (allResolved && previouslyResolvedSize < 4 && !forced) {
            showCongratDialog(summary)
        }
        refreshDebugJourneyPanel()
    }

    /**
     * Date de dernière connexion stockée en local (SharedPreferences). Si elle remonte à 14 jours
     * ou plus, on passe toutes les étapes encore à faire. Retourne true si le parcours est masqué.
     */
    private fun autoSkipAfterInactivity(userId: Int, states: List<WelcomeJourneyState>): Boolean {
        val prefs = EntourageApplication.get().sharedPreferences
        val key = welcomeJourneyLastConnectionKey(userId)
        val now = System.currentTimeMillis()
        val last = prefs.getLong(key, 0L)
        prefs.edit { putLong(key, now) }
        if (!WelcomeJourneyInactivity.shouldAutoSkip(last, now)) return false

        val todo = WelcomeJourneyStep.entries.filter { states[it.index - 1] == WelcomeJourneyState.TODO }
        if (todo.isEmpty()) return false
        // On masque pour cette session : le parcours est considéré comme résolu
        hasInitiallyCompletedAll = true
        ui.journeyVisible = false
        todo.forEach { step ->
            WelcomeJourneyApi.skipHandler(step) { success ->
                if (isAdded && success) markStepSkippedLocally(step)
            }
        }
        return true
    }

    private fun isWelcomeJourneyHidden(userId: Int?): Boolean =
        userId != null && EntourageApplication.get().sharedPreferences
            .getBoolean(welcomeJourneyHiddenKey(userId), false)

    private fun onWelcomeJourneySkip(stepIndex: Int) {
        val step = WelcomeJourneyStep.fromIndex(stepIndex) ?: return
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__HOME__WELCOME_JOURNEY_SKIP_STEP)
        debugLastAction = "Passer étape $stepIndex → POST ${step.apiStep}…"
        refreshDebugJourneyPanel()
        WelcomeJourneyApi.skipHandler(step) { success ->
            if (!isAdded) return@skipHandler
            if (success) {
                markStepSkippedLocally(step)
                debugLastAction = "Passer étape $stepIndex → OK (${step.skippedEvent})"
                // Le summary du serveur fait foi, sauf si un faux summary est injecté (debug / tests)
                if (!(BuildConfig.DEBUG && summaryEventsOverride != null)) homePresenter.getSummary()
            } else {
                debugLastAction = "Passer étape $stepIndex → ÉCHEC"
                android.widget.Toast.makeText(requireContext(), R.string.error_generic, android.widget.Toast.LENGTH_SHORT).show()
            }
            refreshDebugJourneyPanel()
        }
    }

    /** Mise à jour optimiste : l'étape apparaît "Passée" sans attendre le rechargement du summary. */
    private fun markStepSkippedLocally(step: WelcomeJourneyStep) {
        val summary = lastSummary ?: return
        if (BuildConfig.DEBUG && summaryEventsOverride != null) {
            summaryEventsOverride = summaryEventsOverride!! + step.skippedEvent
        } else {
            summary.events = ((summary.events ?: mutableListOf()) + step.skippedEvent).toMutableList()
            WelcomeJourneyServerState.withSkipped(summary.onboardingJourney, step.apiStep)
        }
        handleWelcomeJourneyState(summary)
    }

    private fun showWelcomeJourneyMenu(anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menu.add(0, MENU_HIDE_JOURNEY, 0, R.string.welcome_journey_hide_menu)
        popup.setOnMenuItemClickListener {
            if (it.itemId == MENU_HIDE_JOURNEY) showHideWelcomeJourneyConfirmation()
            true
        }
        popup.show()
    }

    private fun showHideWelcomeJourneyConfirmation() {
        if (!isAdded) return
        val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_hide_welcome_journey, null)
        dialog.setContentView(view)
        view.findViewById<View>(R.id.btn_hide_cancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btn_hide_confirm).setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__HOME__WELCOME_JOURNEY_HIDE)
            EntourageApplication.me(requireContext())?.id?.let { id ->
                EntourageApplication.get().sharedPreferences.edit {
                    putBoolean(welcomeJourneyHiddenKey(id), true)
                }
            }
            ui.journeyVisible = false
            // Le back garde la raison « masqué » (survit à la réinstallation) ; en cas d'échec le masquage local reste
            WelcomeJourneyApi.hideHandler { success ->
                if (success) hideSyncedForUserId = EntourageApplication.get().me()?.id
            }
            debugLastAction = "Parcours masqué"
            refreshDebugJourneyPanel()
            dialog.dismiss()
        }
        dialog.show()
        dialog.behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
    }

    // ---- Debug : appui long sur le logo de la home (jamais en prod) ----

    private fun showDebugJourney() {
        debugJourneyForced = true
        // Parcours vierge : aucune étape faite ni passée, le vrai summary est ignoré
        summaryEventsOverride = emptyList()
        debugLastAction = "Parcours vierge lancé"
        if (lastSummary == null) lastSummary = Summary()
        lastSummary?.let { handleWelcomeJourneyState(it) }
        ui.scrollToJourneyRequest++
        showDebugJourneyPanel()
        refreshDebugJourneyPanel()
    }

    private fun showDebugJourneyPanel() {
        if (debugJourneyPanel != null) return
        val content = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        val density = resources.displayMetrics.density
        val panel = android.widget.LinearLayout(requireContext()).apply {
            tag = "debug_journey_panel"
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(Color.argb(235, 30, 30, 30))
            val pad = (10 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        val table = TextView(requireContext()).apply {
            tag = "debug_journey_table"
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val close = TextView(requireContext()).apply {
            text = "✕ fermer le debug"
            setTextColor(Color.parseColor("#FF9739"))
            textSize = 12f
            setOnClickListener {
                debugJourneyForced = false
                summaryEventsOverride = null
                content.removeView(panel)
                debugJourneyPanel = null
                debugJourneyTable = null
                lastSummary?.let { handleWelcomeJourneyState(it) }
            }
        }
        panel.addView(table)
        panel.addView(close)
        content.addView(
            panel,
            android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END
            ).apply { topMargin = (140 * density).toInt() }
        )
        debugJourneyPanel = panel
        debugJourneyTable = table
    }

    private fun refreshDebugJourneyPanel() {
        val table = debugJourneyTable ?: return
        val events = (if (BuildConfig.DEBUG) summaryEventsOverride else null) ?: lastSummary?.events
        val labels = listOf("Vidéo", "Groupe national", "Visio équipe", "Papotages")
        val sb = StringBuilder("DEBUG parcours de bienvenue\n")
        WelcomeJourneyStep.entries.forEach { step ->
            val state = step.stateOf(events)
            sb.append("${step.index} ${labels[step.index - 1].padEnd(15)} $state\n")
        }
        sb.append("Événements : ${events?.filter { it.startsWith("onboarding") }?.size ?: 0}\n")
        sb.append("Dernière action : ${debugLastAction ?: "-"}")
        table.text = sb.toString()
    }

    private fun handleWelcomeJourneyClick(stepIndex: Int) {
        debugLastAction = "Clic étape $stepIndex → " + when (stepIndex) {
            1 -> "vidéo de bienvenue"
            2 -> "NationalGroupsActivity"
            3 -> "WelcomeEventsListActivity(welcome)"
            else -> "page événements"
        }
        refreshDebugJourneyPanel()
        when (stepIndex) {
            1 -> showVideoModal()
            2 -> showNationalGroupsActivity()
            3 -> {
                val intent = Intent(requireContext(), social.entourage.android.events.list.WelcomeEventsListActivity::class.java)
                intent.putExtra("TYPE", "welcome")
                startActivity(intent)
            }
            // Étape « premier événement » : page événements classique, sans filtre
            4 -> (requireActivity() as? MainActivity)?.goEvent()
        }
    }

    private fun showNationalGroupsActivity() {
        if (!isAdded) return
        
        val intent = Intent(requireContext(), NationalGroupsActivity::class.java)
        nationalGroupsLauncher.launch(intent)
        requireActivity().overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun showGroupsSnackbar() {
        if (!isAdded) return
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            requireView(),
            "Vos groupes sont accessibles dans l'onglet Groupes",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Voir") {
            // Naviguer vers l'onglet Groupes
            (requireActivity() as? MainActivity)?.navigateToGroupsTab()
        }
        snackbar.show()
    }

    private fun markWelcomeJourneyStepCompleted(stepIndex: Int) {
        // Cette méthode sera implémentée pour marquer une étape comme complétée
        // Pour l'instant, on rafraîchit juste l'état
        homePresenter.getSummary()
    }

    fun showVideoModal() {
        if (!isAdded) return
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_welcome_video, null)
        bottomSheetDialog.setContentView(view)

        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            behavior.skipCollapsed = true
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        }

        val btnContinue = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_continue)
        val webView = view.findViewById<android.webkit.WebView>(R.id.webview_video)
        val ivClose = view.findViewById<android.widget.ImageView>(R.id.iv_close)

        // On sauvegarde la référence pour que l'observer puisse injecter la vidéo à la réception
        currentVideoWebView = webView

        ivClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        var countDownTimer: android.os.CountDownTimer? = null
        btnContinue?.isEnabled = false

        // On intercepte la fermeture de la modale pour forcer la mise à jour
        bottomSheetDialog.setOnDismissListener {
            countDownTimer?.cancel()
            webView?.destroy()
            currentVideoWebView = null // On libère la mémoire
            // On appelle DIRECTEMENT getSummary() pour que l'adapter se mette à jour instantanément
            homePresenter.getSummary()
        }

        webView?.settings?.javaScriptEnabled = true
        webView?.webChromeClient = android.webkit.WebChromeClient()

        // Si on a déjà l'url en cache (suite à une ouverture précédente dans la même session), on charge notre HTML clean
        if (!welcomeVideoUrl.isNullOrBlank()) {
            webView?.let { loadCleanVideo(it, welcomeVideoUrl!!) }
        }

        // C'est cet appel qui valide la ressource côté backend et télécharge les URLs de la vidéo
        homePresenter.getWelcomeResource()

        btnContinue?.setOnClickListener {
            // Le dismiss va déclencher le setOnDismissListener
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()

        val originalText = btnContinue?.text?.toString() ?: ""
        countDownTimer = object : android.os.CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isAdded) return
                val secondsRemaining = millisUntilFinished / 1000 + 1
                btnContinue?.text = "$originalText ($secondsRemaining)"
            }

            override fun onFinish() {
                if (!isAdded) return
                btnContinue?.text = originalText
                btnContinue?.isEnabled = true
            }
        }.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        totalchecksum = 0
        hasRunEntryGating = false

        mainPresenter = MainPresenter(requireActivity() as MainActivity)
        homePresenter = ViewModelProvider(requireActivity()).get(HomePresenter::class.java)
        actionsPresenter = ViewModelProvider(requireActivity()).get(ActionsPresenter::class.java)

        val root = ComposeView(requireContext())
        setupState()
        setupSuggestions(root)

        AnalyticsEvents.logEvent(AnalyticsEvents.View__Home)
        if (EnhancedOnboarding.shouldNotDisplayCampain != true) {
            AnalyticsEvents.logEvent(AnalyticsEvents.home_activate_firebase_message)
        }

        setObservations()
        checkNotificationStatus()
        increaseCounter()

        val actions = buildActions()
        return root.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HomeScreen(ui = ui, actions = actions, suggestionSection = suggestionSection) }
            composeRoot = this
        }
    }

    private fun setupState() {
        // Masqué tant que handlePedago() n'a pas confirmé qu'il y a des contenus à découvrir.
        ui.pedagoTitleVisible = false
        ui.actionTitle = getString(R.string.home_title_action)
        ui.actionSubtitle = getString(R.string.home_subtitle_action)
        ui.actionButtonText = getString(R.string.home_btn_more_action)
        ui.onActionButtonClick = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Demand_All)
            (requireActivity() as? MainActivity)?.goDemand()
        }
    }

    private fun openPedago(pedagogicalContent: Pedago) {
        if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
            val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
            intent.putExtra(Const.ID, pedagogicalContent.id)
            PedagoDetailActivity.setPedagoId(pedagogicalContent.id)
            requireActivity().startActivity(intent)
            requireActivity().overrideTransitionCompat(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
    }

    private fun buildActions() = HomeActions(
        onAvatarClick = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Tab__Profil)
            activityResultLauncher.launch(Intent(context, MyProfileFullActivity::class.java))
        },
        onNotifClick = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Notif)
            val intent = Intent(requireContext(), InAppNotificationsActivity::class.java)
            intent.putExtra(Const.NOTIF_COUNT, homePresenter.notifsCount.value)
            activityResultLauncher.launch(intent)
        },
        // Clic sur le logo : lance le parcours de bienvenue vierge (faux summary sans événement)
        // avec un tableau d'état de test, pour vérifier chaque étape sans toucher au compte.
        onLogoClick = if (BuildConfig.DEBUG) ({ showDebugJourney() }) else null,
        onJourneyStep = { handleWelcomeJourneyClick(it) },
        onJourneySkip = { onWelcomeJourneySkip(it) },
        onJourneyHide = { showHideWelcomeJourneyConfirmation() },
        onEventsAll = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Event_All)
            (requireActivity() as? MainActivity)?.goEvent()
        },
        onToolsMap = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Map)
            val intent = Intent(requireContext(), GDSMainActivity::class.java)
            intent.putExtra(GDSMainActivity.EXTRA_AIR_CONDITIONED, false)
            activityResultLauncher.launch(intent)
        },
        onToolsPedago = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Pedago)
            val intent = Intent(requireActivity(), PedagoListActivity::class.java)
            requireContext().startActivity(intent)
            requireActivity().overrideTransitionCompat(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        },
        onToolsCharter = {
            val urlString = if (BuildConfig.DEBUG) {
                "https://preprod.entourage.social/app/resources/87203debda8b"
            } else {
                "https://www.entourage.social/app/resources/eMU_InNSSJbE"
            }
            try {
                startActivity(Intent(Intent.ACTION_VIEW, urlString.toUri()))
            } catch (_: Exception) {
            }
        },
        onToolsDonate = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Click_Home_Donation_Banner)
            try {
                startActivity(Intent(Intent.ACTION_VIEW, "http://don.entourage.social/?cid=97".toUri()))
            } catch (_: Exception) {
            }
        },
        onPedagoClick = { openPedago(it) },
        onConnectionAction = { suggestion ->
            suggestion.suggestedUserInfo?.id?.let { userId ->
                discussionsPresenter.createOrGetConversation(userId.toString())
            }
            homePresenter.dismissSuggestion(suggestion.id, "actioned")
            ui.connectionSuggestion = null
        },
        onConnectionDismiss = { suggestion ->
            homePresenter.dismissSuggestion(suggestion.id, "dismissed")
            ui.connectionSuggestion = null
        },
        onNextStepAction = { suggestion ->
            homePresenter.dismissSuggestion(suggestion.id, "actioned")
            ui.nextStepSuggestion = null
            suggestion.suggestedEntourageInfo?.id?.let { entourageId ->
                when (suggestion.suggestedAction) {
                    "join_event" -> {
                        startActivity(
                            Intent(
                                requireContext(),
                                social.entourage.android.events.details.feed.EventFeedActivity::class.java
                            ).putExtra(Const.EVENT_ID, entourageId)
                        )
                    }
                    else -> {}
                }
            }
        },
        onNextStepDismiss = { suggestion ->
            homePresenter.dismissSuggestion(suggestion.id, "dismissed")
            ui.nextStepSuggestion = null
        },
    )

    private fun setupSuggestions(anchor: View) {
        if (SuggestionFeature.ENABLED) {
            suggestionSection = HomeSuggestionSection(
                fragment = this,
                viewModel = ViewModelProvider(requireActivity())[HomeSuggestionViewModel::class.java],
                snackbarAnchor = anchor,
                onSayHello = { userId -> discussionsPresenter.createOrGetConversation(userId.toString()) },
            ).also {
                it.start()
                it.setSuppressed(ui.journeyVisible)
                ui.onJourneyVisibilityChanged = { journeyVisible -> suggestionSection?.setSuppressed(journeyVisible) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity)
        updateAvatar()
        userPresenter.user.observe(viewLifecycleOwner, userObserver)

        if (MainActivity.shouldLaunchOnboarding) {
            MainActivity.shouldLaunchOnboarding = false
        }
    }

    override fun onResume() {
        super.onResume()
        resetFilter()
        callToInitHome()
        suggestionSection?.onResume()
        actionsPresenter.getUnreadCount()
        sendUserDiscussionStatus()

        val mainActivity = requireActivity() as? MainActivity
        if (mainActivity?.getFromDeepLGoWelcomeVideo() == true) {
            mainActivity.setGoWelcomeVideoFromDeepL(false)
            showVideoModal()
        }
    }

    override fun onDestroyView() {
        composeRoot = null
        userPresenter.user.removeObserver(userObserver)
        suggestionSection = null
        super.onDestroyView()
    }

    private fun runHomeEntryGatingIfNeeded() {
        if (!isAdded) return
        if (hasRunEntryGating) return

        val currentUser = user ?: return
        val prefs = EntourageApplication.get().sharedPreferences

        val isZonePopupAlreadyShown = prefs.getBoolean(PREF_GATING_ZONE_SHOWN, false)
        if (!isZonePopupAlreadyShown) {
            val missingGoal = isUserMissingRole(currentUser)
            val missingZone = isUserMissingZone(currentUser)

            if (missingGoal || missingZone) {
                prefs.edit { putBoolean(PREF_GATING_ZONE_SHOWN, true) }
                hasRunEntryGating = true
                presentCriticalOnboarding(currentUser, missingGoal, missingZone)
                return
            }
        }

        val isNotifPopupAlreadyShown = prefs.getBoolean(PREF_GATING_NOTIF_SHOWN, false)
        val areNotificationsEnabled =
            NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()

        updateTokenForNotificationState(areNotificationsEnabled)

        if (!isNotifPopupAlreadyShown) {
            if (!areNotificationsEnabled) {
                prefs.edit { putBoolean(PREF_GATING_NOTIF_SHOWN, true) }
                hasRunEntryGating = true
                presentNotificationDemand()
                return
            }
        }

        val isEnhancedPopupAlreadyShown =
            prefs.getBoolean(PREF_GATING_ENHANCED_ONBOARDING_SHOWN, false)
        val hasCompletedEnhanced = prefs.getBoolean(PREF_ENHANCED_ONBOARDING_COMPLETED, false)

        if (!isEnhancedPopupAlreadyShown && !hasCompletedEnhanced) {
            prefs.edit { putBoolean(PREF_GATING_ENHANCED_ONBOARDING_SHOWN, true) }
            hasRunEntryGating = true
            presentEnhancedOnboardingIntro()
            return
        }

        hasRunEntryGating = true
    }

    private fun presentCriticalOnboarding(user: User, missingGoal: Boolean, missingZone: Boolean) {
        if (missingGoal) {
            OnboardingStartActivity.FRAGMENT_NUMBER = 3
            startActivity(Intent(requireActivity(), OnboardingStartActivity::class.java))
            requireActivity().overrideTransitionCompat(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            return
        }

        if (missingZone) {
            val goal = user.goal
            val typeForZone = when {
                goal.equals(User.USER_GOAL_ASSO, ignoreCase = true) -> OnboardingZoneChoiceActivity.UserType.ASSO
                goal.equals(User.USER_GOAL_ALONE, ignoreCase = true) -> OnboardingZoneChoiceActivity.UserType.BE_ENTOUR
                else -> OnboardingZoneChoiceActivity.UserType.ENTOUR
            }
            startActivity(OnboardingZoneChoiceActivity.newIntent(requireContext(), typeForZone))
            requireActivity().overrideTransitionCompat(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            return
        }
    }

    private fun isUserMissingRole(user: User): Boolean {
        val goal = user.goal
        return goal.isNullOrBlank() || goal == User.USER_GOAL_NONE
    }

    private fun isUserMissingZone(user: User): Boolean {
        val a1 = user.address
        val a2 = user.addressSecondary
        val ok1 = isAddressValid(a1)
        val ok2 = isAddressValid(a2)
        return !(ok1 || ok2)
    }

    private fun isAddressValid(address: User.Address?): Boolean {
        if (address == null) return false
        val hasCoords = address.latitude != 0.0 && address.longitude != 0.0
        val hasLabel = address.displayAddress.isNotBlank()
        val hasPlaceId = !address.googlePlaceId.isNullOrBlank()
        return hasCoords || hasLabel || hasPlaceId
    }

    private fun presentNotificationDemand() {
        val intent = Intent(requireContext(), NotificationDemandActivity::class.java)
        startActivity(intent)
        requireActivity().overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun presentEnhancedOnboardingIntro() {
        val intent = Intent(requireActivity(), EnhancedOnboarding::class.java)
        startActivity(intent)
        requireActivity().overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun updateTokenForNotificationState(allowed: Boolean) {
        if (allowed) {
            sendToken()
        } else {
            deleteToken()
        }
    }

    private fun checkNotificationStatus() {
        val areNotificationsEnabled =
            NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        if (areNotificationsEnabled) {
            AnalyticsEvents.logEvent(AnalyticsEvents.has_user_activated_notif)
            FirebaseMessaging.getInstance().fcmTokenTask.addOnSuccessListener { _ ->
                AnalyticsEvents.logEvent(AnalyticsEvents.user_have_notif_and_token)
            }
            FirebaseMessaging.getInstance().fcmTokenTask.addOnFailureListener { exception ->
                Timber.e(exception, "FCM Token: Failed to retrieve token")
                AnalyticsEvents.logEvent(AnalyticsEvents.user_have_notif_and_no_token + "_" + user?.id)
            }
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.has_user_disabled_notif)
            (requireActivity() as? MainActivity)?.sendRegistrationToServer("")
        }
    }

    private fun increaseCounter() {
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        var count = sharedPreferences.getInt("COUNT_DISCUSSION_ASK", 0)
        sharedPreferences.edit { putInt("COUNT_DISCUSSION_ASK", ++count) }
    }

    private fun sendUserDiscussionStatus() {
        if (!isAdded) return
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        val isInterested = sharedPreferences.getBoolean("DISCUSSION_INTERESTED", false)
        val userRefused = sharedPreferences.getBoolean("USER_REFUSED_POPUP", false)
        if (userRefused) return
        userPresenter.updateUser(isInterested)
    }

    private fun sendToken() {
        FirebaseMessaging.getInstance().fcmTokenTask.addOnSuccessListener { token ->
            (activity as? MainActivity)?.sendRegistrationToServer(token)
        }
    }

    private fun deleteToken(){
        (requireActivity() as? MainActivity)?.deleteApplicationInfo {
            //TODO WHY WHY WHY
            (requireActivity() as? MainActivity)?.sendRegistrationToServer("")

        }
    }

    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        EntourageApplication.get().mainActivity?.let {
            val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
            viewModel.badgeCount.postValue(unreadMessages)
        }
    }

    private fun callToInitHome() {
        if (isAdded) {
            EntourageApplication.get().me()?.id?.let { meId ->
                homePresenter.getAllEvents(
                    pageEvent,
                    nbOfItemForHozrizontalList,
                    currentFilters.travel_distance(),
                    currentFilters.latitude(),
                    currentFilters.longitude(),
                    "future"
                )
                homePresenter.getPedagogicalResources()
                homePresenter.getInitialPedagogicalResources()
                homePresenter.getNotificationsCount()
                // ATTENTION: getWelcomeResource() a été retiré d'ici pour éviter l'auto-validation !
                userPresenter.getUser(meId)
            }
        }
    }

    private fun doTotalchecksumToDisplayHomeFirstTime() {
        totalchecksum++
        // Le squelette laisse la place au contenu une fois les 4 réponses initiales arrivées (animation d'entrée jouée par l'écran)
        if (totalchecksum >= 4) {
            ui.contentShown = true
        }
    }

    private fun resetFilter() {
        currentFilters = EventActionLocationFilters()
        currentSectionsFilters = ActionSectionFilters()
    }

    private fun setObservations() {
        setupPresenterObservers()
        setupDiscussionObservers()
    }

    private fun setupPresenterObservers() {
        homePresenter.summary.observe(viewLifecycleOwner) { updateContributionsView(it) }
        homePresenter.getAllEvents.observe(viewLifecycleOwner) { handleEvent(it) }
        homePresenter.getAllActions.observe(viewLifecycleOwner) { handleAction(it) }
        homePresenter.pedagogicalContent.observe(viewLifecycleOwner) { handlePedago(it) }
        homePresenter.pedagogicalInitialContent.observe(viewLifecycleOwner) { handleInitialPedago(it) }
        homePresenter.notifsCount.observe(viewLifecycleOwner) { updateNotifsCount(it) }
        homePresenter.welcomeResource.observe(viewLifecycleOwner) { pedago ->
            welcomeVideoUrl = pedago.url
            welcomeVideoHtml = pedago.html

            currentVideoWebView?.let { webView ->
                if (!welcomeVideoUrl.isNullOrBlank()) {
                    loadCleanVideo(webView, welcomeVideoUrl!!)
                }
            }
        }
        homePresenter.suggestionData.observe(viewLifecycleOwner) { response ->
            ui.connectionSuggestion = response?.connection
            ui.nextStepSuggestion = response?.nextStep
        }
        actionsPresenter.unreadMessages.observe(viewLifecycleOwner) { updateUnreadCount(it) }
    }

    private fun setupDiscussionObservers() {
        discussionsPresenter.newConversation.observe(viewLifecycleOwner) { conversation ->
            conversation?.let {
                startActivity(
                    Intent(requireContext(), DetailConversationActivity::class.java)
                        .putExtra(Const.ID, it.id)
                        .putExtra(Const.POST_ID, it.id)
                        .putExtra(Const.POST_AUTHOR_ID, it.user?.id)
                        .putExtra(Const.IS_CONVERSATION_1TO1, true)
                        .putExtra(Const.IS_MEMBER, true)
                        .putExtra(Const.IS_CONVERSATION, true)
                        .putExtra(Const.HAS_TO_SHOW_MESSAGE, it.hasToShowFirstMessage())
                )
            }
        }
    }


    fun handleEvent(allEvent: MutableList<Events>?) {
        if (allEvent == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        ui.events = allEvent.toList()
        ui.eventsVisible = allEvent.isNotEmpty()
    }

    fun handleAction(allAction: MutableList<Action>?) {
        if (allAction == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        isActionEmpty = allAction.isEmpty()

        ui.actions = allAction.toList()

        val showActions = !isActionEmpty
        var title = getString(R.string.home_title_action)
        var subtitle = getString(R.string.home_subtitle_action)
        var btnText = getString(R.string.home_btn_more_action)
        var clickListener: () -> Unit = {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Demand_All)
            (requireActivity() as? MainActivity)?.goDemand()
        }

        if (isContribution) {
            title = getString(R.string.home_title_action_contrib)
            subtitle = getString(R.string.home_subtitle_action_contrib)
            btnText = getString(R.string.home_btn_more_action_contrib)
            clickListener = {
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Contrib_All)
                val mainActivity = (requireActivity() as? MainActivity)
                mainActivity?.goContrib()
            }
        }

        ui.actionTitle = title
        ui.actionSubtitle = subtitle
        ui.actionButtonText = btnText
        ui.onActionButtonClick = clickListener
        ui.actionsVisible = showActions
    }

    fun handleInitialPedago(allPedago: MutableList<Pedago>?) {
        if (allPedago == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        ui.initialPedagos = allPedago.toList()

        var show = allPedago.isNotEmpty()
        val me = EntourageApplication.me(activity)
        if (show && me != null) {
            val involvements = me.involvements
            val hasResourcesWish = involvements.any { it.equals("resources", ignoreCase = true) }
            if (!hasResourcesWish) {
                show = false
            }
        }

        ui.initialPedagoVisible = show
    }

    fun handlePedago(allPedago: MutableList<Pedago>?) {
        if (allPedago == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        val pedagos: MutableList<Pedago> = mutableListOf()
        for (pedago in allPedago) {
            if (pedagos.size > 2) {
                break
            }
            if (pedago.watched == false) {
                pedagos.add(pedago)
            }
        }

        for (pedago in allPedago) {
            pedago.id?.let { id ->
                val createEventId: Int = BuildConfig.PEDAGO_CREATE_EVENT_ID
                val createGroupId: Int = BuildConfig.PEDAGO_CREATE_GROUP_ID
                if (id == createEventId) {
                    this.pedagoItemForCreateEvent = pedago
                }
                if (id == createGroupId) {
                    this.pedagoItemForCreateGroup = pedago
                }
            }
        }

        ui.pedagos = pedagos.toList()
        // Le titre n'est affiché que s'il reste au moins un contenu non lu (comme sur iOS).
        ui.pedagoTitleVisible = pedagos.isNotEmpty()
        homePresenter.getSummary()
    }

    private fun updateContributionsView(summary: Summary) {
        if (!isAdded) return

        handleWelcomeJourneyState(summary)

        val isAssociationFromSummary = summary.association == true
        EntourageApplication.get().sharedPreferences.edit {
            putBoolean(PREF_IS_ASSOCIATION_FROM_SUMMARY, isAssociationFromSummary)
        }
        EnhancedOnboarding.isAssociationFromSummary = isAssociationFromSummary
        EnhancedOnboarding.preference = summary.preference ?: ""
        onActionUnclosed(summary)
        if (summary.signablePermission != null) {
            signablePermission = summary.signablePermission!!
            HomeState.signablePermission = summary.signablePermission!!
        }

        isContribution = summary.preference.equals("contribution")
        isContribProfile = isContribution

        ui.isContribution = isContribution

        if (isContribution) {
            homePresenter.getAllContribs(
                0,
                nbOfItemForVerticalList,
                currentFilters.travel_distance(),
                currentFilters.latitude(),
                currentFilters.longitude(),
                currentSectionsFilters.getSectionsForWS()
            )
        } else {
            homePresenter.getAllDemands(
                0,
                nbOfItemForVerticalList,
                currentFilters.travel_distance(),
                currentFilters.latitude(),
                currentFilters.longitude(),
                currentSectionsFilters.getSectionsForWS()
            )
        }
    }

    private fun updateNotifsCount(count: Int) {
        if (count > 0) {
            ui.notifBadgeText = if (count > 9) "9+" else count.toString()
            ui.notifBadgeVisible = true
        } else {
            ui.notifBadgeVisible = false
        }
    }

    private fun updateAvatar() {
        ui.avatarUrl = user?.avatarURL
    }

    private fun updateUser(user: User) {
        this.user = user
        updateAvatar()

    }

    private fun checkBirthday() {
        if (user?.isBirthday == true) {
            val prefs = EntourageApplication.get().sharedPreferences
            val lastShownYear = prefs.getInt(PREF_BIRTHDAY_SHOWN_YEAR, -1)
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

            if (lastShownYear != currentYear) {
                prefs.edit { putInt(PREF_BIRTHDAY_SHOWN_YEAR, currentYear) }
                if (isAdded) {
                    startActivity(Intent(requireContext(), BirthdayActivity::class.java))
                }
            }
        }
    }

    private fun onActionUnclosed(summary: Summary) {
        summary.unclosedAction?.let { unclosedAction ->
            if (unclosedAction.actionType == "solicitation") {
                handleSolicitationAction(unclosedAction)
            }
            if (unclosedAction.actionType == "contribution") {
                handleContributionAction(unclosedAction)
            }
        }
    }

    private fun handleSolicitationAction(unclosedAction: SummaryAction) {
        AnalyticsEvents.logEvent(AnalyticsEvents.View__StateDemandPop__Day10)
        unclosedAction.title?.let { contentText ->
            CustomAlertDialog.showForLastActionOneDemand(
                requireContext(),
                getString(R.string.custom_dialog_action_title_one_demand),
                contentText,
                getString(R.string.custom_dialog_action_content_one_demande),
                getString(R.string.yes),
                onNo = {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateDemandPop__No__Day10)
                    AnalyticsEvents.logEvent(AnalyticsEvents.View__StateDemandPop__No__Day10)
                    CustomAlertDialog.showForLastActionTwo(
                        requireContext(),
                        getString(R.string.custom_dialog_action_title_two),
                        getString(R.string.custom_dialog_action_content_two_demande),
                        getString(R.string.custom_dialog_action_two_button_contrib),
                        onYes = {
                            (requireActivity() as? MainActivity)?.goDemand()
                            AnalyticsEvents.logEvent(AnalyticsEvents.Clic__SeeDemand__Day10)
                        }
                    )
                },
                onYes = {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateDemandPop__Yes__Day10)
                    AnalyticsEvents.logEvent(AnalyticsEvents.View__DeleteDemandPop__Day10)
                    unclosedAction.id?.let { id ->
                        actionsPresenter.cancelAction(id, true, true, "")
                        CustomAlertDialog.showForLastActionThree(
                            requireContext(),
                            getString(R.string.custom_dialog_action_title_three),
                            getString(R.string.custom_dialog_action_content_three_demande)
                        )
                    }
                }
            )
        }
    }

    private fun handleContributionAction(unclosedAction: SummaryAction) {
        AnalyticsEvents.logEvent(AnalyticsEvents.View__StateContribPop__Day10)
        unclosedAction.title?.let { contentText ->
            CustomAlertDialog.showForLastActionOneContrib(
                requireContext(),
                getString(R.string.custom_dialog_action_title_one_contrib),
                contentText,
                getString(R.string.custom_dialog_action_content_one_contrib),
                getString(R.string.yes),
                onNo = {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateContribPop__No__Day10)
                    AnalyticsEvents.logEvent(AnalyticsEvents.View__StateContribPop__No__Day10)
                    CustomAlertDialog.showForLastActionTwo(
                        requireContext(),
                        getString(R.string.custom_dialog_action_title_two),
                        getString(R.string.custom_dialog_action_content_two_contrib),
                        getString(R.string.custom_dialog_action_two_button_demand),
                        onYes = {
                            (requireActivity() as? MainActivity)?.goDemand()
                            AnalyticsEvents.logEvent(AnalyticsEvents.Clic__SeeContrib__Day10)
                        }
                    )
                },
                onYes = {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateContribPop__Yes__Day10)
                    AnalyticsEvents.logEvent(AnalyticsEvents.View__DeleteContribPop__Day10)
                    unclosedAction.id?.let { id ->
                        actionsPresenter.cancelAction(id, false, true, "")
                        CustomAlertDialog.showForLastActionThree(
                            requireContext(),
                            getString(R.string.custom_dialog_action_title_three),
                            getString(R.string.custom_dialog_action_content_three_contrib)
                        )
                    }
                }
            )
        }
    }


    override fun onHomeChangeLocationUpdateClearFragment() {
        callToInitHome()
    }

    private fun loadCleanVideo(webView: android.webkit.WebView, videoUrl: String) {
        val cleanUrl = if (videoUrl.contains("?")) "$videoUrl&rel=0" else "$videoUrl?rel=0"
        val customHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
                <style>
                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000000; overflow: hidden; }
                    iframe { width: 100%; height: 100%; border: none; }
                </style>
            </head>
            <body>
                <iframe src="$cleanUrl" frameborder="0" allow="autoplay; encrypted-media; picture-in-picture" allowfullscreen></iframe>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL("https://www.entourage.social", customHtml, "text/html", "utf-8", null)
    }

    companion object {
        // États partagés avec le reste de l'app et les tests e2e : ceux de HomeFragment font foi.
        var isContribProfile: Boolean
            get() = HomeFragment.isContribProfile
            set(value) { HomeFragment.isContribProfile = value }
        var signablePermission: Boolean
            get() = HomeFragment.signablePermission
            set(value) { HomeFragment.signablePermission = value }
        private const val PREF_GATING_ZONE_SHOWN = "PREF_GATING_ZONE_SHOWN"
        private const val PREF_GATING_NOTIF_SHOWN = "PREF_GATING_NOTIF_SHOWN"
        internal const val PREF_GATING_ENHANCED_ONBOARDING_SHOWN =
            "PREF_GATING_ENHANCED_ONBOARDING_SHOWN"

        const val PREF_ENHANCED_ONBOARDING_COMPLETED = "PREF_ENHANCED_ONBOARDING_COMPLETED"
        private const val PREF_IS_ASSOCIATION_FROM_SUMMARY = "PREF_IS_ASSOCIATION_FROM_SUMMARY"
        private const val PREF_BIRTHDAY_SHOWN_YEAR = "PREF_BIRTHDAY_SHOWN_YEAR"

        // Variable pour mémoriser si le parcours était fini au lancement
        private var hasInitiallyCompletedAll: Boolean? = null
        // On stocke l'ID pour réinitialiser l'état si on change de compte
        private var currentUserIdForJourney: Int? = null
        // Le parcours a été affiché pendant cette session (félicitations quand le back le retire car terminé)
        private var journeyShownThisSession = false
        // Le back a été informé du masquage manuel pour cet utilisateur (sinon on le lui redit une fois)
        private var hideSyncedForUserId: Int? = null

        private const val MENU_HIDE_JOURNEY = 1

        private fun welcomeJourneyLastConnectionKey(userId: Int) = "PREF_WELCOME_JOURNEY_LAST_CONNECTION_$userId"

        private fun welcomeJourneyHiddenKey(userId: Int) = "PREF_WELCOME_JOURNEY_HIDDEN_$userId"

        /**
         * Debug / tests e2e uniquement (ignoré hors BuildConfig.DEBUG) : remplace `summary.events`
         * par de fausses données pour piloter l'état du parcours de bienvenue.
         */
        var summaryEventsOverride: List<String>?
            get() = HomeFragment.summaryEventsOverride
            set(value) { HomeFragment.summaryEventsOverride = value }
    }
}

