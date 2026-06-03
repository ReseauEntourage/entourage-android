package social.entourage.android.home

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.animation.doOnEnd
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.MainPresenter
import social.entourage.android.R
import android.net.Uri
import android.widget.PopupWindow
import com.google.android.material.bottomsheet.BottomSheetDialog

import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSectionFilters
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Help
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Summary
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.databinding.FragmentHomeBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.events.create.CommunicationHandler
import social.entourage.android.groups.details.rules.GroupRulesActivity
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.chatbot.ChatBotBottomSheet
import social.entourage.android.home.pedago.OnItemClick
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.notifications.InAppNotificationsActivity
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.onboarding.onboard.OnboardingStartActivity
import social.entourage.android.onboarding.onboard.OnboardingZoneChoiceActivity
import social.entourage.android.profile.MyProfileFullActivity
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.small_talks.SmallTalkIntroActivity
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.user.UserPresenter
import timber.log.Timber

class HomeFragment : Fragment(), OnHomeChangeLocationUpdate {

    private lateinit var binding: FragmentHomeBinding
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
    private val NEW_MARGIN = 10
    private val DEFAULT_MARGIN = 80
    private var isAnimating = false
    private var pedagoItemForCreateEvent: Pedago? = null
    private var pedagoItemForCreateGroup: Pedago? = null
    private var checksum = 0
    private var totalchecksum = 0
    private var isEventsEmpty = false
    private var isActionEmpty = false
    private var isContribution = false
    private lateinit var actionsPresenter: ActionsPresenter
    private val smallTalkViewModel: SmallTalkViewModel by lazy {
        ViewModelProvider(this).get(SmallTalkViewModel::class.java)
    }
    private var isRequestLoaded = false
    private var currentRequests: List<UserSmallTalkRequest> = emptyList()
    private val REQUEST_CODE_NATIONAL_GROUPS = 1001

    // Variables pour la vidéo
    private var welcomeVideoUrl: String? = null
    private var welcomeVideoHtml: String? = null
    private var currentVideoWebView: android.webkit.WebView? = null // Permet d'injecter la vidéo une fois reçue

    // Adapters
    private lateinit var concatAdapter: ConcatAdapter

    // Welcome Journey
    private lateinit var welcomeJourneyAdapter: HomeWelcomeJourneyAdapter
    private val homeSkeletonAdapter = HomeSkeletonAdapter()
    private var completedJourneySteps = mutableSetOf<Int>()

    // Sensibilisation (Initial Pedago)
    private lateinit var initialPedagoHeaderAdapter: HomeSectionHeaderAdapter
    private lateinit var homeInitialPedagoAdapter: HomeInitialPedagoAdapter
    private lateinit var initialPedagoWrapperAdapter: HomeHorizontalWrapperAdapter

    // Small Talk
    private lateinit var smallTalkHeaderAdapter: HomeSectionHeaderAdapter
    private lateinit var homeSmallTalkAdapter: HomeSmallTalkAdapter
    // private lateinit var smallTalkWrapperAdapter: HomeHorizontalWrapperAdapter

    // Actions
    private lateinit var actionHeaderAdapter: HomeSectionHeaderAdapter
    private lateinit var homeActionAdapter: HomeActionAdapter
    private lateinit var actionWrapperAdapter: HomeHorizontalWrapperAdapter
    private lateinit var actionButtonAdapter: HomeSectionButtonAdapter

    // Events
    private lateinit var eventHeaderAdapter: HomeSectionHeaderAdapter
    private lateinit var homeEventAdapter: HomeEventAdapter
    private lateinit var eventWrapperAdapter: HomeHorizontalWrapperAdapter
    private lateinit var eventButtonAdapter: HomeSectionButtonAdapter

    // Groups
    private lateinit var groupHeaderAdapter: HomeSectionHeaderAdapter
    private lateinit var homeGroupAdapter: HomeGroupAdapter
    private lateinit var groupWrapperAdapter: HomeHorizontalWrapperAdapter
    private lateinit var groupButtonAdapter: HomeSectionButtonAdapter

    // Map & Hors Zone
    private lateinit var horsZoneAdapter: HomeSingleLayoutAdapter

