package social.entourage.android.groups.list

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.GroupV2FragmentLayoutBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.create.CreateGroupActivity
import social.entourage.android.homev2.HomeGroupAdapter
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.main_filter.MainFilterMode
import social.entourage.android.tools.log.AnalyticsEvents

class GroupeV2Fragment : Fragment(), UpdateGroupInter {

    private lateinit var binding: GroupV2FragmentLayoutBinding
    private lateinit var presenter: GroupPresenter
    private var groupsList: MutableList<Group> = ArrayList()
    private var myGroupsList: MutableList<Group> = ArrayList()
    private var searchResultsList: MutableList<Group> = ArrayList()
    private var myId: Int? = null
    private var page: Int = 0
    private var pageMy: Int = 0
    private lateinit var adapterGroup: GroupsListAdapter
    private lateinit var adapterGroupSearch: GroupsListAdapter
    private lateinit var adapterMyGroup: HomeGroupAdapter
    private var checkSum = 0
    private var isLoading = false
    private var PER_PAGE = 20
    private var addedGroupIds: MutableSet<Int> = mutableSetOf()
    private var addedMyGroupIds: MutableSet<Int> = mutableSetOf()
    private var isFirstResumeWithFilters = true
    private var lastFiltersHash: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = GroupV2FragmentLayoutBinding.bind(view)
        myId = EntourageApplication.me(activity)?.id
        presenter = ViewModelProvider(requireActivity()).get(GroupPresenter::class.java)

