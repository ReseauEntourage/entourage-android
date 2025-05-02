package social.entourage.android.actions

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
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
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.main_filter.MainFilterMode
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents
import kotlin.math.abs

const val CONTRIBUTIONS_TAB = 0
const val DEMANDS_TAB = 1

const val LOCATION_FILTERS = "locationFilters"
const val CATEGORIES_FILTERS = "categoriesFilters"
const val FILTERS = "filters"
const val FILTERS2 = "filters2"

class ActionsFragment : Fragment() {

    private var _binding: NewFragmentActionsBinding? = null
    val binding: NewFragmentActionsBinding get() = _binding!!

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    private var isFromFilters = false
    private var isSearching = false
    private lateinit var presenter: ActionsPresenter

    private var currentLocationFilters = EventActionLocationFilters()
    private var currentCategoriesFilters = ActionSectionFilters()

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
        // Implémentation de mise à jour des filtres
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ViewModelProvider(requireActivity()).get(ActionsPresenter::class.java)
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
        _binding = NewFragmentActionsBinding.inflate(inflater, container, false)
        setSearchAndFilterButtons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ajout du TextWatcher ici pour garantir que _binding est initialisé
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                presenter.onSearchQueryChanged(s.toString())
                if (!s.toString().isEmpty()) {
                    hideFilter()
                } else {
                    showFilter()
                }
            }
        })
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSearching) {
                    isSearching = false
                    binding.searchEditText.clearFocus()
                    hideKeyboard(binding.searchEditText)  // Masquer le clavier
                    (requireActivity() as MainActivity).showBottomBar()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
        createAction()
        initializeViews()
        initializeTab()
        initializeFilters()
        setPage()
        handleSearchButton()
        presenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        presenter.getUnreadCount()
    }

    override fun onResume() {
        super.onResume()
        if (RefreshController.shouldRefreshEventFragment) {
            initializeTab()
            RefreshController.shouldRefreshEventFragment = false
        }
        if (MainFilterActivity.savedActionInterests.size > 0) {
            binding.cardFilterNumber.visibility = View.VISIBLE
            binding.tvNumberOfFilter.text = MainFilterActivity.savedActionInterests.size.toString()
            binding.uiLayoutFilter.background = resources.getDrawable(R.drawable.bg_unselected_filter)
            binding.uiLayoutSearch.background = resources.getDrawable(R.drawable.bg_unselected_filter)
        } else {
            binding.cardFilterNumber.visibility = View.GONE
            binding.uiLayoutFilter.background = resources.getDrawable(R.drawable.bg_unselected_filter)
            binding.uiLayoutSearch.background = resources.getDrawable(R.drawable.bg_unselected_filter)
        }
        binding.createAction.close()
        binding.overlayView.visibility = View.GONE
    }

    fun setSearchAndFilterButtons() {
        binding.uiLayoutSearch.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected_filter) // Ajoute un fond orange rond
        binding.uiBellSearch.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN)
        binding.uiLayoutFilter.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected_filter) // Remet le fond en blanc rond
        binding.uiBellFilter.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN)
        binding.uiLayoutFilter.visibility = View.VISIBLE
        binding.uiLayoutSearch.visibility = View.VISIBLE
    }

    fun handleSearchButton() {
        binding.uiLayoutSearch.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.actions_searchbar_clic)
            binding.collapsingToolbar.visibility = View.GONE
            binding.searchEditText.visibility = View.VISIBLE
            isSearching = true
        }
    }

    fun hideFilter() {
        binding.uiLayoutFilter.visibility = View.GONE
        binding.createAction.visibility = View.GONE
    }

    fun showFilter() {
        binding.uiLayoutFilter.visibility = View.VISIBLE
        binding.createAction.visibility = View.VISIBLE
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
        binding.searchEditText.visibility = View.GONE
        binding.collapsingToolbar.visibility = View.VISIBLE
    }

    private fun tintDrawable(drawable: Drawable?, color: Int) {
        drawable?.let {
            DrawableCompat.setTint(DrawableCompat.wrap(it), color)
        }
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = ActionsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.actions_tab_contribs),
            requireContext().getString(R.string.actions_tab_demands),
            requireContext().getString(R.string.actions_tab_mygroup),
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    presenter.isContrib = true
                    presenter.isMine = false
                    binding.uiLayoutSearch.visibility = View.VISIBLE
                    binding.uiLayoutFilter.visibility = View.VISIBLE
                    if (MainFilterActivity.savedActionInterests.isNotEmpty()) {
                        binding.cardFilterNumber.visibility = View.VISIBLE
                    }
                    AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_contrib)
                } else if (tab?.position == 1) {
                    presenter.isContrib = false
                    presenter.isMine = false
                    binding.uiLayoutSearch.visibility = View.VISIBLE
                    binding.uiLayoutFilter.visibility = View.VISIBLE
                    if (MainFilterActivity.savedActionInterests.isNotEmpty()) {
                        binding.cardFilterNumber.visibility = View.VISIBLE
                    }
                    AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_demand)
                } else {
                    binding.uiLayoutSearch.visibility = View.GONE
                    binding.uiLayoutFilter.visibility = View.GONE
                    binding.cardFilterNumber.visibility = View.GONE
                    AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_myactions)
                    presenter.isMine = true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun initializeViews() {
        binding.uiLayoutFilter.setOnClickListener {
            MainFilterActivity.mod = MainFilterMode.ACTION
            MainFilterActivity.hasToReloadAction = true
            val intent = Intent(activity, MainFilterActivity::class.java)
            startActivity(intent)
        }
        binding.searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val clearIcon = binding.searchEditText.compoundDrawables[2]
                val backIcon = binding.searchEditText.compoundDrawables[0]
                if (clearIcon != null && event.rawX >= (binding.searchEditText.right - clearIcon.bounds.width())) {
                    binding.searchEditText.text.clear()
                    return@setOnTouchListener true
                } else if (backIcon != null && event.rawX <= (binding.searchEditText.left + backIcon.bounds.width())) {
                    binding.searchEditText.text.clear()
                    binding.searchEditText.clearFocus()
                    isSearching = false

                    return@setOnTouchListener true
                }
            }
            false
        }
        val clearDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross_orange)
        val backDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_left_black)

        val color = ContextCompat.getColor(requireContext(), android.R.color.black)
        tintDrawable(clearDrawable, color)
        tintDrawable(backDrawable, color)
        binding.searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(backDrawable, null, clearDrawable, null)
                (requireActivity() as MainActivity).hideBottomBar()
            } else {
                hideKeyboard(v)  // Masquer le clavier lorsque le focus est perdu
                val query = binding.searchEditText.text.toString()
                if (query.isEmpty()) {
                    binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    (requireActivity() as MainActivity).showBottomBar()
                }
            }
        }
    }

    private fun initializeFilters() {
        binding.uiLayoutLocationBt.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_action_location)
            val intent = Intent(context, ActionLocationFilterActivity::class.java)
            intent.putExtra(LOCATION_FILTERS, currentLocationFilters)
            activityResultLauncher?.launch(intent)
        }

        binding.uiLayoutCategoryBt.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_action_filters)
            val intent = Intent(context, social.entourage.android.actions.ActionCategoriesFiltersActivity::class.java)
            intent.putExtra(CATEGORIES_FILTERS, currentCategoriesFilters)
            activityResultLauncher?.launch(intent)
        }

        binding.uiTitleLocationBt.text = currentLocationFilters.getFilterButtonString(requireContext())
        binding.uiTitleCategoryBt.text = getString(R.string.action_bt_cat_filters)

        if (currentCategoriesFilters.getNumberOfSectionsSelected() > 0) {
            binding.uiNbCategoryBt.text = "${currentCategoriesFilters.getNumberOfSectionsSelected()}"
            binding.uiNbCategoryBt.visibility = View.VISIBLE
        } else {
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
        val count: Int = unreadMessages?.unreadCount ?: 0
        EntourageApplication.get().mainActivity?.let {
            val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
            viewModel.badgeCount.postValue(UnreadMessages(count))
        }
    }

    private fun createAction() {
        val speedDialView: SpeedDialView = binding.createAction
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_create_demand, R.drawable.new_ic_create_demand)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabel(getString(R.string.action_menu_create_demand))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabelBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_create_contrib, R.drawable.new_ic_create_contrib)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabel(getString(R.string.action_menu_create_contrib))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setLabelBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                .create()
        )
        speedDialView.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_create_demand -> {
                    val intent = Intent(context, CreateActionActivity::class.java)
                    intent.putExtra(Const.IS_ACTION_DEMAND, true)
                    startActivityForResult(intent, 0)
                    true
                }
                R.id.fab_create_contrib -> {
                    val intent = Intent(context, CreateActionActivity::class.java)
                    intent.putExtra(Const.IS_ACTION_DEMAND, false)
                    startActivityForResult(intent, 0)
                    true
                }
                else -> false
            }
        }
        speedDialView.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                // Vous pouvez ici ajouter une action sur le bouton principal
                return false // Retourner false pour garder le comportement par défaut
            }

            override fun onToggleChanged(isOpen: Boolean) {
                // Gérer la visibilité de l'overlayView
                binding.overlayView.visibility = if (isOpen) View.VISIBLE else View.GONE
            }
        })
    }
}