    // Tools
    private lateinit var homeToolsAdapter: HomeToolsAdapter

    // Pedago
    private lateinit var pedagoHeaderAdapter: HomeSectionHeaderAdapter
    private lateinit var homePedagoAdapter: HomePedagoAdapter

    // Moderator
    private lateinit var homeModeratorAdapter: HomeModeratorAdapter

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

    private fun handleWelcomeJourneyState(summary: Summary) {
        if (!::welcomeJourneyAdapter.isInitialized) return

        val currentUser = EntourageApplication.me(requireContext())

        // Sécurité pour réinitialiser l'état local si l'utilisateur s'est déconnecté/reconnecté avec un autre compte
        if (currentUser?.id != currentUserIdForJourney) {
            hasInitiallyCompletedAll = null
            currentUserIdForJourney = currentUser?.id
        }

        // Règle 1: Masquer si l'utilisateur est partenaire
        if (currentUser?.partner != null) {
            welcomeJourneyAdapter.setVisible(false)
            return
        }

        // Récupération des informations de validation via le WS uniquement
        val events = summary.events
        val isStep1Done = events?.contains("onboarding.resource.welcome_watched") == true
        val isStep2Done = events?.contains("onboarding.neighborhood.nationals") == true
        val isStep3Done = events?.contains("onboarding.outing.webinar_or_first_steps") == true
        val isStep4Done = events?.contains("onboarding.outing.papotages") == true

        val allCompleted = isStep1Done && isStep2Done && isStep3Done && isStep4Done

        // Règle 2: Initialisation du companion object au premier passage
        if (hasInitiallyCompletedAll == null) {
            hasInitiallyCompletedAll = allCompleted
        }

        // Règle 3: Si tout était validé dès le lancement de l'application, on masque tout complètement
        if (hasInitiallyCompletedAll == true) {
            welcomeJourneyAdapter.setVisible(false)
        } else {
            // Sinon on affiche le composant et on met à jour les données
            welcomeJourneyAdapter.setVisible(true)
            welcomeJourneyAdapter.updateAllSteps(isStep1Done, isStep2Done, isStep3Done, isStep4Done)

            // Détection du moment où le parcours est terminé pour afficher le Tooltip de célébration
            val previouslyCompletedSize = completedJourneySteps.size
            completedJourneySteps.clear()
            if (isStep1Done) completedJourneySteps.add(1)
            if (isStep2Done) completedJourneySteps.add(2)
            if (isStep3Done) completedJourneySteps.add(3)
            if (isStep4Done) completedJourneySteps.add(4)

            // Si on vient juste de finir les 4 étapes (n'était pas à 4 avant)
            if (allCompleted && previouslyCompletedSize < 4) {
                showCongratDialog(summary)
            }
        }
    }

    private fun handleWelcomeJourneyClick(stepIndex: Int) {
        when (stepIndex) {
            1 -> showVideoModal()
            2 -> showNationalGroupsActivity()
            3 -> {
                val intent = Intent(requireContext(), social.entourage.android.events.list.WelcomeEventsListActivity::class.java)
                intent.putExtra("TYPE", "welcome")
                startActivity(intent)
            }
            4 -> {
                val intent = Intent(requireContext(), social.entourage.android.events.list.WelcomeEventsListActivity::class.java)
                intent.putExtra("TYPE", "papotages")
                startActivity(intent)
            }
        }
    }