        // Ajout de la gestion du bouton de retour
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchEditText.hasFocus()) {
                    binding.searchEditText.clearFocus()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Observer pour les groupes filtrés
        presenter.getAllGroups.observe(viewLifecycleOwner, { allGroups ->
            handleResponseGetGroups(allGroups)
        })

        presenter.getAllMyGroups.observe(viewLifecycleOwner, { allGroups ->
            handleResponseMyGetGroups(allGroups)
        })

        // Observer pour les résultats de recherche
        presenter.groupSearch.observe(viewLifecycleOwner, { searchResults ->
            updateSearchResults(searchResults)
        })

        adapterGroup = GroupsListAdapter(groupsList, myId, FromScreen.DISCOVER, this)
        adapterGroupSearch = GroupsListAdapter(searchResultsList, myId, FromScreen.DISCOVER, this)
        adapterMyGroup = HomeGroupAdapter()
        binding.recyclerViewVertical.adapter = adapterGroup
        binding.rvSearch.adapter = adapterGroupSearch
        binding.recyclerViewHorizontal.adapter = adapterMyGroup
        binding.recyclerViewVertical.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvSearch.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.layoutFilter.setOnClickListener {
            isFirstResumeWithFilters = true
            MainFilterActivity.mod = MainFilterMode.GROUP
            val intent = Intent(activity, MainFilterActivity::class.java)
            startActivity(intent)
        }
        setupSearchView() // Call the method to setup the search view

        binding.recyclerViewVertical.isVerticalScrollBarEnabled = false
        binding.recyclerViewHorizontal.apply {
            val settingLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = settingLayoutManager
            val offsetInPixels = resources.getDimensionPixelSize(R.dimen.horizontal_offset)
            setPadding(offsetInPixels, 0, 0, 0)
            clipToPadding = false
            isHorizontalScrollBarEnabled = false
        }
        handleCreateGroupButton()
        setupScrollViewListener()
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_GROUP_SHOW)
        initView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GroupV2FragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_SHOW_DISCOVER
        )
        binding.progressBar.visibility = View.VISIBLE

        val currentFiltersHash = getCurrentFiltersHash()
        if (currentFiltersHash != lastFiltersHash) {
            isFirstResumeWithFilters = true
            lastFiltersHash = currentFiltersHash
        }
        if (isFirstResumeWithFilters) {
            Log.wtf("wtf", "isFirstResumeWithFilters" + isFirstResumeWithFilters)
            isFirstResumeWithFilters = false
            groupsList.clear()
            myGroupsList.clear()
            addedGroupIds.clear()
            addedMyGroupIds.clear()
            adapterGroup.notifyDataSetChanged()
            adapterMyGroup.notifyDataSetChanged()
            page = 0
            pageMy = 0
            presenter.isLastPage = false
            applyFilters()
        } else {
            loadMoreGroups()
        }
        if (MainFilterActivity.savedGroupInterests.size > 0) {
            binding.cardFilterNumber.visibility = View.VISIBLE
            binding.tvNumberOfFilter.text = MainFilterActivity.savedGroupInterests.size.toString()
            binding.layoutFilter.background = resources.getDrawable(R.drawable.bg_selected_filter_main)

        } else {
            binding.cardFilterNumber.visibility = View.GONE
            binding.layoutFilter.background = resources.getDrawable(R.drawable.bg_unselected_filter_main)

        }
    }

    private fun getCurrentFiltersHash(): Int {
        return (MainFilterActivity.savedGroupInterests.joinToString(",") +
                MainFilterActivity.savedRadius +
                MainFilterActivity.savedLocation?.name).hashCode()
    }

    private fun tintDrawable(drawable: Drawable?, color: Int) {
        drawable?.let {
            DrawableCompat.setTint(DrawableCompat.wrap(it), color)
        }
    }

    private fun setupSearchView() {
        val clearDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross_orange)
        val backDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_left_black)

        val color = ContextCompat.getColor(requireContext(), android.R.color.black)
        tintDrawable(clearDrawable, color)
        tintDrawable(backDrawable, color)

        binding.searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideMainViews()
                binding.rvSearch.visibility = View.VISIBLE
                binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(backDrawable, null, clearDrawable, null)
            } else {
                val query = binding.searchEditText.text.toString()
                if (query.isEmpty()) {
                    showMainViews()
                    binding.rvSearch.visibility = View.GONE
                    binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
            }
        }

        binding.searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val clearIcon = binding.searchEditText.compoundDrawables[2]
                val backIcon = binding.searchEditText.compoundDrawables[0]
                if (clearIcon != null && event.rawX >= (binding.searchEditText.right - clearIcon.bounds.width())) {
                    binding.searchEditText.text.clear()
                    return@setOnTouchListener true
                } else if (backIcon != null && event.rawX <= (binding.searchEditText.left + backIcon.bounds.width())) {
                    binding.searchEditText.clearFocus()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Ajouter un TextWatcher pour surveiller les changements de texte
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()

            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isEmpty()) {

                } else {
                    performSearch(query)
                }
            }
        })
    }

    private fun performSearch(query: String) {
        presenter.page_search = 0
        presenter.isLastPageSearch = false
        presenter.groupSearch.value = mutableListOf()
        searchGroups(query)
    }

    private fun searchGroups(query: String) {
        val perPage = 20
        presenter.getAllGroupsWithSearchQuery(query, presenter.page_search, perPage)
        myId?.let {
            presenter.getMyGroupsWithSearchQuery(it, query, presenter.page_search, perPage)
        }
    }

    private fun hideMainViews() {
        val layoutParams = binding.searchEditText.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginEnd = 70
        binding.searchEditText.layoutParams = layoutParams
        binding.recyclerViewHorizontal.visibility = View.GONE
        binding.recyclerViewVertical.visibility = View.GONE
        binding.textViewTitleGroupes.visibility = View.GONE
        binding.titleAllGroups.visibility = View.GONE
        binding.separatorAllGroups.visibility = View.GONE
        binding.titleMyGroups.visibility = View.GONE
        binding.separatorMyGroups.visibility = View.GONE
        binding.createGroupExpanded.visibility = View.GONE
        binding.createGroup.visibility = View.GONE
        binding.layoutFilter.visibility = View.GONE
    }

    private fun showMainViews() {
        val layoutParams = binding.searchEditText.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginEnd = 8
        binding.searchEditText.layoutParams = layoutParams
        binding.recyclerViewHorizontal.visibility = View.VISIBLE
        binding.recyclerViewVertical.visibility = View.VISIBLE
        binding.textViewTitleGroupes.visibility = View.VISIBLE
        binding.titleAllGroups.visibility = View.VISIBLE
        binding.separatorAllGroups.visibility = View.VISIBLE
        binding.titleMyGroups.visibility = View.VISIBLE
        binding.separatorMyGroups.visibility = View.VISIBLE
        binding.layoutFilter.visibility = View.VISIBLE
        binding.createGroupExpanded.visibility = View.VISIBLE
    }

    private fun initView() {
        binding.progressBar.visibility = View.VISIBLE
        binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        groupsList.clear()
        myGroupsList.clear()
        presenter.isLastPage = false
        isLoading = false
        presenter.getAllGroups(page, PER_PAGE)
        if (myId != null) {
            presenter.getMyGroups(pageMy, 100, myId!!)
        }
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let {
            val newGroups = it.filter { group -> !addedGroupIds.contains(group.id) }
            groupsList.addAll(newGroups)
            adapterGroup.updateGroupsList(groupsList)
            addedGroupIds.addAll(newGroups.map { group -> group.id!! })
        }
        checkingSumForEmptyView()
        isLoading = false
        binding.progressBar.visibility = View.GONE
    }

    private fun handleResponseMyGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let {
            val newGroups = it.filter { group -> !addedMyGroupIds.contains(group.id) }
            myGroupsList.addAll(newGroups)
            adapterMyGroup.resetData(myGroupsList)
            addedMyGroupIds.addAll(newGroups.map { group -> group.id!! })
            if(myGroupsList.size == 0) {
                binding.titleMyGroups.visibility = View.GONE
                binding.separatorMyGroups.visibility = View.GONE
            }else {
                binding.titleMyGroups.visibility = View.VISIBLE
                binding.separatorMyGroups.visibility = View.VISIBLE
            }
        }
        checkingSumForEmptyView()
        isLoading = false
        binding.progressBar.visibility = View.GONE
    }

    private fun updateSearchResults(searchResults: MutableList<Group>?) {
        searchResults?.let {
            searchResultsList.clear()
            searchResultsList.addAll(it)
            adapterGroupSearch.updateGroupsList(searchResultsList)
            binding.progressBar.visibility = View.GONE

        }
    }

    private fun checkingSumForEmptyView() {
        checkSum++
        if (checkSum > 1) {
            binding.emptyStateLayout.visibility = View.GONE
        }
        if (groupsList.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun handleCreateGroupButton() {
        binding.createGroup.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_PLUS
            )
            startActivityForResult(Intent(context, CreateGroupActivity::class.java), 0)
        }
        binding.createGroupExpanded.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_PLUS
            )
            startActivityForResult(Intent(context, CreateGroupActivity::class.java), 0)
        }
    }

    private fun setupScrollViewListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                val newFontSize = calculateFontSize(scrollY)
                binding.textViewTitleGroupes.textSize = newFontSize
                handleButtonBehavior(scrollY <= oldScrollY)
                if (!binding.nestedScrollView.canScrollVertically(1) && !presenter.isLastPage && !isLoading) {
                    isLoading = true
                    page++
                    loadMoreGroups()
                }
            }

            binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1) && !presenter.isLastPageSearch && !isLoading) {
                        isLoading = true
                        presenter.page_search++
                        val query = binding.searchEditText.text.toString()
                        searchGroups(query)
                    }
                }
            })
        }
    }

    private fun loadMoreGroups() {
        binding.progressBar.visibility = View.VISIBLE
        if (MainFilterActivity.savedGroupInterests.isNotEmpty()) {
            applyFilters()
        } else {
            presenter.getAllGroups(page, PER_PAGE)
        }
        isLoading = false
    }

    private fun calculateFontSize(scrollY: Int): Float {
        val minSize = 15f
        val maxSize = 24f
        val scrollThreshold = 100

        return if (scrollY > scrollThreshold) minSize else maxSize
    }

    private fun handleButtonBehavior(isExtended: Boolean) {
        if (isExtended) {
            animateToExtendedState()
        } else {
            animateToRetractedState()
        }
    }

    private fun animateToExtendedState() {
        if (binding.createGroupExpanded.visibility == View.VISIBLE) {
            return
        }

        binding.createGroupExpanded.alpha = 0f
        binding.createGroupExpanded.visibility = View.VISIBLE
        binding.createGroupExpanded.animate().scaleX(1f).alpha(1f).setDuration(200).withEndAction {
            binding.createGroup.visibility = View.GONE
        }.start()
        binding.createGroup.animate().scaleX(0f).alpha(0f).setDuration(200).start()
    }

    private fun animateToRetractedState() {
        if (binding.createGroup.visibility == View.VISIBLE) {
            return
        }

        binding.createGroup.alpha = 0f
        binding.createGroup.visibility = View.VISIBLE
        binding.createGroup.animate().scaleX(1f).alpha(1f).setDuration(200).withEndAction {
            binding.createGroupExpanded.visibility = View.GONE
        }.start()
        binding.createGroupExpanded.animate().scaleX(0f).alpha(0f).setDuration(200).start()
    }

    private fun applyFilters() {
        val interests = MainFilterActivity.savedGroupInterests.joinToString(",")
        val radius = MainFilterActivity.savedRadius
        val location = MainFilterActivity.savedLocation
        val latitude = location?.lat
        val longitude = location?.lng

        presenter.getAllGroupsWithFilter(page, PER_PAGE, interests, radius, latitude, longitude)
        myId?.let {
            presenter.getMyGroupsWithFilter(it, pageMy, 100, interests, radius, latitude, longitude)
        }
    }

    override fun updateGroup() {
        binding.progressBar.visibility = View.GONE
    }
}
