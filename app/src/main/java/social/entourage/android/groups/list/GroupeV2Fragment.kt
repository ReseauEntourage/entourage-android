package social.entourage.android.groups.list

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.GroupV2FragmentLayoutBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.create.CreateGroupActivity
import social.entourage.android.homev2.HomeGroupAdapter
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.tools.log.AnalyticsEvents

class GroupeV2Fragment: Fragment(), UpdateGroupInter {

    private lateinit var binding: GroupV2FragmentLayoutBinding
    private lateinit var presenter: GroupPresenter
    private var groupsList: MutableList<Group> = ArrayList()
    private var myGroupsList: MutableList<Group> = ArrayList()
    private var myId: Int? = null
    private var page: Int = 0
    private var pageMy: Int = 0
    private lateinit var adapterGroup: GroupsListAdapter
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
        presenter.getAllGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        presenter.getAllMyGroups.observe(viewLifecycleOwner, ::handleResponseMyGetGroups)
        adapterGroup = GroupsListAdapter(groupsList, myId, FromScreen.DISCOVER, this)
        adapterMyGroup = HomeGroupAdapter()
        binding.recyclerViewVertical.adapter = adapterGroup
        binding.recyclerViewHorizontal.adapter = adapterMyGroup
        binding.recyclerViewVertical.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.layoutFilter.setOnClickListener {
            isFirstResumeWithFilters = true
            val intent = Intent(activity, MainFilterActivity::class.java)
            startActivity(intent)
        }

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

        val currentFiltersHash = getCurrentFiltersHash()
        if (currentFiltersHash != lastFiltersHash) {
            isFirstResumeWithFilters = true
            lastFiltersHash = currentFiltersHash
        }

        if (MainFilterActivity.savedInterests.size > 0) {
            binding.cardFilterNumber.visibility = View.VISIBLE
            binding.tvNumberOfFilter.text = MainFilterActivity.savedInterests.size.toString()

            if (isFirstResumeWithFilters) {
                isFirstResumeWithFilters = false
                groupsList.clear()
                myGroupsList.clear()
                addedGroupIds.clear()
                addedMyGroupIds.clear()
                adapterGroup.notifyDataSetChanged()
                adapterMyGroup.notifyDataSetChanged()
                page = 0
                pageMy = 0
                applyFilters()
            } else {
                loadMoreGroups()
            }
        } else {
            binding.cardFilterNumber.visibility = View.GONE
        }
    }

    private fun getCurrentFiltersHash(): Int {
        return (MainFilterActivity.savedInterests.joinToString(",") +
                MainFilterActivity.savedRadius +
                MainFilterActivity.savedLocation?.name).hashCode()
    }

    private fun initView() {
        binding.progressBar.visibility = View.VISIBLE
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
    }

    private fun handleResponseMyGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let {
            val newGroups = it.filter { group -> !addedMyGroupIds.contains(group.id) }
            myGroupsList.addAll(newGroups)
            adapterMyGroup.resetData(myGroupsList)
            addedMyGroupIds.addAll(newGroups.map { group -> group.id!! })
        }
        checkingSumForEmptyView()
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
        }
    }

    private fun loadMoreGroups() {
        binding.progressBar.visibility = View.VISIBLE
        if (MainFilterActivity.savedInterests.isNotEmpty()) {
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
        val interests = MainFilterActivity.savedInterests.joinToString(",")
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
