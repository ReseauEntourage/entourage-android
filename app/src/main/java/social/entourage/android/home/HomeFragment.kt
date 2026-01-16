package social.entourage.android.home

import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
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
import social.entourage.android.api.model.Help
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Summary
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.databinding.FragmentHomeBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.events.create.CommunicationHandler
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

class HomeFragment : Fragment(), OnHomeHelpItemClickListener, OnHomeChangeLocationUpdate {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homePresenter: HomePresenter
    private var homeGroupAdapter = HomeGroupAdapter()
    private lateinit var homeEventAdapter: HomeEventAdapter
    private lateinit var homeSmallTalkAdapter: HomeSmallTalkAdapter
    private var homeActionAdapter = HomeActionAdapter(false)
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private lateinit var homeHelpAdapter: HomeHelpAdapter
    private var homePedagoAdapter: HomePedagoAdapter? = null
    private var homeInitialPedagoAdapter: HomeInitialPedagoAdapter? = null
    private lateinit var mainPresenter: MainPresenter
    private var pagegroup = 0
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        totalchecksum = 0
        hasRunEntryGating = false
        binding = FragmentHomeBinding.inflate(layoutInflater)
        disapearAllAtBeginning()
        mainPresenter = MainPresenter(requireActivity() as MainActivity)
        binding.progressBar.visibility = View.VISIBLE
        homePresenter = ViewModelProvider(requireActivity()).get(HomePresenter::class.java)
        actionsPresenter = ViewModelProvider(requireActivity()).get(ActionsPresenter::class.java)
        homeHelpAdapter = HomeHelpAdapter(this)
        homeEventAdapter = HomeEventAdapter(requireContext())
        homePedagoAdapter = HomePedagoAdapter(object : OnItemClick {
            override fun onItemClick(pedagogicalContent: Pedago) {
                if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
                    val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
                    intent.putExtra(Const.ID, pedagogicalContent.id)
                    PedagoDetailActivity.setPedagoId(pedagogicalContent.id)
                    requireActivity().startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        })
        homeInitialPedagoAdapter = HomeInitialPedagoAdapter(object : OnItemClick {
            override fun onItemClick(pedagogicalContent: Pedago) {
                if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
                    val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
                    intent.putExtra(Const.ID, pedagogicalContent.id)
                    PedagoDetailActivity.setPedagoId(pedagogicalContent.id)
                    requireActivity().startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        })

        homeSmallTalkAdapter = HomeSmallTalkAdapter(
            onStartClick = {
                startActivity(Intent(requireContext(), SmallTalkIntroActivity::class.java))
            },
            onConversationClick = { conversation ->
                val intent = Intent(requireContext(), DetailConversationActivity::class.java)
                DetailConversationActivity.isSmallTalkMode = true
                DetailConversationActivity.smallTalkId = conversation.smalltalkId.toString()
                startActivity(intent)
            },
            onMatchingClick = {
                Toast.makeText(requireContext(), getString(R.string.small_talk_subtitle_waiting), Toast.LENGTH_SHORT).show()
            },
            requireContext()
        )

        binding.rvHomeSmallTalk.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomeSmallTalk.adapter = homeSmallTalkAdapter

        AnalyticsEvents.logEvent(AnalyticsEvents.View__Home)
        if (EnhancedOnboarding.shouldNotDisplayCampain == true) {
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.home_activate_firebase_message)
        }

        setRecyclerViews()
        setSeeAllButtons()
        setObservations()
        setNotifButton()
        setMapButton()
        setProfileButton()
        setNestedScrollViewAnimation()
        checkNotificationStatus()
        increaseCounter()
        adjustChevronForRTL()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity)
        updateAvatar()
        userPresenter.user.observe(viewLifecycleOwner, userObserver)

        // On vérifie directement le cookie ici aussi pour éviter le lancement depuis MainActivity si besoin
        val hasCompletedEnhanced = EntourageApplication.get().sharedPreferences
            .getBoolean(PREF_ENHANCED_ONBOARDING_COMPLETED, false)

        if (MainActivity.shouldLaunchOnboarding && !hasCompletedEnhanced) {
            MainActivity.shouldLaunchOnboarding = false
            val intent = Intent(requireActivity(), EnhancedOnboarding::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (MainActivity.shouldLaunchOnboarding) {
            MainActivity.shouldLaunchOnboarding = false
        }
    }

    override fun onResume() {
        super.onResume()
        checksum = 0
        resetFilter()
        callToInitHome()
        actionsPresenter.getUnreadCount()
        if (MainActivity.shouldLaunchProfile) {
            MainActivity.shouldLaunchProfile = false
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Tab__Profil)
            startActivityForResult(Intent(context, MyProfileFullActivity::class.java), 0)
        }
        testNotifDemandePage()
        sendUserDiscussionStatus()
        loadSmallTalkItems()
    }

