package social.entourage.android.events

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.ViewPagerDefaultPageController
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.databinding.FragmentEventsBinding
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.events.list.DiscoverEventsListFragment
import social.entourage.android.events.list.EventsViewPagerAdapter
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.main_filter.MainFilterMode
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.HighlightOverlayView

const val DISCOVER_EVENTS_TAB = 1

class EventsFragment : Fragment() {
    private var _binding: FragmentEventsBinding? = null
    private var currentFilters = EventActionLocationFilters()
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var isFromFilters = false
    private var statusBarHeight = 0


    //TODO title same size as
    val binding: FragmentEventsBinding get() = _binding!!
    private lateinit var eventsPresenter: EventsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(Const.IS_OUTING_DISCOVER)) {
                ViewPagerDefaultPageController.shouldSelectDiscoverEvents = it.getBoolean(Const.IS_OUTING_DISCOVER)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        { result ->
            val filters = result.data?.getSerializableExtra(EventFiltersActivity.FILTERS) as? EventActionLocationFilters
            filters?.let {
                this.currentFilters = filters
                updateFilters()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        // Listen for WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(binding.topEventLayout) { view, windowInsets ->
            // Get the insets for the statusBars() type:
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            statusBarHeight = insets.top
            view.updatePadding(
                top = insets.top
            )
            // Return the original insets so they aren’t consumed
            windowInsets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchAndFilterButtons()
        eventsPresenter = ViewModelProvider(requireActivity()).get(EventsPresenter::class.java)
        showBubbleCase()
        createEvent()
        initializeTab()
        setPage()
        eventsPresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        eventsPresenter.haveToChangePage.observe(requireActivity(),::handlePageChange)
        eventsPresenter.haveToCreateEvent.observe(requireActivity(),::handleLaunchCreateEvent)
        eventsPresenter.isCreateButtonExtended.observe(requireActivity(),::handleButtonBehavior)
        eventsPresenter.hasToHideButton.observe(requireActivity(),::handleShowHideButton)
        eventsPresenter.shouldChangeTopView.observe(requireActivity(),::handleTopTitle)
        eventsPresenter.textSizeChange.observe(requireActivity(),::handleTextSize)
        eventsPresenter.hasChangedFilterLocationForParentFragment.observe(requireActivity(),::handleFilterTitleAfterChange)
        eventsPresenter.getUnreadCount()
        handleFilterButton()
        handleSearchButton()
    }

    override fun onResume() {
        super.onResume()
        if (isFromDetails == true){
            isFromDetails = false
        }else{
            this.currentFilters  = EventActionLocationFilters()
            updateFilters()
        }
        if (RefreshController.shouldRefreshEventFragment) {
            initializeTab()
            RefreshController.shouldRefreshEventFragment = false
        }
        initView()
        if (MainFilterActivity.savedGroupInterests.size > 0) {
            binding.cardFilterNumber.visibility = View.VISIBLE
            binding.tvNumberOfFilter.text = MainFilterActivity.savedGroupInterests.size.toString()

        } else {
            binding.cardFilterNumber.visibility = View.GONE

        }
        resetSearchButtonState()
    }

    fun showCustomBubble(targetView: View) {
        val activity = requireActivity()
        val rootLayout = activity.findViewById<ViewGroup>(android.R.id.content)

        // Créer l'overlay
        val overlayView = HighlightOverlayView(activity, targetView)

        // Inflater votre 'bubble_layout.xml'
        val inflater = LayoutInflater.from(activity)
        val bubbleView = inflater.inflate(R.layout.layout_bubble_info_dialog, overlayView, false)

        // **Récupérer le TextView du bouton et appliquer le soulignement**
        val bubbleButton = bubbleView.findViewById<TextView>(R.id.bubbleButton)
        bubbleButton.paintFlags = bubbleButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Mesurer le bubbleView
        bubbleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        // Obtenir la position de 'targetView'
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)

        // Positionner le bubbleView
        bubbleView.x = 0f
        bubbleView.y = 150f

        // Ajouter le bubbleView à l'overlay
        overlayView.addView(bubbleView)

        // Ajouter l'overlay à la racine
        rootLayout.addView(overlayView)

        // Gérer le clic sur l'overlay pour le retirer
        overlayView.setOnClickListener {
            rootLayout.removeView(overlayView)
        }

        // Gérer le clic sur le bubbleView si nécessaire
        bubbleView.setOnClickListener {
            // Action lorsque le bubbleView est cliqué
            rootLayout.removeView(overlayView)
        }
    }

    private fun showBubbleCase(){
       if(MainActivity.shouldLaunchEvent == true){
            MainActivity.shouldLaunchEvent = false
            MainFilterActivity.savedGroupInterests = MainFilterActivity.savedGroupInterestsFromOnboarding
           if(MainFilterActivity.savedGroupInterestsFromOnboarding.size > 0){
               MainFilterActivity.hasFilter = true
           }
           showCustomBubble(binding.uiLayoutFilter)
       }
    }
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
    fun handleTopTitle(hideTitle: Boolean) {
        if (!isAdded) {
            // Le fragment n'est pas attaché, on ne tente pas de mettre à jour l'UI
            return
        }
        val constraintSet = androidx.constraintlayout.widget.ConstraintSet()
        constraintSet.clone(binding.rootLayout)

        // Convertit 20dp en pixels
        val marginPx = 35.dpToPx(requireContext())

        if (hideTitle) {
            // 1. Écrase la hauteur de top_event_layout
            constraintSet.constrainHeight(R.id.top_event_layout, 0)

            // 2. Connecte le coordinator au parent (root_layout) avec une marge top de 20dp
            constraintSet.clear(R.id.coordinator, androidx.constraintlayout.widget.ConstraintSet.TOP)
            constraintSet.connect(
                R.id.coordinator,
                androidx.constraintlayout.widget.ConstraintSet.TOP,
                R.id.root_layout,
                androidx.constraintlayout.widget.ConstraintSet.TOP,
                marginPx // <-- LA MARGE
            )

        } else {
            // 1. Remet la hauteur en wrap_content
            constraintSet.constrainHeight(
                R.id.top_event_layout,
                androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
            )

            // 2. Connecte le coordinator SOUS top_event_layout, sans marge
            constraintSet.clear(R.id.coordinator, androidx.constraintlayout.widget.ConstraintSet.TOP)
            constraintSet.connect(
                R.id.coordinator,
                androidx.constraintlayout.widget.ConstraintSet.TOP,
                R.id.top_event_layout,
                androidx.constraintlayout.widget.ConstraintSet.BOTTOM
            )
        }

        // Lance l’animation de transition
        androidx.transition.TransitionManager.beginDelayedTransition(binding.rootLayout)
        constraintSet.applyTo(binding.rootLayout)
    }



    fun setSearchAndFilterButtons(){
        binding.uiLayoutSearch.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected_filter) // Ajoute un fond orange rond
        binding.uiBellSearch.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN)
        binding.uiLayoutFilter.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected_filter) // Remet le fond en blanc rond
        binding.uiBellFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN)
        binding.uiLayoutFilter.visibility = View.VISIBLE
        binding.uiLayoutSearch.visibility = View.VISIBLE
    }

    private fun handleFilterButton() {
       binding.uiLayoutFilter.setOnClickListener {
           DiscoverEventsListFragment.isFirstResumeWithFilters = true
           MainFilterActivity.mod = MainFilterMode.EVENT
           val intent = Intent(activity, MainFilterActivity::class.java)
           startActivity(intent)
           requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

       }
    }

    private fun handleSearchButton(){
        binding.uiLayoutSearch.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.events_searchbar_clic)
            this.eventsPresenter.changeSearchMode()
        }
    }



    private fun resetSearchButtonState() {
        binding.uiLayoutFilter.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected_filter) // Remet le fond en blanc rond
        binding.uiBellFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN) // Applique une tint noire par défaut
        binding.uiLayoutSearch.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected_filter) // Remet le fond en blanc rond
        binding.uiBellSearch.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN) // Applique une tint noire par défaut
    }


    private fun updateFilters() {
        isFromFilters = true
        binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())

    }
    private fun handleShowHideButton(hideButton:Boolean){
        if(hideButton){
            binding.createEventExpanded.visibility = View.GONE
            binding.createEventRetracted.visibility = View.GONE
        }else{
            binding.createEventExpanded.visibility = View.VISIBLE }
    }
    fun initView(){

        binding.uiLayoutLocationBt.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Event__LocationFilter)
            eventsPresenter.changedFilterFromUpperFragment()

        }
        binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())
        binding.createEventRetracted.visibility = View.GONE
        binding.createEventExpanded.visibility = View.VISIBLE
    }

    fun handleFilterTitleAfterChange(filter:EventActionLocationFilters){
        if(isAdded){
            this.currentFilters = filter
            binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())
        }
    }

    fun handleButtonBehavior(isExtended:Boolean){
        if (isExtended) {
            animateToExtendedState()
        } else {
            animateToRetractedState()
        }
    }

    fun handleTextSize(size:Float){
        binding.uiTitleEvents.textSize = size
    }

    private fun animateToExtendedState() {
        if (binding.createEventExpanded.visibility == View.VISIBLE) {
            // Le bouton est déjà dans l'état étendu
            return
        }

        binding.createEventExpanded.alpha = 0f
        binding.createEventExpanded.visibility = View.VISIBLE
        binding.createEventExpanded.animate().scaleX(1f).alpha(1f).setDuration(200).withEndAction {
            binding.createEventRetracted.visibility = View.GONE
        }.start()
        binding.createEventRetracted.animate().scaleX(0f).alpha(0f).setDuration(200).start()
    }

    private fun animateToRetractedState() {
        if (binding.createEventRetracted.visibility == View.VISIBLE) {
            // Le bouton est déjà dans l'état rétracté
            return
        }

        binding.createEventRetracted.alpha = 0f
        binding.createEventRetracted.visibility = View.VISIBLE
        binding.createEventRetracted.animate().scaleX(1f).alpha(1f).setDuration(200).withEndAction {
            binding.createEventExpanded.visibility = View.GONE
        }.start()
        binding.createEventExpanded.animate().scaleX(0f).alpha(0f).setDuration(200).start()
    }



    private fun handlePageChange(haveChange:Boolean){
        ViewPagerDefaultPageController.shouldSelectDiscoverEvents = true
        setPage()
    }

    private fun handleLaunchCreateEvent(haveToLaunchCreateEvent:Boolean){
        if(haveToLaunchCreateEvent){
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Event__LocationFilter)
            startActivityForResult(
                Intent(context, CreateEventActivity::class.java),
                0
            )
        }
    }
    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = EventsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
    }

    private fun setPage() {
        binding.viewPager.doOnPreDraw {
            binding.viewPager.currentItem = DISCOVER_EVENTS_TAB
            ViewPagerDefaultPageController.shouldSelectDiscoverEvents = true
        }

    }

    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        val count:Int = unreadMessages?.unreadCount ?: 0
        EntourageApplication.get().mainActivity?.let {
            val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
            viewModel.badgeCount.postValue(UnreadMessages(count))
        }
    }


    private fun createEvent() {
        binding.createEventExpanded.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Event__New)
            startActivityForResult(
                Intent(context, CreateEventActivity::class.java),
                0
            )
        }
        binding.createEventRetracted.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Event__New)
            startActivityForResult(
                Intent(context, CreateEventActivity::class.java),
                0
            )
        }
    }
    companion object {
        var isFromDetails = false
    }
}