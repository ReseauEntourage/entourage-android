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
import social.entourage.android.databinding.NewFragmentGroupsBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.create.CreateGroupActivity
import social.entourage.android.homev2.HomeGroupAdapter
import social.entourage.android.tools.log.AnalyticsEvents

class GroupeV2Fragment: Fragment() {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = GroupV2FragmentLayoutBinding.bind(view)
        myId = EntourageApplication.me(activity)?.id
        presenter = ViewModelProvider(requireActivity()).get(GroupPresenter::class.java)
        presenter.getAllGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        presenter.getAllMyGroups.observe(viewLifecycleOwner, ::handleResponseMyGetGroups)
        adapterGroup = GroupsListAdapter(groupsList, null, FromScreen.DISCOVER)
        adapterMyGroup= HomeGroupAdapter()
        binding.recyclerViewVertical.adapter = adapterGroup
        binding.recyclerViewHorizontal.adapter = adapterMyGroup
        binding.recyclerViewVertical.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            activity,
            androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerViewVertical.isVerticalScrollBarEnabled = false
        binding.recyclerViewHorizontal.apply {
            // Pagination
            val settinglayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = settinglayoutManager
            val offsetInPixels = resources.getDimensionPixelSize(R.dimen.horizontal_offset) // Define this in your resources
            setPadding(offsetInPixels, 0, 0, 0)
            clipToPadding = false
            isHorizontalScrollBarEnabled = false
        }
        handleCreateGroupButton()
        setupScrollViewListener()
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_GROUP_SHOW)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GroupV2FragmentLayoutBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_SHOW_DISCOVER
        )
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        groupsList.clear()
        myGroupsList.clear()
        presenter.getAllGroups(page, 100)
        if (myId != null) {
            presenter.getMyGroups(pageMy, 100, myId!!)
        }
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let { groupsList.addAll(it)
            adapterGroup.updateGroupsList(groupsList)
        }
        checkingSumForEmptyView()


    }

    private fun handleResponseMyGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let {
            myGroupsList.addAll(it)
            adapterMyGroup.resetData(myGroupsList)
        }
        checkingSumForEmptyView()
    }

    private fun checkingSumForEmptyView() {
        checkSum++
        if(checkSum >= 1){
            binding.progressBar.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }
    private fun handleCreateGroupButton() {
        binding.createGroup.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_PLUS
            )
            startActivityForResult(Intent(context, CreateGroupActivity::class.java), 0)
        }
    }
    private fun setupScrollViewListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val newFontSize = calculateFontSize(scrollY)
                binding.textViewTitleGroupes.textSize = newFontSize
            }
        }
    }
    private fun calculateFontSize(scrollY: Int): Float {
        // Ici, tu peux ajuster la logique de calcul en fonction de tes besoins
        val minSize = 15f // taille minimale du texte
        val maxSize = 24f // taille maximale du texte
        val scrollThreshold = 100 // ajuste cette valeur en fonction du seuil de défilement souhaité

        return if (scrollY > scrollThreshold) minSize else maxSize
    }
}