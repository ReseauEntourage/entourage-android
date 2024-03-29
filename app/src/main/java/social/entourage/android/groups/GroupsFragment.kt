package social.entourage.android.groups

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentGroupsBinding
import social.entourage.android.RefreshController
import social.entourage.android.ViewPagerDefaultPageController
import social.entourage.android.events.DISCOVER_EVENTS_TAB
import social.entourage.android.events.MY_EVENTS_TAB
import social.entourage.android.groups.create.CreateGroupActivity
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import kotlin.math.abs

const val MY_GROUPS_TAB = 0
const val DISCOVER_GROUPS_TAB = 1

class GroupsFragment : Fragment() {
    private var _binding: NewFragmentGroupsBinding? = null
    val binding: NewFragmentGroupsBinding get() = _binding!!
    private lateinit var presenter: GroupPresenter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_GROUP_SHOW)
        _binding = NewFragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = ViewModelProvider(requireActivity()).get(GroupPresenter::class.java)
        createGroup()
        initializeTab()
        handleImageViewAnimation()
        setPage()

        presenter.isPageHaveToChange.observe(requireActivity(),::handlePageChange)
        presenter.unreadMessages.observe(viewLifecycleOwner, ::updateUnreadCount)
        presenter.getUnreadCount()


    }


    private fun handlePageChange(isChanged:Boolean){
        ViewPagerDefaultPageController.shouldSelectDiscoverGroups = true
        setPage()
    }

    private fun goDiscover(){
        if(isAdded){
            binding.viewPager.setCurrentItem(
                DISCOVER_GROUPS_TAB ,
                false
            )
            binding.viewPager
        }
    }


    private fun setPage() {
        /*binding.viewPager.doOnPreDraw {
            binding.viewPager.setCurrentItem(
                if (ViewPagerDefaultPageController.shouldSelectDiscoverGroups) DISCOVER_GROUPS_TAB else MY_GROUPS_TAB,
                false
            )
            ViewPagerDefaultPageController.shouldSelectDiscoverGroups = false
        }*/
        binding.viewPager.doOnPreDraw {

            val goDiscoverGroup = activity?.intent?.getBooleanExtra("goDiscoverGroup", false) ?: false
            val insideVarOfActivity = (context as MainActivity).getFromDeepLGoDiscoverGroup()


            if(insideVarOfActivity){
                binding.viewPager.setCurrentItem(
                    DISCOVER_GROUPS_TAB

                )
                ViewPagerDefaultPageController.shouldSelectDiscoverGroups = true
                (context as MainActivity).setGoDiscoverGroupFromDeepL(false)
            }else{
                binding.viewPager.setCurrentItem(
                    if (ViewPagerDefaultPageController.shouldSelectDiscoverEvents) DISCOVER_GROUPS_TAB else MY_GROUPS_TAB,
                    false
                )
                ViewPagerDefaultPageController.shouldSelectDiscoverGroups = false
            }
        }
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = GroupsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.my_groups),
            requireContext().getString(R.string.discover_groups),
        )
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    AnalyticsEvents.logEvent(
                        if (position == MY_GROUPS_TAB)
                            AnalyticsEvents.ACTION_GROUP_MY_GROUP
                        else
                            AnalyticsEvents.ACTION_GROUP_DISCOVER
                    )
                }
            }
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        if (RefreshController.shouldRefreshFragment) {
            initializeTab()
            RefreshController.shouldRefreshFragment = false
        }
    }

    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        val count:Int = unreadMessages?.unreadCount ?: 0
        EntourageApplication.get().mainActivity?.let {
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

    private fun createGroup() {
        binding.createGroup.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_PLUS
            )
            startActivityForResult(Intent(context, CreateGroupActivity::class.java), 0)
        }
    }
}