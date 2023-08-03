package social.entourage.android.home

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.new_home_card.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentHomeBinding
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.Navigation
import social.entourage.android.ViewPagerDefaultPageController
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.notifications.InAppNotificationsActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.api.model.HomeAction
import social.entourage.android.api.model.Summary
import social.entourage.android.api.model.SummaryAction
import social.entourage.android.api.model.User
import social.entourage.android.groups.details.feed.FeedActivity
import social.entourage.android.profile.ProfileActivity
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.CommunicationRecoWebUrlHandlerViewModel
import social.entourage.android.welcome.WelcomeOneActivity
import social.entourage.android.welcome.WelcomeTestActivity
import social.entourage.android.welcome.WelcomeThreeActivity
import social.entourage.android.welcome.WelcomeTwoActivity
import timber.log.Timber
import java.time.LocalDate
import java.util.Calendar

class HomeFragment : Fragment() {
    private var _binding: NewFragmentHomeBinding? = null
    val binding: NewFragmentHomeBinding get() = _binding!!
    private val homePresenter: HomePresenter by lazy { HomePresenter() }
    private lateinit var actionsPresenter: ActionsPresenter

    private var user: User? = null
    private var userSummary: Summary? = null

    private var isAlreadyLoadSummary = false
    private var timer:CountDownTimer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity) ?: return
        actionsPresenter = ViewModelProvider(requireActivity()).get(ActionsPresenter::class.java)
        homePresenter.summary.observe(requireActivity(), ::updateContributionsView)
        isAlreadyLoadSummary = true

        val viewModel = ViewModelProvider(requireActivity()).get(
            CommunicationRecoWebUrlHandlerViewModel::class.java)
        viewModel.isValid.observe(requireActivity(), ::reloadDatasFromRecos)

        updateView()
        handleProfileButton()
        handlePedagogicalContentButton()
        homePresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        homePresenter.getUnreadCount()
        homePresenter.notifsCount.observe(requireActivity(), ::updateNotifsCount)
       timer = object: CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                showCongratDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homePresenter.getSummary()

        reloadDatasFromRecos(true)
        homePresenter.getNotificationsCount()
        AnalyticsEvents.logEvent(AnalyticsEvents.Home_view_home)
        showAlertForRugbyDay()
        //TODO : suppress this testing code
