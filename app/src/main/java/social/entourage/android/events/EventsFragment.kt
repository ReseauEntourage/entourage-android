package social.entourage.android.events

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentEventsBinding
import social.entourage.android.RefreshController
import social.entourage.android.ViewPagerDefaultPageController
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.events.list.DiscoverEventsListFragment
import social.entourage.android.events.list.EventsViewPagerAdapter
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import kotlin.math.abs

const val MY_EVENTS_TAB = 0
const val DISCOVER_EVENTS_TAB = 1

class EventsFragment : Fragment() {
    private var _binding: NewFragmentEventsBinding? = null
    private var currentFilters = EventActionLocationFilters()
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var isFromFilters = false

    //TODO title same size as
    val binding: NewFragmentEventsBinding get() = _binding!!
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
        _binding = NewFragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventsPresenter = ViewModelProvider(requireActivity()).get(EventsPresenter::class.java)
        createEvent()
        initializeTab()
        setPage()
        eventsPresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        eventsPresenter.haveToChangePage.observe(requireActivity(),::handlePageChange)
        eventsPresenter.haveToCreateEvent.observe(requireActivity(),::handleLaunchCreateEvent)
        eventsPresenter.isCreateButtonExtended.observe(requireActivity(),::handleButtonBehavior)
        eventsPresenter.hasChangedFilterLocationForParentFragment.observe(requireActivity(),::handleFilterTitleAfterChange)
        eventsPresenter.getUnreadCount()
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

    }
    private fun updateFilters() {
        isFromFilters = true
        binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())

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
            binding.viewPager.setCurrentItem(
               DISCOVER_EVENTS_TAB
            )
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