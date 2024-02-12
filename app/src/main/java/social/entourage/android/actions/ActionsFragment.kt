package social.entourage.android.actions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentActionsBinding
import social.entourage.android.RefreshController
import social.entourage.android.ViewPagerDefaultPageController
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.actions.list.ActionsViewPagerAdapter
import social.entourage.android.actions.list.me.MyActionsListActivity
import social.entourage.android.api.model.ActionSectionFilters
import social.entourage.android.groups.details.feed.rotationDegree
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.homev2.HomeV2Fragment
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import kotlin.math.abs

const val CONTRIBUTIONS_TAB = 0
const val DEMANDS_TAB = 1

const val LOCATION_FILTERS = "locationFilters"
const val CATEGORIES_FILTERS = "categoriesFilters"
const val FILTERS = "filters"
const val FILTERS2 = "filters2"

class ActionsFragment : Fragment() {

    private var _binding:NewFragmentActionsBinding? = null
    val binding: NewFragmentActionsBinding get() = _binding!!

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    private var isFromFilters = false

    private var currentLocationFilters = EventActionLocationFilters()
    private var currentCategoriesFilters = ActionSectionFilters()

    private val speedDialMenuAdapter = object : SpeedDialMenuAdapter() {
        override fun getCount(): Int = 2

        override fun getMenuItem(context: Context, position: Int): SpeedDialMenuItem =
            when (position) {
                0 -> SpeedDialMenuItem(
                    context,
                    R.drawable.new_ic_create_demand,
                    getString(R.string.action_menu_create_demand)
                )
                1 -> SpeedDialMenuItem(
                    context,
                    R.drawable.new_ic_create_contrib,
                    getString(R.string.action_menu_create_contrib)
                )
                else -> SpeedDialMenuItem(
                    context,
                    R.drawable.new_ic_create_demand,
                    getString(R.string.action_menu_create_demand)
                )
            }

        override fun onMenuItemClick(position: Int): Boolean {
            when (position) {
                0 -> {
                    val intent = Intent(context, CreateActionActivity::class.java)
                    intent.putExtra(Const.IS_ACTION_DEMAND, true)
                    startActivityForResult(intent, 0)
                }
                1 -> {
                    val intent = Intent(context, CreateActionActivity::class.java)
                    intent.putExtra(Const.IS_ACTION_DEMAND, false)
                    startActivityForResult(intent, 0)
                }
            }
            return true
        }

        override fun onPrepareItemLabel(context: Context, position: Int, label: TextView) {
            TextViewCompat.setTextAppearance(label, R.style.left_courant_bold_black)
        }

        override fun onPrepareItemCard(context: Context, position: Int, card: View) {
            card.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.new_bg_circle_orange
            )
        }

        override fun fabRotationDegrees(): Float = rotationDegree
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                (result.data?.getSerializableExtra(LOCATION_FILTERS) as? EventActionLocationFilters)?.let {
                    this.currentLocationFilters = it
                    updateFilters()
                }

                (result.data?.getSerializableExtra(CATEGORIES_FILTERS) as? ActionSectionFilters)?.let {
                    this.currentCategoriesFilters = it
                    updateFilters()
                }
            })
    }

    private fun updateFilters() {
        isFromFilters = true
        binding.uiTitleLocationBt.text = currentLocationFilters.getFilterButtonString(requireContext())

        if (currentCategoriesFilters.getNumberOfSectionsSelected() > 0) {
            binding.uiNbCategoryBt.text = "${currentCategoriesFilters.getNumberOfSectionsSelected()}"
            binding.uiNbCategoryBt.visibility = View.VISIBLE
        }
        else {
            binding.uiNbCategoryBt.visibility = View.GONE
        }

        reload()
    }

    private fun reload() {
        val bundle = bundleOf()
        bundle.putSerializable(LOCATION_FILTERS,currentLocationFilters)
        bundle.putSerializable(CATEGORIES_FILTERS,currentCategoriesFilters)
        //Use to pass datas to child Fragment - Fragment Listener only work for 1 child fragment, need to pass multiples fragment results ;)
        childFragmentManager.setFragmentResult(FILTERS, bundle)
        childFragmentManager.setFragmentResult(FILTERS2, bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isDemand = false
        arguments?.let {
            isDemand = it.getBoolean(Const.IS_ACTION_DEMAND, false)
        }
        isDemand = !HomeV2Fragment.isContribProfile
        ViewPagerDefaultPageController.shouldSelectActionDemand = isDemand
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentActionsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAction()
        initializeViews()
        initializeTab()
        initializeFilters()
        handleImageViewAnimation()
        setPage()

        val presenter = ActionsPresenter()
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
        val adapter = ActionsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.actions_tab_contribs),
            requireContext().getString(R.string.actions_tab_demands)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_contrib)
                }
                else {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_demand)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun initializeViews() {
        binding.uiButtonMyActions.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_myactions)
            val intent = Intent(context, MyActionsListActivity::class.java)
            startActivityForResult(intent, 0)
        }
    }

    private fun initializeFilters() {
        binding.uiLayoutLocationBt.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_action_location)
            val intent = Intent(context, ActionLocationFilterActivity::class.java)
            intent.putExtra(LOCATION_FILTERS,currentLocationFilters)
            activityResultLauncher?.launch(intent)
        }

        binding.uiLayoutCategoryBt.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_action_filters)
            val intent = Intent(context, social.entourage.android.actions.ActionCategoriesFiltersActivity::class.java)
            intent.putExtra(CATEGORIES_FILTERS,currentCategoriesFilters)
            activityResultLauncher?.launch(intent)
        }

        binding.uiTitleLocationBt.text = currentLocationFilters.getFilterButtonString(requireContext())
        binding.uiTitleCategoryBt.text = getString(R.string.action_bt_cat_filters)

        if (currentCategoriesFilters.getNumberOfSectionsSelected() > 0) {
            binding.uiNbCategoryBt.text = "${currentCategoriesFilters.getNumberOfSectionsSelected()}"
            binding.uiNbCategoryBt.visibility = View.VISIBLE
        }
        else {
            binding.uiNbCategoryBt.visibility = View.GONE
        }
    }

    private fun setPage() {
        binding.viewPager.doOnPreDraw {
            binding.viewPager.setCurrentItem(
                if (ViewPagerDefaultPageController.shouldSelectActionDemand) DEMANDS_TAB else CONTRIBUTIONS_TAB,
                true
            )
            ViewPagerDefaultPageController.shouldSelectActionDemand = false
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

    private fun createAction() {
        binding.createAction.setContentCoverColour(0xeeffffff.toInt())
        binding.createAction.speedDialMenuAdapter = speedDialMenuAdapter
        binding.createAction.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_action_create)
        }
    }
}