//        var summary = Summary()
//        var action = SummaryAction()
//        action.title = "ma contrib/demande"
//        action.actionType = "solicitation"
//        action.id = 10000
//        summary.unclosedAction = action
//        onActionUnclosed(summary)
    }

    /*    private fun updateNotifsCount(count:Int) {
        context?.resources?.let { resources->


            binding.uiBellNotif.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (count > 0) R.drawable.ic_new_notif_on else R.drawable,
                    null
                )
            )
        }
    }
*/

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

    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        val count:Int = unreadMessages?.unreadCount ?: 0
       EntourageApplication.get().mainActivity?.let {
           val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
           viewModel.badgeCount.postValue(UnreadMessages(count))
        }
    }

    private fun reloadDatasFromRecos(isOk:Boolean) {
        if (!isAlreadyLoadSummary) {
            isAlreadyLoadSummary = true
            homePresenter.getSummary()
        }

    }

    private fun updateContributionsView(summary: Summary) {
        if(isAdded){
            isAlreadyLoadSummary = false
            userSummary = summary
            with(binding) {
                summary.meetingsCount?.let {
                    meetingLabel.text =
                        if (it <= 0) getString(R.string.contributions_meeting_empty) else getString(R.string.contributions_meeting)
                    meetingValue.text = it.toString()
                    heartIcon.isVisible = it > 0
                    heartIconEmpty.isVisible = it <= 0
                }

                summary.outingParticipationsCount?.let {
                    eventCard.value.text = it.toString()
                    eventCard.isEmpty.isVisible = it <= 0
                    eventCard.isNotEmpty.isVisible = it > 0
                    eventCard.label.text = if (it < 2) getString(R.string.contribution_event) else getString(R.string.contributions_event)
                }

                summary.neighborhoodParticipationsCount?.let {
                    groupCard.value.text = it.toString()
                    groupCard.isEmpty.isVisible = it <= 0
                    groupCard.isNotEmpty.isVisible = it > 0
                    groupCard.label.text = if (it < 2) getString(R.string.contribution_group) else getString(R.string.contributions_group)
                }
            }
            summary.recommendations?.let { setRecommendationsList(it) }
            initializeHelpSection()
            handleOnClickCounters()

            if ((homePresenter.summary.value?.congratulations?.count() ?: 0) > 0 ) {
                val timer = object: CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        showCongratDialog()
                    }
                }
                timer.start()
            }
        }
        //TODO TO RECONECT FOR UnclosedActions
        onActionUnclosed(summary)
    }

    fun showAlertForRugbyDay() {
        val currentCalendar = Calendar.getInstance()
        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.AUGUST)
            set(Calendar.DAY_OF_MONTH, 3)
        }

        // Remettre à zéro les heures, minutes, secondes et millisecondes
        listOf(currentCalendar, targetCalendar).forEach {
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }

        if (currentCalendar.time == targetCalendar.time) {
            CustomAlertDialog.showRugbyPopUpWithCancelFirst(
                requireContext(),
                getString(R.string.pop_up_rugby_france_title),
                getString(R.string.pop_up_rugby_france_content),
                getString(R.string.join),
            ) {
                //HERE GO TO GROUP SPORT efEKBnEVujAU
                requireActivity().startActivity(
                    Intent(requireContext(), FeedActivity::class.java).putExtra(
                        Const.GROUP_ID,
                        44
                    )
                )
            }
        }
    }

    private fun onActionUnclosed(summary: Summary){
        if(summary.unclosedAction != null){
            if(summary.unclosedAction!!.actionType == "solicitation"){
                AnalyticsEvents.logEvent(AnalyticsEvents.View__StateDemandPop__Day10)
                val contentText = summary.unclosedAction!!.title
                CustomAlertDialog.showForLastActionOne(
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
                            getString(R.string.custom_dialog_action_two_button_demand),
                        onYes = {
                            (activity as MainActivity).goDemand()
                            AnalyticsEvents.logEvent(AnalyticsEvents.Clic__SeeDemand__Day10)
                        })
                    },
                    onYes = {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateDemandPop__Yes__Day10)
                        actionsPresenter.cancelAction(summary.unclosedAction!!.id!!,true,true, "")
                        AnalyticsEvents.logEvent(AnalyticsEvents.View__DeleteDemandPop__Day10)
                        CustomAlertDialog.showForLastActionThree(requireContext(),
                            getString(R.string.custom_dialog_action_title_three),
                            getString(R.string.custom_dialog_action_content_three_demande))
                    }
                )
            }
            if(summary.unclosedAction!!.actionType == "contribution"){
                AnalyticsEvents.logEvent(AnalyticsEvents.View__StateContribPop__Day10)
                val contentText = summary.unclosedAction!!.title
                CustomAlertDialog.showForLastActionOne(
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
                            getString(R.string.custom_dialog_action_two_button_contrib),
                            onYes = {
                                (activity as MainActivity).goContrib()
                                AnalyticsEvents.logEvent(AnalyticsEvents.Clic__SeeContrib__Day10)

                            })

                    },
                    onYes = {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Clic__StateContribPop__Yes__Day10)
                        actionsPresenter.cancelAction(summary.unclosedAction!!.id!!,false,true, "")
                        AnalyticsEvents.logEvent(AnalyticsEvents.View__DeleteContribPop__Day10)
                        CustomAlertDialog.showForLastActionThree(requireContext(),
                            getString(R.string.custom_dialog_action_title_three),
                            getString(R.string.custom_dialog_action_content_three_contrib))
                    }
                )
            }
        }
    }

    private fun updateView() {
        with(binding) {
            imageUser.let { photoView ->
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
            welcomeUser.text = String.format(
                getString(R.string.welcome_user),
                user?.displayName
            )

        }
    }

    private fun handleProfileButton() {
        binding.imageUser.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_profile)
            startActivityForResult(
                Intent(context, ProfileActivity::class.java), 0
            )
        }

        binding.uiLayoutNotif.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_notif)
            val intent = Intent(requireContext(), InAppNotificationsActivity::class.java)
            intent.putExtra(Const.NOTIF_COUNT,homePresenter.notifsCount.value)
            startActivityForResult(intent, 0)
        }
    }
    private fun handlePedagogicalContentButton() {
        binding.pedagogicalContent.title.text = getString(R.string.pedagogical_content)
        binding.pedagogicalContent.title.setTextAppearance(context, R.style.left_courant_bold_black)
        val dim = 120
        binding.pedagogicalContent.image.background = resources.getDrawable(R.drawable.home_rounded_white)
        binding.pedagogicalContent.image.layoutParams.width = dim
        binding.pedagogicalContent.image.layoutParams.height = dim
        binding.pedagogicalContent.image.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.pedagogicalContent.root.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_pedago)
            startActivityForResult(
                Intent(
                    requireContext(),
                    PedagoListActivity::class.java
                ), 0
            )
        }
    }

    private fun handleOnClickCounters() {
        with(binding) {
            meetingCard.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_meetcount)
                CustomAlertDialog.showWithoutActions(
                    requireContext(),
                    getString(R.string.create_encounters),
                    getString(R.string.participate_to_events),
                    R.drawable.new_illu_header_group
                )
            }
            groupCard.root.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_groupcount)
                userSummary?.let {
                    ViewPagerDefaultPageController.shouldSelectDiscoverGroups = it.neighborhoodParticipationsCount == 0
                }
                val bottomNavigationView =
                    requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                bottomNavigationView.selectedItemId = R.id.navigation_groups
            }

            eventCard.root.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_eventcount)
                userSummary?.let {
                    ViewPagerDefaultPageController.shouldSelectDiscoverEvents = it.outingParticipationsCount == 0
                }
                val bottomNavigationView =
                    requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                bottomNavigationView.selectedItemId = R.id.navigation_events
            }
        }
    }

    private fun initializeHelpSection() {
        binding.moderator.root.title.text = userSummary?.moderator?.displayName
        binding.moderator.root.description.text = getString(R.string.moderator_subtitle)
        userSummary?.moderator?.imageURL?.let {
            Glide.with(requireContext())
                .load(Uri.parse(it))
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .circleCrop()
                .into(binding.moderator.root.icon_card)
        } ?: kotlin.run {
            binding.moderator.root.icon_card.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.placeholder_user, null)
            )
        }

        binding.moderator.root.setOnClickListener {
            userSummary?.moderator?.id?.let {
                AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_moderator)
                startActivityForResult(
                    Intent(context, UserProfileActivity::class.java).putExtra(
                        Const.USER_ID,
                        it
                    ), 0
                )
            }
        }

        binding.solidarityPlaces.root.title.text = getString(R.string.solidarity_places_map)
        binding.solidarityPlaces.root.description.text = getString(R.string.solidarity_places_map_sub)
        binding.solidarityPlaces.root.icon_card.setImageDrawable(
            ResourcesCompat.getDrawable(resources,R.drawable.new_solidarity_map, null)
        )

        binding.solidarityPlaces.root.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Home_action_map)
            val intent = Intent(requireContext(), GDSMainActivity::class.java)
            startActivityForResult(intent, 0)
        }
    }

    private fun setRecommendationsList(recommendationsList: MutableList<HomeAction>) {
        binding.recommendations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RecommendationsListAdapter(recommendationsList,
                object : OnItemClickListener {
                    override fun onItemClick(recommendation: HomeAction) {
                        AnalyticsEvents.logEvent("Action__Home__HowToStart")
                        if (recommendation.homeType != null && recommendation.action != null && recommendation.params != null) {
                            Navigation.getNavigateIntent(
                                context,
                                parentFragmentManager,
                                recommendation.homeType!!,
                                recommendation.action!!,
                                recommendation.params!!
                            )?.let { intent ->
                                startActivityForResult(intent, 0)
                            }
                        }
                    }
                }, context)
        }
    }

    private fun showCongratDialog() {
        //TODO: disabled for MVP
//        homePresenter.summary.value?.congratulations?.let {
//            if (it.isEmpty()) return
//            HomeCongratPopFragment.newInstance(it as ArrayList<HomeAction>).show(parentFragmentManager, HomeCongratPopFragment.TAG)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}