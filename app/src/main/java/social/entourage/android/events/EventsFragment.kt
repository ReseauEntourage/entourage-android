package social.entourage.android.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_action_filter)
            eventsPresenter.changedFilterFromUpperFragment()

        }
        binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())
        binding.createEventRetracted.visibility = View.GONE
        binding.createEventExpanded.visibility = View.VISIBLE
    }

    fun handleFilterTitleAfterChange(filter:EventActionLocationFilters){
        //TODO CORRECT Context
        this.currentFilters = filter
        binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())

    }

    fun handleButtonBehavior(isExtended:Boolean){
        /*
        android:src="@drawable/new_fab_plus"
        android:text="@string/create_event_btn_title"
                */
        if(isExtended){
            binding.createEventRetracted.visibility = View.GONE
            binding.createEventExpanded.visibility = View.VISIBLE
        }else{
            binding.createEventRetracted.visibility = View.VISIBLE
            binding.createEventExpanded.visibility = View.GONE
        }
    }

    private fun handlePageChange(haveChange:Boolean){
        ViewPagerDefaultPageController.shouldSelectDiscoverEvents = true
        setPage()
    }

    private fun handleLaunchCreateEvent(haveToLaunchCreateEvent:Boolean){
        if(haveToLaunchCreateEvent){
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_action_create)
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
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_action_create)
            startActivityForResult(
                Intent(context, CreateEventActivity::class.java),
                0
            )
        }
        binding.createEventRetracted.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_action_create)
            startActivityForResult(
                Intent(context, CreateEventActivity::class.java),
                0
            )
        }
    }
}