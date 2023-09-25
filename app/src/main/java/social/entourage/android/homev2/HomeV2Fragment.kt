package social.entourage.android.homev2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.new_fragment_edit_group.nestedScrollView
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
import social.entourage.android.api.model.User
import social.entourage.android.databinding.FragmentHomeV2LayoutBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.HomePresenter
import social.entourage.android.home.pedago.OnItemClick
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.home.pedago.PedagoListFragmentDirections
import social.entourage.android.notifications.InAppNotificationsActivity
import social.entourage.android.profile.ProfileActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.UserProfileActivity

class HomeV2Fragment: Fragment() {

    //VAR
    private lateinit var binding:FragmentHomeV2LayoutBinding
    private lateinit var homePresenter:HomePresenter
    private var homeGroupAdapter = HomeGroupAdapter()
    private var homeEventAdapter = HomeEventAdapter()
    private var homeActionAdapter = HomeActionAdapter()
    private var homeHelpAdapter = HomeHelpAdapter()
    private var homePedagoAdapter:HomePedagoAdapter? = null
    private var pagegroup = 0
    private var pageEvent = 0
    private var nbOfItemForHozrizontalList = 10
    private var nbOfItemForVerticalList = 3
    private var currentFilters = EventActionLocationFilters()
    private var currentSectionsFilters = ActionSectionFilters()
    private var user: User? = null
    private val NEW_MARGIN = 10
    private val DEFAULT_MARGIN = 80
    private val NEW_MARGIN_LOGO = 10
    private val DEFAULT_MARGIN_LOGO = 30
    private var isAnimating = false



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeV2LayoutBinding.inflate(layoutInflater)
        homePresenter = ViewModelProvider(requireActivity()).get(HomePresenter::class.java)
        homePedagoAdapter = HomePedagoAdapter(object : OnItemClick {
            override fun onItemClick(pedagogicalContent: Pedago) {
                Log.wtf("wtf", "pedagoclicked")
                if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
                    val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
                    intent.putExtra(Const.ID, pedagogicalContent.id)
                    intent.putExtra(Const.HTML_CONTENT, pedagogicalContent.html)
                    requireActivity().startActivity(intent)
                }
            }
        })

        setRecyclerViews()
        setSeeAllButtons()
        setObservations()
        setNotifButton()
        setMapButton()
        setProfileButton()
        setNestedScrollViewAnimation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity) ?: return
        updateAvatar()
        callToInitHome()
    }

    fun callToInitHome(){
        if(isAdded){
            val meId = EntourageApplication.get().me()?.id
            if(meId == null) return
            homePresenter.getMyGroups(pagegroup,nbOfItemForHozrizontalList,meId)
            homePresenter.getAllEvents(pageEvent,nbOfItemForHozrizontalList,currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),"future")
            homePresenter.getAllDemands(0,nbOfItemForVerticalList,currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),currentSectionsFilters.getSectionsForWS())
            homePresenter.getPedagogicalResources()
            homePresenter.getNotificationsCount()
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

    fun setNotifButton(){
        binding.uiLayoutNotif.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_notif)
            val intent = Intent(requireContext(), InAppNotificationsActivity::class.java)
            intent.putExtra(Const.NOTIF_COUNT,homePresenter.notifsCount.value)
            startActivityForResult(intent, 0)
        }
    }

    fun setObservations(){
        homePresenter.summary.observe(requireActivity(), ::updateContributionsView)
        homePresenter.getAllEvents.observe(viewLifecycleOwner,::handleEvent)
        homePresenter.getAllMyGroups.observe(viewLifecycleOwner,::handleGroup)
        homePresenter.getAllActions.observe(viewLifecycleOwner,::handleAction)
        homePresenter.pedagogicalContent.observe(viewLifecycleOwner,::handlePedago)
        homePresenter.notifsCount.observe(requireActivity(), ::updateNotifsCount)

    }

    fun handleGroup(allGroup: MutableList<Group>?){
        if(allGroup == null){
            return
        }
        this.homeGroupAdapter.resetData(allGroup)
    }

    fun handleEvent(allEvent: MutableList<Events>?){
        if(allEvent == null){
            return
        }
        this.homeEventAdapter.resetData(allEvent)

    }
    fun handleAction(allAction: MutableList<Action>?){
        if(allAction == null){
            //
            return
        }

        this.homeActionAdapter.resetData(allAction)

    }
    fun handlePedago(allPedago: MutableList<Pedago>?){
        if(allPedago == null) {

            return
        }
        var pedagos:MutableList<Pedago> = mutableListOf()
        for (k in 0 until 2) {
            pedagos.add(allPedago[k])
        }
        this.homePedagoAdapter?.resetData(pedagos)
        for(pedago in allPedago){
            if(pedago.name!!.contains("Comment créer")){
                Log.wtf("wtf", "pedago name " + pedago.name + " pedago id " + pedago.id)
            }
            if(pedago.name!!.contains("points communs")){
                Log.wtf("wtf", "pedago name " + pedago.name + " pedago id " + pedago.id)
            }
        }
        homePresenter.getSummary()

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


    //HERE JUST RECONNECT OLD FUNCTIONS
    private fun updateNotifsCount(count: Int) {
        Log.wtf("wtf", "notif count ? " + count)
        context?.resources?.let { resources ->
            val bgColor = if (count > 0) {
                ResourcesCompat.getColor(resources, R.color.orange, null)
            } else {
                ResourcesCompat.getColor(resources, R.color.partner_logo_background, null)
            }

            val shapeDrawable = binding.uiLayoutNotif.background as? GradientDrawable
            shapeDrawable?.setColor(bgColor)

            binding.uiBellNotif.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (count > 0) R.drawable.ic_white_notif_on else R.drawable.ic_new_notif_off,
                    null
                )
            )
        }
    }

    private fun updateAvatar() {
        with(binding) {
            avatar.let { photoView ->
                user?.avatarURL?.let { avatarURL ->
                    Glide.with(requireActivity())
                        .load(avatarURL)
                        .placeholder(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(photoView)
                } ?: run {
                    photoView.setImageResource(R.drawable.placeholder_user)
                }

            }
        }
    }

    private fun setMapButton(){
        binding.homeButtonMap.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_map)
            val intent = Intent(requireContext(), GDSMainActivity::class.java)
            startActivityForResult(intent, 0)
        }
    }

    private fun setProfileButton(){
        binding.avatar.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_profile)
            startActivityForResult(
                Intent(context, ProfileActivity::class.java), 0
            )
        }

    }

    private fun setNestedScrollViewAnimation() {
        binding.homeNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val layoutParamsProfile = binding.avatar.layoutParams as ViewGroup.MarginLayoutParams
            val layoutParamsNotif = binding.uiLayoutNotif.layoutParams as ViewGroup.MarginLayoutParams
            val layoutParamsLogo = binding.ivLogoHome.layoutParams as ViewGroup.MarginLayoutParams
            Log.wtf("wtf", "wtf scroll y " + scrollY)
            if(scrollY == 0) isAnimating = false
            if(isAnimating == false){
                isAnimating = true
                val animator = if (scrollY > 50) {
                    ValueAnimator.ofInt(layoutParamsProfile.topMargin, NEW_MARGIN).apply {
                        duration = 100
                        addUpdateListener { animation ->
                            val animatedValue = animation.animatedValue as Int
                            layoutParamsProfile.topMargin = animatedValue
                            layoutParamsNotif.topMargin = animatedValue
                            layoutParamsLogo.topMargin = animatedValue
                            binding.avatar.layoutParams = layoutParamsProfile
                            binding.uiLayoutNotif.layoutParams = layoutParamsNotif
                            binding.ivLogoHome.layoutParams = layoutParamsLogo
                            binding.homeTitle.visibility = View.GONE
                        }
                    }
                } else {
                    ValueAnimator.ofInt(layoutParamsProfile.topMargin, DEFAULT_MARGIN).apply {
                        duration = 100
                        addUpdateListener { animation ->
                            val animatedValue = animation.animatedValue as Int
                            layoutParamsProfile.topMargin = animatedValue
                            layoutParamsNotif.topMargin = animatedValue
                            layoutParamsLogo.topMargin = animatedValue
                            binding.avatar.layoutParams = layoutParamsProfile
                            binding.uiLayoutNotif.layoutParams = layoutParamsNotif
                            binding.ivLogoHome.layoutParams = layoutParamsLogo
                            binding.homeTitle.visibility = View.VISIBLE
                        }
                    }
                }
                animator.doOnEnd {
                    isAnimating = false
                }
                animator.start()
            }
        })
    }
}