package social.entourage.android.homev2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.gson.Gson
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.actions.ActionsPresenter
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
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.HomePresenter
import social.entourage.android.home.pedago.OnItemClick
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.notifications.InAppNotificationsActivity
import social.entourage.android.profile.ProfileActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.user.UserPresenter
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.user.edit.place.UserEditActionZoneFragment

class HomeV2Fragment: Fragment(), OnHomeV2HelpItemClickListener, OnHomeV2ChangeLocationUpdate {

    //VAR
    private lateinit var binding:FragmentHomeV2LayoutBinding
    private lateinit var homePresenter:HomePresenter
    private var homeGroupAdapter = HomeGroupAdapter()
    private lateinit var homeEventAdapter:HomeEventAdapter
    private var homeActionAdapter = HomeActionAdapter(false)
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private lateinit var homeHelpAdapter:HomeHelpAdapter
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
    private var pedagoItemForCreateEvent:Pedago? = null
    private var pedagoItemForCreateGroup:Pedago? = null
    private var checksum = 0
    private var totalchecksum = 0
    private var isEventsEmpty = false
    private var isActionEmpty = false
    private var isContribution = false
    private lateinit var actionsPresenter: ActionsPresenter
    private var locationPopupHasPop = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        totalchecksum = 0
        binding = FragmentHomeV2LayoutBinding.inflate(layoutInflater)
        binding.homeNestedScrollView.visibility = View.GONE
        disapearAllAtBeginning()
        userPresenter.user.observe(viewLifecycleOwner, ::updateUser)
        binding.progressBar.visibility = View.VISIBLE
        homePresenter = ViewModelProvider(requireActivity()).get(HomePresenter::class.java)
        actionsPresenter = ViewModelProvider(requireActivity()).get(ActionsPresenter::class.java)
        homeHelpAdapter = HomeHelpAdapter(this)
        homeEventAdapter = HomeEventAdapter(requireContext())
        homePedagoAdapter = HomePedagoAdapter(object : OnItemClick {
            override fun onItemClick(pedagogicalContent: Pedago) {
                if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
                    val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
                    intent.putExtra(Const.ID, pedagogicalContent.id)
                    //intent.putExtra(Const.HTML_CONTENT, pedagogicalContent.html)
                    requireActivity().startActivity(intent)
                }
            }
        })
        AnalyticsEvents.logEvent(AnalyticsEvents.View__Home)
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
    }

    override fun onResume() {
        super.onResume()
        checksum = 0
        resetFilter()
        callToInitHome()

    }

    fun noAdressPopFillAdress(){
        if(!locationPopupHasPop){
            locationPopupHasPop = true
            if(user?.address == null){

                AnalyticsEvents.logEvent(AnalyticsEvents.view_miss_location_popup)
                CustomAlertDialog.showOnlyOneButtonNoClose(requireContext(),
                    getString(R.string.home_v2_no_adress_title),
                    getString(R.string.home_v2_no_adress_content),
                    getString(R.string.home_v2_no_adress_button)
                ) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.clic_miss_location_add)
                    binding.frameLayoutChangeLocation.visibility = View.VISIBLE
                    childFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame_layout_change_location,
                            UserEditActionZoneFragment.newInstance(null, false, this)
                        )
                        .commit()
                }
            }
        }
    }

    fun callToInitHome(){

        if(isAdded){
            val meId = EntourageApplication.get().me()?.id
            if(meId == null) return
            homePresenter.getMyGroups(pagegroup,nbOfItemForHozrizontalList,meId)
            homePresenter.getAllEvents(pageEvent,nbOfItemForHozrizontalList,currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),"future")
            homePresenter.getPedagogicalResources()
            homePresenter.getNotificationsCount()
            userPresenter.getUser(meId)
        }
    }

    private fun checkSumEventAction(){
        checksum++
        if (checksum == 2){
            if(isEventsEmpty && isActionEmpty){
                binding.itemHz.layoutItemHz.visibility = View.VISIBLE
            }else{
                binding.itemHz.layoutItemHz.visibility = View.GONE
            }
        }
        binding.itemHz.buttonHzItem.setOnClickListener {
            val urlString = "https://reseauentourage.notion.site/Buffet-du-lien-social-69c20e089dbd483cb093e90ae2953a54"
            WebViewFragment.newInstance(urlString, 0, true)
                .show(requireActivity().supportFragmentManager, WebViewFragment.TAG)
        }
    }

    private fun doTotalchecksumToDisplayHomeFirstTime(){
        totalchecksum++
        if(totalchecksum == 5){
            binding.homeNestedScrollView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    fun resetFilter(){
        currentFilters = EventActionLocationFilters()
        currentSectionsFilters = ActionSectionFilters()
    }

    fun disapearAllAtBeginning(){
        binding.btnMoreGroup.visibility = View.GONE
        binding.rvHomeGroup.visibility = View.GONE
        binding.homeSubtitleGroup.visibility = View.GONE
        binding.homeTitleGroup.visibility = View.GONE
        binding.btnMoreEvent.visibility = View.GONE
        binding.rvHomeEvent.visibility = View.GONE
        binding.homeSubtitleEvent.visibility = View.GONE
        binding.homeTitleEvent.visibility = View.GONE

        binding.btnMoreAction.visibility = View.GONE
        binding.rvHomeAction.visibility = View.GONE
        binding.homeSubtitleAction.visibility = View.GONE
        binding.homeTitleAction.visibility = View.GONE

        binding.btnMorePedago.visibility = View.GONE
        binding.rvHomePedago.visibility = View.GONE
        binding.homeSubtitlePedago.visibility = View.GONE
        binding.homeTitlePedago.visibility = View.GONE
    }

    private fun setRecyclerViews(){
        //Group RV
        val settingGrouplayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val offsetInPixels = resources.getDimensionPixelSize(R.dimen.horizontal_offset_home) // Define this in your resources

        binding.rvHomeGroup.adapter = homeGroupAdapter
        binding.rvHomeGroup.layoutManager = settingGrouplayoutManager
        binding.rvHomeGroup.setPadding(offsetInPixels, 0, 0, 0)
        binding.rvHomeGroup.clipToPadding = false
        //Event RV
        val settingEventlayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomeEvent.adapter = homeEventAdapter
        binding.rvHomeEvent.layoutManager = settingEventlayoutManager
        binding.rvHomeEvent.setPadding(offsetInPixels, 0, 0, 0)
        binding.rvHomeEvent.clipToPadding = false
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
        binding.btnMoreGroup.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Group_All)
            mainActivity?.setGoDiscoverGroupFromDeepL(true)
            mainActivity?.goGroup()
        }
        binding.btnMoreEvent.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Event_All)
            mainActivity?.goEvent()
        }
        binding.btnMoreAction.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Demand_All)
            mainActivity?.goDemand()
        }
        binding.btnMorePedago.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Pedago)
            val intent = Intent(requireActivity(), PedagoListActivity::class.java)
            requireContext().startActivity(intent)
        }
    }

    fun setNotifButton(){
        binding.uiLayoutNotif.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Notif)
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
        doTotalchecksumToDisplayHomeFirstTime()

        if(allGroup.size > 0 ){
            binding.btnMoreGroup.visibility = View.VISIBLE
            binding.rvHomeGroup.visibility = View.VISIBLE
            binding.homeSubtitleGroup.visibility = View.VISIBLE
            binding.homeTitleGroup.visibility = View.VISIBLE
        }else{
            binding.btnMoreGroup.visibility = View.GONE
            binding.rvHomeGroup.visibility = View.GONE
            binding.homeSubtitleGroup.visibility = View.GONE
            binding.homeTitleGroup.visibility = View.GONE
        }
        this.homeGroupAdapter.resetData(allGroup)

    }

    fun handleEvent(allEvent: MutableList<Events>?){
        if(allEvent == null){
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()

        if(allEvent.size > 0 ){
            var _offline_events:MutableList<Events> = mutableListOf()
            for(event in allEvent){
                if(event.online == false){
                    _offline_events.add(event)
                }
            }
            if(_offline_events.size == 0){
                isEventsEmpty = true
            }else{
                isEventsEmpty = false
            }
            binding.btnMoreEvent.visibility = View.VISIBLE
            binding.rvHomeEvent.visibility = View.VISIBLE
            binding.homeSubtitleEvent.visibility = View.VISIBLE
            binding.homeTitleEvent.visibility = View.VISIBLE
        }else{
            isEventsEmpty = true
            binding.btnMoreEvent.visibility = View.GONE
            binding.rvHomeEvent.visibility = View.GONE
            binding.homeSubtitleEvent.visibility = View.GONE
            binding.homeTitleEvent.visibility = View.GONE
        }
        checkSumEventAction()
        this.homeEventAdapter.resetData(allEvent)

    }
    fun handleAction(allAction: MutableList<Action>?){
        if(allAction == null){
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()

        if(allAction.size > 0 ){
            if(isContribution){
                binding.homeTitleAction.text = getString(R.string.home_v2_title_action_contrib)
                binding.homeSubtitleAction.text = getString(R.string.home_v2_subtitle_action_contrib)
                binding.titleButtonAction.text = getString(R.string.home_v2_btn_more_action_contrib)
                binding.btnMoreAction.setOnClickListener {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Contrib_All)
                    val mainActivity = (requireActivity() as? MainActivity)
                    mainActivity?.goContrib()
                }
            }
            isActionEmpty = false
            binding.btnMoreAction.visibility = View.VISIBLE
            binding.rvHomeAction.visibility = View.VISIBLE
            binding.homeSubtitleAction.visibility = View.VISIBLE
            binding.homeTitleAction.visibility = View.VISIBLE
        }else{
            isActionEmpty = true
            binding.btnMoreAction.visibility = View.GONE
            binding.rvHomeAction.visibility = View.GONE
            binding.homeSubtitleAction.visibility = View.GONE
            binding.homeTitleAction.visibility = View.GONE
        }
        if(!isContribution){
            checkSumEventAction()
        }
        this.homeActionAdapter.resetData(allAction)

    }
    fun handlePedago(allPedago: MutableList<Pedago>?){
        if(allPedago == null) {
            return
        }
        doTotalchecksumToDisplayHomeFirstTime()
        if(allPedago.size > 0 ){
            binding.btnMorePedago.visibility = View.VISIBLE
            binding.rvHomePedago.visibility = View.VISIBLE
            binding.homeSubtitlePedago.visibility = View.VISIBLE
            binding.homeTitlePedago.visibility = View.VISIBLE
        }else{
            binding.btnMorePedago.visibility = View.GONE
            binding.rvHomePedago.visibility = View.GONE
            binding.homeSubtitlePedago.visibility = View.GONE
            binding.homeTitlePedago.visibility = View.GONE
        }
        var pedagos:MutableList<Pedago> = mutableListOf()
        for(pedago in allPedago){
            if (pedagos.size > 1){
                break
            }
            if(pedago.watched == false){
                pedagos.add(pedago)
            }
        }
        /*for (k in 0 until 2) {
            pedagos.add(allPedago[k])
        }*/
        for(pedago in allPedago) {

            pedago.id?.let { id ->
                val createEventId: Int = BuildConfig.PEDAGO_CREATE_EVENT_ID.toInt()
                val createGroupId: Int = BuildConfig.PEDAGO_CREATE_GROUP_ID.toInt()
                if(id == createEventId) {
                    this.pedagoItemForCreateEvent = pedago
                }
                if(id == createGroupId) {
                    this.pedagoItemForCreateGroup = pedago
                }
            }
        }

        this.homePedagoAdapter?.resetData(pedagos)
        homePresenter.getSummary()

    }

    private fun updateContributionsView(summary: Summary) {
        onActionUnclosed(summary)
        handleHelps(summary)
        isContribution = summary.preference.equals("contribution")
        isContribProfile = isContribution
        if(isContribution){
            if(!homeActionAdapter.getIsContrib()){
                homeActionAdapter = HomeActionAdapter(isContribution)
            }
            binding.rvHomeAction.adapter = homeActionAdapter
            homePresenter.getAllContribs(0,nbOfItemForVerticalList,currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),currentSectionsFilters.getSectionsForWS())
        }else{
            homePresenter.getAllDemands(0,nbOfItemForVerticalList,currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),currentSectionsFilters.getSectionsForWS())
        }
    }

    fun handleHelps(summary: Summary){
        if(isAdded){
            doTotalchecksumToDisplayHomeFirstTime()
            val formattedString = requireContext().getString(R.string.home_v2_help_title_three, summary.moderator?.displayName)
            val help1 = Help(requireContext().getString(R.string.home_v2_help_title_one) , R.drawable.first_help_item_illu)
            val help2 = Help(requireContext().getString(R.string.home_v2_help_title_two) , R.drawable.ic_home_v2_create_group)
            val help3 = Help(formattedString , R.drawable.first_help_item_illu)
            var helps:MutableList<Help> = mutableListOf()
            helps.add(help1)
            helps.add(help2)
            helps.add(help3)
            homeHelpAdapter.resetData(helps, summary)
        }
    }
    //HERE JUST RECONNECT OLD FUNCTIONS
    private fun updateNotifsCount(count: Int) {
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
    private fun updateUser(user:User){
        this.user = user
        updateAvatar()
        noAdressPopFillAdress()
    }

    private fun setMapButton(){
        binding.homeButtonMap.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Map)
            val intent = Intent(requireContext(), GDSMainActivity::class.java)
            startActivityForResult(intent, 0)
        }
    }

    private fun setProfileButton(){
        binding.avatar.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Tab__Profil)
            startActivityForResult(
                Intent(context, ProfileActivity::class.java), 0
            )
        }

    }

    private fun setNestedScrollViewAnimation() {
        binding.homeNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val layoutParamsProfile = binding.avatar.layoutParams as ViewGroup.MarginLayoutParams
            val layoutParamsNotif = binding.uiLayoutNotif.layoutParams as ViewGroup.MarginLayoutParams
            val layoutParamsLogo = binding.ivLogoHome.layoutParams as ViewGroup.MarginLayoutParams

            if (isAnimating) {
                return@OnScrollChangeListener // Ne faites rien si une animation est déjà en cours
            }

            if (scrollY == 0) {
                isAnimating = false
                // Réinitialisez les valeurs ici si scrollY revient à 0
                layoutParamsProfile.topMargin = DEFAULT_MARGIN
                layoutParamsNotif.topMargin = DEFAULT_MARGIN
                layoutParamsLogo.topMargin = DEFAULT_MARGIN
                binding.avatar.layoutParams = layoutParamsProfile
                binding.uiLayoutNotif.layoutParams = layoutParamsNotif
                binding.ivLogoHome.layoutParams = layoutParamsLogo
                binding.homeTitle.visibility = View.VISIBLE
            } else if (scrollY > 50 && oldScrollY <= 50) {
                isAnimating = true
                startAnimation(layoutParamsProfile, layoutParamsNotif, layoutParamsLogo, View.GONE)
            } else if (scrollY <= 50 && oldScrollY > 50) {
                isAnimating = true
                startAnimation(layoutParamsProfile, layoutParamsNotif, layoutParamsLogo, View.VISIBLE)
            }
        })
    }

    private fun startAnimation(layoutParamsProfile: ViewGroup.MarginLayoutParams, layoutParamsNotif: ViewGroup.MarginLayoutParams, layoutParamsLogo: ViewGroup.MarginLayoutParams, titleVisibility: Int) {
        val animator = ValueAnimator.ofInt(layoutParamsProfile.topMargin, if (titleVisibility == View.GONE) NEW_MARGIN else DEFAULT_MARGIN).apply {
            duration = 100
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                layoutParamsProfile.topMargin = animatedValue
                layoutParamsNotif.topMargin = animatedValue
                layoutParamsLogo.topMargin = animatedValue
                binding.avatar.layoutParams = layoutParamsProfile
                binding.uiLayoutNotif.layoutParams = layoutParamsNotif
                binding.ivLogoHome.layoutParams = layoutParamsLogo
                binding.homeTitle.visibility = titleVisibility
            }
            doOnEnd {
                isAnimating = false
            }
        }
        animator.start()
    }

    override fun onItemClick(position: Int, moderatorId:Int) {
        if(position == 0){
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_CreateGroup)
            val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
            intent.putExtra(Const.ID, pedagoItemForCreateGroup?.id)
            intent.putExtra(Const.HTML_CONTENT, pedagoItemForCreateGroup?.html)
            requireActivity().startActivity(intent)
        }
        if(position == 1){
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_CreateEvent)
            val intent = Intent(requireActivity(), PedagoDetailActivity::class.java)
            intent.putExtra(Const.ID, pedagoItemForCreateEvent?.id)
            intent.putExtra(Const.HTML_CONTENT, pedagoItemForCreateEvent?.html)
            requireActivity().startActivity(intent)
        }
        if(position == 2){
            AnalyticsEvents.logEvent(AnalyticsEvents.Action__Home__Moderator)
            startActivity(
                Intent(context, UserProfileActivity::class.java).putExtra(
                    Const.USER_ID,
                    moderatorId
                )
            )
        }
    }
    private fun onActionUnclosed(summary: Summary){
        if(summary.unclosedAction != null){
            if(summary.unclosedAction!!.actionType == "solicitation"){
                AnalyticsEvents.logEvent(AnalyticsEvents.View__StateDemandPop__Day10)
                val contentText = summary.unclosedAction!!.title
                CustomAlertDialog.showForLastActionOneDemand(
                    requireContext(),
                    getString(R.string.custom_dialog_action_title_one_demand),
                    contentText!!,
                    getString(R.string.custom_dialog_action_content_one_demande),
                    getString(R.string.yes),
                    onNo = {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateDemandPop__No__Day10)
                        AnalyticsEvents.logEvent(AnalyticsEvents.View__StateDemandPop__No__Day10)
                        CustomAlertDialog.showForLastActionTwo(requireContext(),
                            getString(R.string.custom_dialog_action_title_two),
                            getString(R.string.custom_dialog_action_content_two_demande),
                            getString(R.string.custom_dialog_action_two_button_contrib),
                            onYes = {
                                (activity as MainActivity).goContrib()
                                AnalyticsEvents.logEvent(AnalyticsEvents.Clic__SeeDemand__Day10)
                            })
                    },
                    onYes = {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateDemandPop__Yes__Day10)
                        AnalyticsEvents.logEvent(AnalyticsEvents.View__DeleteDemandPop__Day10)
                        actionsPresenter.cancelAction(summary.unclosedAction!!.id!!,true,true, "")
                        CustomAlertDialog.showForLastActionThree(requireContext(),
                            getString(R.string.custom_dialog_action_title_three),
                            getString(R.string.custom_dialog_action_content_three_demande))

                    }
                )
            }
            if(summary.unclosedAction!!.actionType == "contribution"){
                AnalyticsEvents.logEvent(AnalyticsEvents.View__StateContribPop__Day10)
                val contentText = summary.unclosedAction!!.title
                CustomAlertDialog.showForLastActionOneContrib(
                    requireContext(),
                    getString(R.string.custom_dialog_action_title_one_contrib),
                    contentText!!,
                    getString(R.string.custom_dialog_action_content_one_contrib),
                    getString(R.string.yes),
                    onNo = {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateContribPop__No__Day10)
                        AnalyticsEvents.logEvent(AnalyticsEvents.View__StateContribPop__No__Day10)
                        CustomAlertDialog.showForLastActionTwo(requireContext(),
                            getString(R.string.custom_dialog_action_title_two),
                            getString(R.string.custom_dialog_action_content_two_contrib),
                            getString(R.string.custom_dialog_action_two_button_demand),
                            onYes = {
                                (activity as MainActivity).goDemand()
                                AnalyticsEvents.logEvent(AnalyticsEvents.Clic__SeeContrib__Day10)

                            })

                    },
                    onYes = {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateContribPop__Yes__Day10)
                        AnalyticsEvents.logEvent(AnalyticsEvents.View__DeleteContribPop__Day10)
                        actionsPresenter.cancelAction(summary.unclosedAction!!.id!!,false,true, "")
                        CustomAlertDialog.showForLastActionThree(requireContext(),
                            getString(R.string.custom_dialog_action_title_three),
                            getString(R.string.custom_dialog_action_content_three_contrib))
                    }
                )
            }
        }
    }
    companion object {
        var isContribProfile = false
    }

    override fun onHomeV2ChangeLocationUpdateClearFragment() {
        binding.frameLayoutChangeLocation.visibility = View.GONE
        callToInitHome()
    }
}

interface OnHomeV2ChangeLocationUpdate{
    fun onHomeV2ChangeLocationUpdateClearFragment()
}