    private fun showNationalGroupsActivity() {
        if (!isAdded) return
        
        val intent = Intent(requireContext(), NationalGroupsActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_NATIONAL_GROUPS)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_NATIONAL_GROUPS && resultCode == Activity.RESULT_OK) {
            // Marquer l'étape comme complétée
            markWelcomeJourneyStepCompleted(2)
            
            // Afficher la snackbar
            showGroupsSnackbar()
            
            // Rafraîchir l'état du parcours
            homePresenter.getSummary()
        }
    }

    private fun showGroupsSnackbar() {
        if (!isAdded) return
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            binding.root,
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
        binding = FragmentHomeBinding.inflate(layoutInflater)

        // CORRECTION: On laisse le header visible pour qu'il prenne sa place tout de suite
        binding.homeHeader.visibility = View.VISIBLE

        mainPresenter = MainPresenter(requireActivity() as MainActivity)
        binding.progressBar.visibility = View.GONE
        homePresenter = ViewModelProvider(requireActivity()).get(HomePresenter::class.java)
        actionsPresenter = ViewModelProvider(requireActivity()).get(ActionsPresenter::class.java)

        setupAdapters()
        setupRecyclerView()

        AnalyticsEvents.logEvent(AnalyticsEvents.View__Home)
        if (EnhancedOnboarding.shouldNotDisplayCampain == true) {
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.home_activate_firebase_message)
        }

        setObservations()
        setNotifButton()
        setProfileButton()
        setRecyclerViewScrollListener()
        checkNotificationStatus()
        increaseCounter()
        updatePaddingTopForEdgeToEdge(binding.homeHeader)

        binding.chatbotButton.setOnClickListener {
            ChatBotBottomSheet().show(parentFragmentManager, "chatbot")
        }

        smallTalkViewModel.userRequests.observe(viewLifecycleOwner) { requests ->
            currentRequests = requests
            composeSmallTalkItemsSimplified()
        }

        loadSmallTalkItems()
        return binding.root
    }

    private fun testNotifDemandePage() {
        // Appui long existant...
        binding.ivLogoHome.setOnLongClickListener {
            // ... ton code existant ...
            true
        }

        // CLIC SIMPLE : Injection dans le pipeline de notification
        binding.ivLogoHome.setOnClickListener {
            // 1. Le JSON exact de la payload (avec stage="birthday" qui est la clé critique pour le PendingIntent)
            val jsonPayload = """
            {
                "sender": "L'équipe Entourage",
                "object": "Joyeux anniversaire 🎉",
                "content": {
                    "message": "On est heureux de vous compter parmi nous. Cliquez ici pour lire notre message d'anniversaire !",
                    "extra": {
                        "stage": "birthday",
                        "tracking": "birthday",
                        "popup": "birthday"
                    }
                }
            }
            """

            try {
                // 2. On désérialise le JSON en objet métier PushNotificationMessage
                val message = com.google.gson.Gson().fromJson(
                    jsonPayload,
                    social.entourage.android.api.model.notification.PushNotificationMessage::class.java
                )

                // 3. On balance ça au Manager.
                // Il va lire "stage: birthday", créer le PendingIntent avec "goBirthday=true" et afficher la notif système.
                social.entourage.android.notifications.PushNotificationManager.handlePushNotification(message, requireContext())

                // (Optionnel) Log pour confirmer le tir
                social.entourage.android.tools.log.AnalyticsEvents.logEvent("debug_birthday_proc_triggered")

            } catch (e: Exception) {
                android.util.Log.e("DEBUG_NOTIF", "Erreur parsing JSON: ${e.message}")
            }
        }
    }


    private fun setupAdapters() {
        val viewPool = RecyclerView.RecycledViewPool()

        // 0. Welcome Journey
        welcomeJourneyAdapter = HomeWelcomeJourneyAdapter(requireContext()) { stepIndex ->
            handleWelcomeJourneyClick(stepIndex)
        }


        // 1. Initial Pedago
        initialPedagoHeaderAdapter = HomeSectionHeaderAdapter()
        homeInitialPedagoAdapter = HomeInitialPedagoAdapter(object : OnItemClick {
            override fun onItemClick(pedagogicalContent: Pedago) {
                if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
                    val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
                    intent.putExtra(Const.ID, pedagogicalContent.id)
                    PedagoDetailActivity.setPedagoId(pedagogicalContent.id)
                    requireActivity().startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                }
            }
        })
        initialPedagoWrapperAdapter = HomeHorizontalWrapperAdapter(homeInitialPedagoAdapter, viewPool)

        // 2. Small Talk
        smallTalkHeaderAdapter = HomeSectionHeaderAdapter()
        homeSmallTalkAdapter = HomeSmallTalkAdapter(
            onStartClick = {
                AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_BONNES_ONDES_START_DISCUSSION)
                startActivity(Intent(requireContext(), SmallTalkIntroActivity::class.java))
            },
            onViewClick = {
                AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_BONNES_ONDES_VIEW_MESSAGES)
                (requireActivity() as? MainActivity)?.goConv(isSmallTalkFilter = true)
            },
            onMatchingClick = {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.small_talk_subtitle_waiting),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onLaunchNewClick = {
                startActivity(Intent(requireContext(), SmallTalkIntroActivity::class.java))
            },
            requireContext()
        )
        // smallTalkWrapperAdapter = HomeHorizontalWrapperAdapter(homeSmallTalkAdapter, viewPool)

        // 3. Actions
        actionHeaderAdapter = HomeSectionHeaderAdapter()
        homeActionAdapter = HomeActionAdapter(false)
        actionWrapperAdapter = HomeHorizontalWrapperAdapter(homeActionAdapter, viewPool) // NOUVEAU
        actionButtonAdapter = HomeSectionButtonAdapter {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Demand_All)
            (requireActivity() as? MainActivity)?.goDemand()
        }

        // 4. Events
        eventHeaderAdapter = HomeSectionHeaderAdapter()
        homeEventAdapter = HomeEventAdapter(requireContext())
        eventWrapperAdapter = HomeHorizontalWrapperAdapter(homeEventAdapter, viewPool)
        eventButtonAdapter = HomeSectionButtonAdapter {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Event_All)
            (requireActivity() as? MainActivity)?.goEvent()
        }

        // 5. Groups
        groupHeaderAdapter = HomeSectionHeaderAdapter()
        homeGroupAdapter = HomeGroupAdapter()
        groupWrapperAdapter = HomeHorizontalWrapperAdapter(homeGroupAdapter, viewPool)
        groupButtonAdapter = HomeSectionButtonAdapter {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Group_All)
            val mainActivity = (requireActivity() as? MainActivity)
            mainActivity?.setGoDiscoverGroupFromDeepL(true)
            mainActivity?.goGroup()
        }

        // 6. Map & Hors Zone
        horsZoneAdapter = HomeSingleLayoutAdapter(R.layout.home_hors_zone) { view ->
            val button = view.findViewById<View>(R.id.button_hz_item)
            button.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Buffet)
                val urlString =
                    "https://reseauentourage.notion.site/Buffet-du-lien-social-69c20e089dbd483cb093e90ae2953a54"
                WebViewFragment.newInstance(urlString, 0, true)
                    .show(requireActivity().supportFragmentManager, WebViewFragment.TAG)
            }
        }
        horsZoneAdapter.setVisible(false)

        // 7. Tools
        homeToolsAdapter = HomeToolsAdapter(requireContext(),
            onMapClick = {
                AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Map)
                val intent = Intent(requireContext(), GDSMainActivity::class.java)
                startActivityForResult(intent, 0)
            },
            onPedagoClick = {
                AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Pedago)
                val intent = Intent(requireActivity(), PedagoListActivity::class.java)
                requireContext().startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            },
            onCharterClick = {
                // Sélection de l'URL selon l'environnement (DEBUG = preprod / RELEASE = prod)
                val urlString = if (BuildConfig.DEBUG) {
                    "https://preprod.entourage.social/app/resources/87203debda8b"
                } else {
                    "https://www.entourage.social/app/resources/eMU_InNSSJbE"
                }

                try {
                    val uri = android.net.Uri.parse(urlString)
                    val intent = Intent(Intent.ACTION_VIEW, uri)

                    startActivity(intent)
                } catch (e: Exception) {

                }
            }
        )

        // 8. Pedago
        pedagoHeaderAdapter = HomeSectionHeaderAdapter()

        homePedagoAdapter = HomePedagoAdapter(object : OnItemClick {
            override fun onItemClick(pedagogicalContent: Pedago) {
                if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
                    val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
                    intent.putExtra(Const.ID, pedagogicalContent.id)
                    PedagoDetailActivity.setPedagoId(pedagogicalContent.id)
                    requireActivity().startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                }
            }
        })

        // 9. Moderator
        homeModeratorAdapter = HomeModeratorAdapter { moderatorId ->
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Moderator)
            discussionsPresenter.createOrGetConversation(moderatorId.toString())
        }
    }

    private fun setupRecyclerView() {
        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()


        concatAdapter = ConcatAdapter(
            config,
            homeSkeletonAdapter
        )

        binding.rvHome.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = concatAdapter
            itemAnimator = null
            visibility = View.VISIBLE
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
        checksum = 0
        resetFilter()
        callToInitHome()
        actionsPresenter.getUnreadCount()
        sendUserDiscussionStatus()
        loadSmallTalkItems()

        val mainActivity = requireActivity() as? MainActivity
        if (mainActivity?.getFromDeepLGoWelcomeVideo() == true) {
            mainActivity.setGoWelcomeVideoFromDeepL(false)
            showVideoModal()
        }
    }

    private fun loadSmallTalkItems() {
        isRequestLoaded = false
        smallTalkViewModel.listUserRequests()
    }

    private fun composeSmallTalkItemsSimplified() {
        val items = mutableListOf<HomeSmallTalkItem>()
        val matchedRequests = currentRequests.filter { it.smalltalkId != null }
        val unmatchedRequestsCount = currentRequests.count { it.smalltalkId == null }

        if (currentRequests.isEmpty()) {
            items.add(HomeSmallTalkItem.MatchPossible)
        } else if (matchedRequests.isEmpty() && unmatchedRequestsCount > 0) {
            items.add(HomeSmallTalkItem.Waiting)
        } else if (matchedRequests.isNotEmpty()) {
            items.add(
                HomeSmallTalkItem.Active(
                    activeRequests = matchedRequests,
                    waitingCount = unmatchedRequestsCount,
                    totalCount = currentRequests.size
                )
            )
        }

        homeSmallTalkAdapter.submitList(items)

        val hasItems = items.isNotEmpty()
        smallTalkHeaderAdapter.update("", null, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        userPresenter.user.removeObserver(userObserver)
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
            requireActivity().overridePendingTransition(
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
            requireActivity().overridePendingTransition(
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
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun presentEnhancedOnboardingIntro() {
        val intent = Intent(requireActivity(), EnhancedOnboarding::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
            FirebaseMessaging.getInstance().token.addOnSuccessListener { _ ->
                AnalyticsEvents.logEvent(AnalyticsEvents.user_have_notif_and_token)
            }
            FirebaseMessaging.getInstance().token.addOnFailureListener { exception ->
                Timber.e("FCM Token: Failed to retrieve token :%s", exception)
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
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
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
        CommunicationHandler.resetValues()
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

    private fun checkSumEventAction() {
        checksum++
        val showHorsZone = (checksum == 2) && (isEventsEmpty && isActionEmpty)
        horsZoneAdapter.setVisible(showHorsZone)
    }

    private fun doTotalchecksumToDisplayHomeFirstTime() {
        totalchecksum++
        // CORRECTION: On fait un fade out sur la progress bar plutôt que de changer la visibilité du RV
        // Ça évite le saut de contenu
        if (totalchecksum >= 4) {
            if (concatAdapter.adapters.contains(homeSkeletonAdapter)) {
                concatAdapter.removeAdapter(homeSkeletonAdapter)
                concatAdapter.addAdapter(welcomeJourneyAdapter)
                concatAdapter.addAdapter(initialPedagoHeaderAdapter)
                concatAdapter.addAdapter(initialPedagoWrapperAdapter)
                concatAdapter.addAdapter(actionHeaderAdapter)
                concatAdapter.addAdapter(actionWrapperAdapter)
                concatAdapter.addAdapter(actionButtonAdapter)
                concatAdapter.addAdapter(eventHeaderAdapter)
                concatAdapter.addAdapter(eventWrapperAdapter)
                concatAdapter.addAdapter(eventButtonAdapter)
                concatAdapter.addAdapter(homeModeratorAdapter)
                concatAdapter.addAdapter(groupHeaderAdapter)
                concatAdapter.addAdapter(groupWrapperAdapter)
                concatAdapter.addAdapter(groupButtonAdapter)
                concatAdapter.addAdapter(horsZoneAdapter)
                concatAdapter.addAdapter(smallTalkHeaderAdapter)
                concatAdapter.addAdapter(homeSmallTalkAdapter)
                concatAdapter.addAdapter(homeToolsAdapter)
                concatAdapter.addAdapter(homePedagoAdapter)
                binding.rvHome.scheduleLayoutAnimation()
            }
            if (binding.progressBar.visibility == View.VISIBLE) {
                binding.progressBar.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.progressBar.visibility = View.GONE
                    }
                    .start()
            }
            if (binding.homeHeader.alpha < 1f) {
                binding.homeHeader.animate().alpha(1f).duration = 200
            }
        }
    }

    private fun resetFilter() {
        currentFilters = EventActionLocationFilters()
        currentSectionsFilters = ActionSectionFilters()
    }

    private fun setNotifButton() {
        binding.uiLayoutNotif.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Notif)
            val intent = Intent(requireContext(), InAppNotificationsActivity::class.java)
            intent.putExtra(Const.NOTIF_COUNT, homePresenter.notifsCount.value)
            startActivityForResult(intent, 0)
        }
    }

    private fun setObservations() {
        homePresenter.summary.observe(viewLifecycleOwner) { updateContributionsView(it) }
        homePresenter.getAllEvents.observe(viewLifecycleOwner) { handleEvent(it) }
        homePresenter.getAllActions.observe(viewLifecycleOwner) { handleAction(it) }
        homePresenter.pedagogicalContent.observe(viewLifecycleOwner) { handlePedago(it) }
        homePresenter.pedagogicalInitialContent.observe(viewLifecycleOwner) { handleInitialPedago(it) }
        homePresenter.notifsCount.observe(viewLifecycleOwner) { updateNotifsCount(it) }
        homePresenter.welcomeResource.observe(viewLifecycleOwner) { pedago ->
            welcomeVideoUrl = pedago.url
            welcomeVideoHtml = pedago.html

            // On injecte le contenu vidéo dès qu'il est reçu (uniquement si la modale est ouverte)
            currentVideoWebView?.let { webView ->
                if (!welcomeVideoUrl.isNullOrBlank()) {
                    loadCleanVideo(webView, welcomeVideoUrl!!)
                }
            }
        }
        actionsPresenter.unreadMessages.observe(viewLifecycleOwner) { updateUnreadCount(it) }
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

    fun handleGroup(allGroup: MutableList<Group>?) {
        if (allGroup == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        this.homeGroupAdapter.resetData(allGroup)
        groupHeaderAdapter.update(getString(R.string.home_title_group), getString(R.string.home_subtitle_group), false)
        groupWrapperAdapter.setVisible(false)
        groupButtonAdapter.update(getString(R.string.home_btn_more_group), false)
    }

    fun handleEvent(allEvent: MutableList<Events>?) {
        if (allEvent == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        val _offline_events: MutableList<Events> = mutableListOf()
        if (allEvent.isNotEmpty()) {
            for (event in allEvent) {
                if (event.online == false) {
                    _offline_events.add(event)
                }
            }
            isEventsEmpty = _offline_events.size == 0
        } else {
            isEventsEmpty = true
        }

        checkSumEventAction()
        this.homeEventAdapter.resetData(allEvent)

        val showEvents = allEvent.isNotEmpty()
        eventHeaderAdapter.update(getString(R.string.home_title_event), getString(R.string.home_subtitle_event), showEvents)
        eventWrapperAdapter.setVisible(showEvents)
        eventButtonAdapter.update(getString(R.string.home_btn_more_event), showEvents)
    }

    fun handleAction(allAction: MutableList<Action>?) {
        if (allAction == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        isActionEmpty = allAction.isEmpty()

        if (!isContribution) {
            checkSumEventAction()
        }
        this.homeActionAdapter.resetData(allAction)

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

        actionHeaderAdapter.update(title, subtitle, showActions)
        actionWrapperAdapter.setVisible(showActions) // NOUVEAU : On gère la visibilité du wrapper
        actionButtonAdapter.update(btnText, showActions, clickListener)
    }

    fun handleInitialPedago(allPedago: MutableList<Pedago>?) {
        if (allPedago == null) return
        doTotalchecksumToDisplayHomeFirstTime()

        this.homeInitialPedagoAdapter.resetData(allPedago)

        var show = allPedago.isNotEmpty()
        val me = EntourageApplication.me(activity)
        if (show && me != null) {
            val involvements = me.involvements
            val hasResourcesWish = involvements.any { it.equals("resources", ignoreCase = true) }
            if (!hasResourcesWish) {
                show = false
            }
        }

        initialPedagoHeaderAdapter.update(getString(R.string.home_title_sensibilisation), getString(R.string.home_subtitle_sensibilisation), show)
        initialPedagoWrapperAdapter.setVisible(show)
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

        this.homePedagoAdapter?.resetData(pedagos)
        homePresenter.getSummary()

        val show = allPedago.isNotEmpty()
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
        handleModerator(summary)
        if (summary.signablePermission != null) {
            HomeFragment.signablePermission = summary.signablePermission!!
        }

        isContribution = summary.preference.equals("contribution")
        isContribProfile = isContribution

        homeActionAdapter.setContrib(isContribution)

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

    private fun handleModerator(summary: Summary) {
        if (isAdded) {
            doTotalchecksumToDisplayHomeFirstTime()
            homeModeratorAdapter.updateSummary(summary)
        }
    }

    private fun updateNotifsCount(count: Int) {
        if (count > 0) {
            if (count > 9) {
                binding.tvNumberOfFilter.text = "9+"
            } else {
                binding.tvNumberOfFilter.text = count.toString()
            }
            binding.cardNotifNumber.visibility = View.VISIBLE
        } else {
            binding.cardNotifNumber.visibility = View.INVISIBLE
        }

        context?.resources?.let { resources ->
            binding.uiBellNotif.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_new_notif_off,
                    null
                )
            )
        }
    }

    private fun updateAvatar() {
        with(binding) {
            avatar.let { photoView ->
                user?.avatarURL?.let { avatarURL ->
                    Glide.with(requireActivity())
                        .load(avatarURL)
                        .placeholder(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(photoView)
                } ?: run {
                    photoView.setImageResource(R.drawable.placeholder_user)
                }
            }
        }
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

    private fun setProfileButton() {
        binding.avatar.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Tab__Profil)
            startActivityForResult(Intent(context, MyProfileFullActivity::class.java), 0)
        }
    }

    private fun setRecyclerViewScrollListener() {
        binding.rvHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val scrollY = binding.rvHome.computeVerticalScrollOffset()
                val layoutParamsHomeHeader = binding.homeHeader.layoutParams as ViewGroup.MarginLayoutParams

                if (isAnimating) {
                    return
                }

                if (scrollY == 0) {
                    isAnimating = false
                    layoutParamsHomeHeader.topMargin = DEFAULT_MARGIN
                    binding.homeHeader.layoutParams = layoutParamsHomeHeader
                    binding.homeTitle.visibility = View.VISIBLE
                } else if (scrollY > 50 && dy > 0 && binding.homeTitle.visibility == View.VISIBLE) {
                    isAnimating = true
                    startAnimation(layoutParamsHomeHeader, View.GONE)
                } else if (scrollY <= 50 && dy < 0 && binding.homeTitle.visibility == View.GONE) {
                    isAnimating = true
                    startAnimation(layoutParamsHomeHeader, View.VISIBLE)
                }
            }
        })
    }

    private fun startAnimation(
        layoutParamsHomeHeader: ViewGroup.MarginLayoutParams,
        titleVisibility: Int
    ) {
        val animator = ValueAnimator.ofInt(
            layoutParamsHomeHeader.topMargin,
            if (titleVisibility == View.GONE) NEW_MARGIN else DEFAULT_MARGIN
        ).apply {
            duration = 100
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                layoutParamsHomeHeader.topMargin = animatedValue
                binding.homeHeader.layoutParams = layoutParamsHomeHeader
                binding.homeTitle.visibility = titleVisibility
            }
            doOnEnd {
                isAnimating = false
            }
        }
        animator.start()
    }


    private fun onActionUnclosed(summary: Summary) {
        summary.unclosedAction?.let { unclosedAction ->
            if (unclosedAction.actionType == "solicitation") {
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
            if (unclosedAction.actionType == "contribution") {
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
        }
    }

    override fun onHomeChangeLocationUpdateClearFragment() {
        binding.frameLayoutChangeLocation.visibility = View.GONE
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
        var isContribProfile = false
        var signablePermission = false
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
    }
}

interface OnHomeChangeLocationUpdate {
    fun onHomeChangeLocationUpdateClearFragment()
}