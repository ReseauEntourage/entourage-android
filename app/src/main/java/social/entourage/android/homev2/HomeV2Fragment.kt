package social.entourage.android.homev2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSectionFilters
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Help
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Summary
import social.entourage.android.databinding.FragmentHomeV2LayoutBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.home.HomePresenter
import social.entourage.android.home.pedago.PedagoListActivity

class HomeV2Fragment: Fragment() {

    //VAR
    private lateinit var binding:FragmentHomeV2LayoutBinding
    private lateinit var homePresenter:HomePresenter
    private var homeGroupAdapter = HomeGroupAdapter()
    private var homeEventAdapter = HomeEventAdapter()
    private var homeActionAdapter = HomeActionAdapter()
    private var homeHelpAdapter = HomeHelpAdapter()
    private var homePedagoAdapter = HomePedagoAdapter()
    private var pagegroup = 0
    private var pageEvent = 0
    private var nbOfItemForHozrizontalList = 10
    private var nbOfItemForVerticalList = 3
    private var currentFilters = EventActionLocationFilters()
    private var currentSectionsFilters = ActionSectionFilters()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeV2LayoutBinding.inflate(layoutInflater)
        homePresenter = ViewModelProvider(requireActivity()).get(HomePresenter::class.java)
        setRecyclerViews()
        setSeeAllButtons()
        setObservations()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callToInitHome()
    }

    fun callToInitHome(){
        if(isAdded){
            val meId = EntourageApplication.get().me()?.id
            if(meId == null) return
            homePresenter.getSummary()
            homePresenter.getMyGroups(pagegroup,nbOfItemForHozrizontalList,meId)
            homePresenter.getAllEvents(pageEvent,nbOfItemForHozrizontalList,currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),"future")
            homePresenter.getAllDemands(0,nbOfItemForVerticalList,currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),currentSectionsFilters.getSectionsForWS())
            homePresenter.getPedagogicalResources()

        }
    }

    private fun setRecyclerViews(){
        //Group RV
        val settingGrouplayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomeGroup.adapter = homeGroupAdapter
        binding.rvHomeGroup.layoutManager = settingGrouplayoutManager
        //Event RV
        val settingEventlayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomeEvent.adapter = homeEventAdapter
        binding.rvHomeEvent.layoutManager = settingEventlayoutManager
        //Action RV
        val settingActionlayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHomeAction.adapter = homeActionAdapter
        binding.rvHomeAction.layoutManager = settingActionlayoutManager
        //Pedago RV
        val settingPedagolayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHomePedago.adapter = homePedagoAdapter
        binding.rvHomePedago.layoutManager = settingPedagolayoutManager
        //Help RV
        val settingHelplayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHomeHelp.adapter = homeHelpAdapter
        binding.rvHomeHelp.layoutManager = settingHelplayoutManager

    }

    fun setSeeAllButtons(){
        val mainActivity = (requireActivity() as? MainActivity)
        binding.btnMoreGroup.setOnClickListener { mainActivity?.goGroup() }
        binding.btnMoreEvent.setOnClickListener { mainActivity?.goEvent() }
        binding.btnMoreAction.setOnClickListener { mainActivity?.goDemand() }
        binding.btnMorePedago.setOnClickListener {
            val intent = Intent(requireActivity(), PedagoListActivity::class.java)
            requireContext().startActivity(intent)
        }
    }

    fun setObservations(){
        homePresenter.summary.observe(requireActivity(), ::updateContributionsView)
        homePresenter.getAllEvents.observe(viewLifecycleOwner,::handleEvent)
        homePresenter.getAllMyGroups.observe(viewLifecycleOwner,::handleGroup)
        homePresenter.getAllActions.observe(viewLifecycleOwner,::handleAction)
        homePresenter.pedagogicalContent.observe(viewLifecycleOwner,::handlePedago)
    }

    fun handleGroup(allGroup: MutableList<Group>?){
        if(allGroup == null) return
        this.homeGroupAdapter.resetData(allGroup!!)
    }

    fun handleEvent(allEvent: MutableList<Events>?){
        if(allEvent == null) return
        this.homeEventAdapter.resetData(allEvent!!)

    }
    fun handleAction(allAction: MutableList<Action>?){
        if(allAction == null) return
        this.homeActionAdapter.resetData(allAction!!)

    }
    fun handlePedago(allPedago: MutableList<Pedago>?){
        if(allPedago == null) return
        var pedagos:MutableList<Pedago> = mutableListOf()
        for(pedago in allPedago){
            Log.wtf("wtf" , Gson().toJson(pedago))
        }
        this.homePedagoAdapter.resetData(pedagos)
    }

    private fun updateContributionsView(summary: Summary) {
        handleHelps(summary)
    }

    fun handleHelps(summary: Summary){
        val formattedString = requireContext().getString(R.string.home_v2_help_title_three, summary.moderator?.displayName)

        val help1 = Help(requireContext().getString(R.string.home_v2_help_title_one) , R.drawable.first_help_item_illu)
        val help2 = Help(requireContext().getString(R.string.home_v2_help_title_two) , R.drawable.first_help_item_illu)
        val help3 = Help(formattedString , R.drawable.first_help_item_illu)
        var helps:MutableList<Help> = mutableListOf()
        helps.add(help1)
        helps.add(help2)
        helps.add(help3)
        homeHelpAdapter.resetData(helps)
    }

}