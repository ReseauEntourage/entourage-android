package social.entourage.android.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.new_home_card.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentHomeBinding
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.Navigation
import social.entourage.android.ViewPagerDefaultPageController
import social.entourage.android.home.notifications.NotificationsInAppActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.api.model.HomeAction
import social.entourage.android.api.model.Summary
import social.entourage.android.api.model.User
import social.entourage.android.profile.ProfileActivity
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.CommunicationRecoWebUrlHandlerViewModel
import timber.log.Timber

class HomeFragment : Fragment() {
    private var _binding: NewFragmentHomeBinding? = null
    val binding: NewFragmentHomeBinding get() = _binding!!
    private val homePresenter: HomePresenter by lazy { HomePresenter() }

    private var user: User? = null
    private var userSummary: Summary? = null

    private var isAlreadyLoadSummary = false

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
        homePresenter.summary.observe(requireActivity(), ::updateContributionsView)
        isAlreadyLoadSummary = true
        homePresenter.getSummary()

        val viewModel = ViewModelProvider(requireActivity()).get(
            CommunicationRecoWebUrlHandlerViewModel::class.java)
        viewModel.isValid.observe(requireActivity(), ::reloadDatasFromRecos)

        updateView()
        handleProfileButton()
        handlePedagogicalContentButton()

        homePresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        homePresenter.getUnreadCount()
        homePresenter.notifsCount.observe(requireActivity(), ::updateNotifsCount)
    }

    override fun onResume() {
        super.onResume()
        reloadDatasFromRecos(true)
        homePresenter.getNotificationsCount()
        AnalyticsEvents.logEvent(AnalyticsEvents.Home_view_home)
    }

    private fun updateNotifsCount(count:Int) {
        context?.resources?.let { resources->
            binding.uiBellNotif.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (count > 0) R.drawable.ic_new_notif_on else R.drawable.ic_new_notif_off,
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
            val intent = Intent(requireContext(),NotificationsInAppActivity::class.java)
            intent.putExtra(Const.NOTIF_COUNT,homePresenter.notifsCount.value)
            startActivityForResult(intent, 0)
        }
    }

    private fun handlePedagogicalContentButton() {
        binding.pedagogicalContent.title.text = getString(R.string.pedagogical_content)
        binding.pedagogicalContent.title.setTextAppearance(context, R.style.left_courant_bold_black)
        binding.pedagogicalContent.root.elevation = 0F
        binding.pedagogicalContent.image.scaleType = ImageView.ScaleType.CENTER_CROP
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
                })
        }
    }

    private fun showCongratDialog() {
        //TODO: disabled for MVP
//        homePresenter.summary.value?.congratulations?.let {
//            if (it.isEmpty()) return
//            HomeCongratPopFragment.newInstance(it as ArrayList<HomeAction>).show(parentFragmentManager, HomeCongratPopFragment.TAG)
//        }
    }
}