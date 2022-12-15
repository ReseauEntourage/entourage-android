package social.entourage.android.new_v8.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentEventsBinding
import social.entourage.android.new_v8.RefreshController
import social.entourage.android.new_v8.ViewPagerDefaultPageController
import social.entourage.android.new_v8.events.create.CreateEventActivity
import social.entourage.android.new_v8.events.list.EventsViewPagerAdapter
import social.entourage.android.new_v8.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.new_v8.home.UnreadMessages
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents
import kotlin.math.abs

const val DISCOVER_EVENTS_TAB = 0
const val MY_EVENTS_TAB = 1

class EventsFragment : Fragment() {
    private var _binding: NewFragmentEventsBinding? = null
    val binding: NewFragmentEventsBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(Const.IS_OUTING_DISCOVER)) {
                ViewPagerDefaultPageController.shouldSelectDiscoverEvents = it.getBoolean(Const.IS_OUTING_DISCOVER)
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
        createEvent()
        initializeTab()
        handleImageViewAnimation()
        setPage()

        val presenter = EventsPresenter()
        presenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        presenter.getUnreadCount()
    }

    override fun onResume() {
        super.onResume()
        if (RefreshController.shouldRefreshEventFragment) {
            initializeTab()
            RefreshController.shouldRefreshEventFragment = false
        }
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = EventsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.discover_events),
            requireContext().getString(R.string.my_events)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    private fun setPage() {
        binding.viewPager.doOnPreDraw {
            binding.viewPager.setCurrentItem(
                if (ViewPagerDefaultPageController.shouldSelectDiscoverEvents) DISCOVER_EVENTS_TAB else MY_EVENTS_TAB,
                false
            )
            ViewPagerDefaultPageController.shouldSelectDiscoverEvents = true
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Event_view_discover)
                }
                else {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Event_view_my)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        val count:Int = unreadMessages?.unreadCount ?: 0
        EntourageApplication.get().getMainActivity()?.let {
            val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
            viewModel.badgeCount.postValue(UnreadMessages(count))
        }
    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.img.alpha = 1f - res
        })
    }

    private fun createEvent() {
        binding.createEvent.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_action_create)
            startActivityForResult(
                Intent(context, CreateEventActivity::class.java),
                0
            )
        }
    }
}