    private fun loadSmallTalkItems() {
        isRequestLoaded = false
        smallTalkViewModel.listUserRequests()
    }

    private fun composeSmallTalkItemsSimplified() {
        val items = mutableListOf<HomeSmallTalkItem>()
        val matchedRequests = currentRequests.filter { it.smalltalkId != null }
        val matchedItems = matchedRequests.map { userRequest ->
            HomeSmallTalkItem.ConversationItem(userRequest)
        }
        items.addAll(matchedItems)
        val hasUnmatchedRequest = currentRequests.any { it.smalltalkId == null }
        when {
            matchedItems.size >= 3 -> {
            }
            hasUnmatchedRequest -> {
                items.add(HomeSmallTalkItem.Waiting)
            }
            else -> {
                items.add(HomeSmallTalkItem.MatchPossible)
            }
        }
        homeSmallTalkAdapter.submitList(items)
    }

    private fun testNotifDemandePage() {
        binding.ivLogoHome.setOnLongClickListener {
            val intent = Intent(requireContext(), SmallTalkIntroActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true
        }
    }

    private fun testToken() {
        binding.ivLogoHome.setOnLongClickListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("FCM Token", token)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Token copié dans le presse-papiers", Toast.LENGTH_LONG).show()
            }
            true
        }
    }

    private fun adjustChevronForRTL() {
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        if (isRTL) {
            binding.chevron1.scaleX = -1f
            binding.chevron2.scaleX = -1f
            binding.chevron3.scaleX = -1f
            binding.chevron4.scaleX = -1f
        } else {
            binding.chevron1.scaleX = 1f
            binding.chevron2.scaleX = 1f
            binding.chevron3.scaleX = 1f
            binding.chevron4.scaleX = 1f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userPresenter.user.removeObserver(userObserver)
    }

    private fun runHomeEntryGatingIfNeeded() {
        // 1. Vérifications de base (Fragment attaché, déjà exécuté)
        if (!isAdded) return
        if (hasRunEntryGating) return

        val currentUser = user ?: return

        // 2. Vérification Session (RAM)
        if (HomeEntryGatingSession.didPresentCriticalThisSession ||
            HomeEntryGatingSession.didPresentNotifThisSession ||
            HomeEntryGatingSession.didPresentEnhancedThisSession
        ) {
            hasRunEntryGating = true
            return
        }

        // 3. Critical Onboarding (Goal / Zone)
        val missingGoal = isUserMissingRole(currentUser)
        val missingZone = isUserMissingZone(currentUser)

        Timber.wtf("wtf gating missingGoal=$missingGoal missingZone=$missingZone goal=${currentUser.goal} zone a1=${Gson().toJson(currentUser.address)} a2=${Gson().toJson(currentUser.addressSecondary)}")

        if (missingGoal || missingZone) {
            HomeEntryGatingSession.didPresentCriticalThisSession = true
            presentCriticalOnboarding(currentUser, missingGoal, missingZone)
            hasRunEntryGating = true
            return
        }

        // 4. Vérification Notifications
        val notifAllowed = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        updateTokenForNotificationState(notifAllowed)

        if (!notifAllowed) {
            val shared = EntourageApplication.get().sharedPreferences

            // --- COOKIE CHECK (Android) ---
            val alreadyShownHistory = shared.getBoolean(PREF_NOTIF_DEMAND_ALREADY_SHOWN, false)

            if (!alreadyShownHistory) {
                // Si on ne l'a JAMAIS montré
                val shouldShowNow = shared.getBoolean(PREF_NOTIF_SHOULD_SHOW_NEXT, false)

                if (shouldShowNow) {
                    shared.edit {
                        putBoolean(PREF_NOTIF_SHOULD_SHOW_NEXT, false)
                        putBoolean(PREF_NOTIF_DEMAND_ALREADY_SHOWN, true)
                    }
                    HomeEntryGatingSession.didPresentNotifThisSession = true
                    presentNotificationDemand()
                    hasRunEntryGating = true
                    return
                } else {
                    // Logique compteur (2e, 5e, 10e connexion)
                    val reachedThreshold = incrementAndCheckNotificationDemand()
                    if (reachedThreshold) {
                        shared.edit { putBoolean(PREF_NOTIF_SHOULD_SHOW_NEXT, true) }
                    }
                }
            }

        } else {
            resetNotificationDemandCounter()
            EntourageApplication.get().sharedPreferences.edit { putBoolean(PREF_NOTIF_SHOULD_SHOW_NEXT, false) }
        }

        // 5. Enhanced Onboarding (LOGIQUE SIMPLIFIÉE)
        val sp = EntourageApplication.get().sharedPreferences

        // On vérifie le cookie de complétion
        val hasCompletedEnhanced = sp.getBoolean(PREF_ENHANCED_ONBOARDING_COMPLETED, false)

        // Si ce n'est PAS complété (cookie false)
        if (!hasCompletedEnhanced) {
            // On vérifie qu'on ne l'a pas déjà lancé dans cette session (pour éviter les boucles)
            if (!HomeEntryGatingSession.didPresentEnhancedThisSession) {
                HomeEntryGatingSession.didPresentEnhancedThisSession = true
                presentEnhancedOnboardingIntro()
                hasRunEntryGating = true
                return
            }
        }

        if (MainActivity.shouldLaunchOnboarding) {
            MainActivity.shouldLaunchOnboarding = false
        }

        hasRunEntryGating = true
    }

    private fun presentCriticalOnboarding(user: User, missingGoal: Boolean, missingZone: Boolean) {
        if (missingGoal) {
            OnboardingStartActivity.FRAGMENT_NUMBER = 3
            startActivity(Intent(requireActivity(), OnboardingStartActivity::class.java))
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
        Timber.wtf("wtf zone a1=${Gson().toJson(a1)} a2=${Gson().toJson(a2)} ok1=$ok1 ok2=$ok2")
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
            mainPresenter.updateApplicationInfo("")
        }
    }

    private fun incrementAndCheckNotificationDemand(): Boolean {
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        var connectionCount = sharedPreferences.getInt(PREF_NOTIFICATION_CONNECTION_COUNT, 0)
        connectionCount++
        sharedPreferences.edit { putInt(PREF_NOTIFICATION_CONNECTION_COUNT, connectionCount) }
        return connectionCount == 2 || connectionCount == 5 || connectionCount == 10
    }

    private fun resetNotificationDemandCounter() {
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        sharedPreferences.edit { putInt(PREF_NOTIFICATION_CONNECTION_COUNT, 0) }
    }

    private fun checkNotificationStatus() {
        val areNotificationsEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
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
            mainPresenter.updateApplicationInfo("")
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
            mainPresenter.updateApplicationInfo(token)
        }
    }

    private fun deleteToken() {
        mainPresenter.deleteApplicationInfo { }
    }

    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        EntourageApplication.get().mainActivity?.let {
            val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
            viewModel.badgeCount.postValue(unreadMessages)
        }
        CommunicationHandler.resetValues()
    }

    private fun setMarginTop(view: View, marginTop: Int) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = marginTop
        view.layoutParams = layoutParams
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
                userPresenter.getUser(meId)
            }
        }
    }

    private fun checkSumEventAction() {
        checksum++
        if (checksum == 2) {
            if (isEventsEmpty && isActionEmpty) {
                binding.itemHz.layoutItemHz.visibility = View.VISIBLE
            } else {
                binding.itemHz.layoutItemHz.visibility = View.GONE
            }
        }
        binding.itemHz.buttonHzItem.setOnClickListener {
            val urlString = "https://reseauentourage.notion.site/Buffet-du-lien-social-69c20e089dbd483cb093e90ae2953a54"
            WebViewFragment.newInstance(urlString, 0, true)
                .show(requireActivity().supportFragmentManager, WebViewFragment.TAG)
        }
    }

    private fun doTotalchecksumToDisplayHomeFirstTime() {
        totalchecksum++
        if (totalchecksum == 4) {
            binding.homeNestedScrollView.visibility = View.VISIBLE
            binding.homeHeader.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun resetFilter() {
        currentFilters = EventActionLocationFilters()
        currentSectionsFilters = ActionSectionFilters()
    }

    private fun disapearAllAtBeginning() {
        binding.homeNestedScrollView.visibility = View.GONE
        binding.homeHeader.visibility = View.GONE
        binding.btnMoreGroup.visibility = View.GONE
        binding.rvHomeGroup.visibility = View.GONE
        binding.homeSubtitleGroup.visibility = View.GONE
        binding.homeTitleGroup.visibility = View.GONE
        binding.btnMoreEvent.visibility = View.GONE
        binding.rvHomeEvent.visibility = View.GONE
        binding.homeSubtitleEvent.visibility = View.GONE
        binding.homeTitleEvent.visibility = View.GONE
        binding.btnMoreAction.visibility = View.GONE
        binding.rvHomeAction.visibility = View.GONE
        binding.homeSubtitleAction.visibility = View.GONE
        binding.homeTitleAction.visibility = View.GONE
        binding.btnMorePedago.visibility = View.GONE
        binding.rvHomePedago.visibility = View.GONE
        binding.homeSubtitlePedago.visibility = View.GONE
        binding.homeTitlePedago.visibility = View.GONE
    }

    private fun setRecyclerViews() {
        val settingGrouplayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val offsetInPixels = resources.getDimensionPixelSize(R.dimen.horizontal_offset_home)

        binding.rvHomeGroup.adapter = homeGroupAdapter
        binding.rvHomeGroup.layoutManager = settingGrouplayoutManager
        binding.rvHomeGroup.setPadding(offsetInPixels, 0, 0, 0)
        binding.rvHomeGroup.clipToPadding = false

        val settingEventlayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomeEvent.adapter = homeEventAdapter
        binding.rvHomeEvent.layoutManager = settingEventlayoutManager
        binding.rvHomeEvent.setPadding(offsetInPixels, 0, 0, 0)
        binding.rvHomeEvent.clipToPadding = false

        val settingActionlayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHomeAction.adapter = homeActionAdapter
        binding.rvHomeAction.layoutManager = settingActionlayoutManager

        val settingPedagolayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHomePedago.adapter = homePedagoAdapter
        binding.rvHomePedago.layoutManager = settingPedagolayoutManager

        binding.rvHomeSensibilisation.adapter = homeInitialPedagoAdapter
        binding.rvHomeSensibilisation.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomeSensibilisation.setPadding(offsetInPixels, 0, 0, 0)

        val settingHelplayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHomeHelp.adapter = homeHelpAdapter
        binding.rvHomeHelp.layoutManager = settingHelplayoutManager
    }

    private fun setSeeAllButtons() {
        val mainActivity = (requireActivity() as? MainActivity)
        binding.btnMoreGroup.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Group_All)
            mainActivity?.setGoDiscoverGroupFromDeepL(true)
            mainActivity?.goGroup()
        }
        binding.btnMoreEvent.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Event_All)
            mainActivity?.goEvent()
        }
        binding.btnMoreAction.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Demand_All)
            mainActivity?.goDemand()
        }
        binding.btnMorePedago.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Pedago)
            val intent = Intent(requireActivity(), PedagoListActivity::class.java)
            requireContext().startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
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
        homePresenter.summary.observe(viewLifecycleOwner, ::updateContributionsView)
        homePresenter.getAllEvents.observe(viewLifecycleOwner, ::handleEvent)
        homePresenter.getAllActions.observe(viewLifecycleOwner, ::handleAction)
        homePresenter.pedagogicalContent.observe(viewLifecycleOwner, ::handlePedago)
        homePresenter.pedagogicalInitialContent.observe(viewLifecycleOwner, ::handleInitialPedago)
        homePresenter.notifsCount.observe(viewLifecycleOwner, ::updateNotifsCount)
        actionsPresenter.unreadMessages.observe(viewLifecycleOwner, ::updateUnreadCount)
    }

    fun handleGroup(allGroup: MutableList<Group>?) {
        if (allGroup == null) {
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()

        if (allGroup.size > 0) {
            binding.btnMoreGroup.visibility = View.GONE
            binding.rvHomeGroup.visibility = View.GONE
            binding.homeSubtitleGroup.visibility = View.GONE
            binding.homeTitleGroup.visibility = View.GONE
        } else {
            binding.btnMoreGroup.visibility = View.GONE
            binding.rvHomeGroup.visibility = View.GONE
            binding.homeSubtitleGroup.visibility = View.GONE
            binding.homeTitleGroup.visibility = View.GONE
        }
        this.homeGroupAdapter.resetData(allGroup)
    }

    fun handleEvent(allEvent: MutableList<Events>?) {
        if (allEvent == null) {
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()

        if (allEvent.size > 0) {
            val _offline_events: MutableList<Events> = mutableListOf()
            for (event in allEvent) {
                if (event.online == false) {
                    _offline_events.add(event)
                }
            }
            isEventsEmpty = _offline_events.size == 0

            binding.btnMoreEvent.visibility = View.VISIBLE
            binding.rvHomeEvent.visibility = View.VISIBLE
            binding.homeSubtitleEvent.visibility = View.GONE
            binding.homeTitleEvent.visibility = View.VISIBLE
        } else {
            isEventsEmpty = true
            binding.btnMoreEvent.visibility = View.GONE
            binding.rvHomeEvent.visibility = View.GONE
            binding.homeSubtitleEvent.visibility = View.GONE
            binding.homeTitleEvent.visibility = View.GONE
        }
        checkSumEventAction()
        this.homeEventAdapter.resetData(allEvent)
    }

    fun handleAction(allAction: MutableList<Action>?) {
        if (allAction == null) {
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()

        if (allAction.size > 0) {
            if (isContribution) {
                binding.homeTitleAction.text = getString(R.string.home_title_action_contrib)
                binding.homeSubtitleAction.text = getString(R.string.home_subtitle_action_contrib)
                binding.titleButtonAction.text = getString(R.string.home_btn_more_action_contrib)
                binding.btnMoreAction.setOnClickListener {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Contrib_All)
                    val mainActivity = (requireActivity() as? MainActivity)
                    mainActivity?.goContrib()
                }
            }
            isActionEmpty = false
            binding.btnMoreAction.visibility = View.VISIBLE
            binding.rvHomeAction.visibility = View.VISIBLE
            binding.homeSubtitleAction.visibility = View.GONE
            binding.homeTitleAction.visibility = View.VISIBLE
        } else {
            isActionEmpty = true
            binding.btnMoreAction.visibility = View.GONE
            binding.rvHomeAction.visibility = View.GONE
            binding.homeSubtitleAction.visibility = View.GONE
            binding.homeTitleAction.visibility = View.GONE
        }
        if (!isContribution) {
            checkSumEventAction()
        }
        this.homeActionAdapter.resetData(allAction)
    }

    fun handleInitialPedago(allPedago: MutableList<Pedago>?) {
        if (allPedago == null) {
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()
        if (allPedago.size > 0) {
            binding.rvHomeSensibilisation.visibility = View.VISIBLE
            binding.homeSubtitleSensibilisation.visibility = View.GONE
            binding.homeTitleSensibilisation.visibility = View.VISIBLE
            setMarginTop(binding.homeTitleAction, 16)
        } else {
            binding.rvHomeSensibilisation.visibility = View.GONE
            binding.homeSubtitleSensibilisation.visibility = View.GONE
            binding.homeTitleSensibilisation.visibility = View.GONE
            setMarginTop(binding.homeTitleAction, 0)
        }
        this.homeInitialPedagoAdapter?.resetData(allPedago)
    }

    fun handlePedago(allPedago: MutableList<Pedago>?) {
        if (allPedago == null) {
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()
        if (allPedago.size > 0) {
            binding.btnMorePedago.visibility = View.VISIBLE
            binding.rvHomePedago.visibility = View.VISIBLE
            binding.homeSubtitlePedago.visibility = View.GONE
            binding.homeTitlePedago.visibility = View.VISIBLE
        } else {
            binding.btnMorePedago.visibility = View.GONE
            binding.rvHomePedago.visibility = View.GONE
            binding.homeSubtitlePedago.visibility = View.GONE
            binding.homeTitlePedago.visibility = View.GONE
        }
        val pedagos: MutableList<Pedago> = mutableListOf()
        for (pedago in allPedago) {
            if (pedagos.size > 1) {
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
    }

    private fun updateContributionsView(summary: Summary) {
        if (!isAdded) return

        val isAssociationFromSummary = summary.association == true
        EntourageApplication.get().sharedPreferences.edit {
            putBoolean(PREF_IS_ASSOCIATION_FROM_SUMMARY, isAssociationFromSummary)
        }
        EnhancedOnboarding.isAssociationFromSummary = isAssociationFromSummary
        EnhancedOnboarding.preference = summary.preference ?: ""
        //presentEnhancedOnboardingIntro()
        onActionUnclosed(summary)
        handleHelps(summary)
        if (summary.signablePermission != null) {
            HomeFragment.signablePermission = summary.signablePermission!!
        }

        isContribution = summary.preference.equals("contribution")
        isContribProfile = isContribution

        if (isContribution) {
            if (!homeActionAdapter.getIsContrib()) {
                homeActionAdapter = HomeActionAdapter(isContribution)
            }
            binding.rvHomeAction.adapter = homeActionAdapter
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

    private fun handleHelps(summary: Summary) {
        if (isAdded) {
            doTotalchecksumToDisplayHomeFirstTime()
            val formattedString = requireContext().getString(R.string.home_help_title_three, summary.moderator?.displayName)
            val help3 = Help(formattedString, R.drawable.first_help_item_illu)
            val helps: MutableList<Help> = mutableListOf()
            helps.add(help3)
            homeHelpAdapter.resetData(helps, summary)
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

    private fun setMapButton() {
        binding.homeButtonMap.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Map)
            val intent = Intent(requireContext(), GDSMainActivity::class.java)
            startActivityForResult(intent, 0)
        }
    }

    private fun setProfileButton() {
        binding.avatar.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Tab__Profil)
            startActivityForResult(Intent(context, MyProfileFullActivity::class.java), 0)
        }
    }

    private fun setNestedScrollViewAnimation() {
        binding.homeNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val layoutParamsHomeHeader = binding.homeHeader.layoutParams as ViewGroup.MarginLayoutParams

            if (isAnimating) {
                return@OnScrollChangeListener
            }

            if (scrollY == 0) {
                isAnimating = false
                layoutParamsHomeHeader.topMargin = DEFAULT_MARGIN
                binding.homeHeader.layoutParams = layoutParamsHomeHeader
                binding.homeTitle.visibility = View.VISIBLE
            } else if (scrollY > 50 && oldScrollY <= 50) {
                isAnimating = true
                startAnimation(layoutParamsHomeHeader, View.GONE)
            } else if (scrollY <= 50 && oldScrollY > 50) {
                isAnimating = true
                startAnimation(layoutParamsHomeHeader, View.VISIBLE)
            }
        })
    }

    private fun startAnimation(layoutParamsHomeHeader: ViewGroup.MarginLayoutParams, titleVisibility: Int) {
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

    override fun onItemClick(position: Int, moderatorId: Int) {
        if (position == 2) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_CreateGroup)
            val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
            intent.putExtra(Const.ID, pedagoItemForCreateGroup?.id)
            PedagoDetailActivity.setPedagoId(pedagoItemForCreateGroup?.id!!)
            PedagoDetailActivity.setHtmlContent(pedagoItemForCreateGroup?.html!!)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        if (position == 1) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_CreateEvent)
            val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
            intent.putExtra(Const.ID, pedagoItemForCreateEvent?.id)
            PedagoDetailActivity.setPedagoId(pedagoItemForCreateEvent?.id!!)
            PedagoDetailActivity.setHtmlContent(pedagoItemForCreateEvent?.html!!)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        if (position == 0) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Moderator)
            startActivity(
                Intent(context, ProfileFullActivity::class.java).putExtra(
                    Const.USER_ID,
                    moderatorId
                )
            )
        }
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
                                    (activity as MainActivity).goContrib()
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
                                    (activity as MainActivity).goDemand()
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

    companion object {
        var isContribProfile = false
        var signablePermission = false

        private const val PREF_NOTIFICATION_CONNECTION_COUNT = "connectionCount"
        private const val PREF_ENHANCED_ONBOARDING_LAUNCHED = "PREF_ENHANCED_ONBOARDING_LAUNCHED"
        private const val PREF_ENHANCED_ONBOARDING_SKIPPED = "PREF_ENHANCED_ONBOARDING_SKIPPED"
        const val PREF_ENHANCED_ONBOARDING_COMPLETED = "PREF_ENHANCED_ONBOARDING_COMPLETED" // AJOUT
        private const val PREF_NOTIF_SHOULD_SHOW_NEXT = "PREF_NOTIF_SHOULD_SHOW_NEXT"
        private const val PREF_IS_ASSOCIATION_FROM_SUMMARY = "PREF_IS_ASSOCIATION_FROM_SUMMARY"
        private const val PREF_NOTIF_DEMAND_ALREADY_SHOWN = "PREF_NOTIF_DEMAND_ALREADY_SHOWN"
    }

    private object HomeEntryGatingSession {
        var didPresentCriticalThisSession: Boolean = false
        var didPresentNotifThisSession: Boolean = false
        var didPresentEnhancedThisSession: Boolean = false
    }
}

interface OnHomeChangeLocationUpdate {
    fun onHomeChangeLocationUpdateClearFragment()
}