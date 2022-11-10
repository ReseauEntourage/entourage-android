package social.entourage.android.new_v8.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.new_home_card.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentHomeBinding
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.new_v8.Navigation
import social.entourage.android.new_v8.ViewPagerDefaultPageController
import social.entourage.android.new_v8.home.pedago.PedagoListActivity
import social.entourage.android.new_v8.models.*
import social.entourage.android.new_v8.profile.ProfileActivity
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.tools.view.CommunicationRecoWebUrlHandlerViewModel

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

        val _model = ViewModelProvider(requireActivity()).get(CommunicationRecoWebUrlHandlerViewModel::class.java)
        _model.isValid.observe(requireActivity(), ::reloadDatasFromRecos)

        updateView()
        handleProfileButton()
        handlePedagogicalContentButton()
    }

    override fun onResume() {
        super.onResume()
        if (!isAlreadyLoadSummary) {
            reloadDatasFromRecos(true)
        }
        else {
            isAlreadyLoadSummary = false
        }
    }

    private fun reloadDatasFromRecos(isOk:Boolean) {
        homePresenter.getSummary()
    }

    private fun updateContributionsView(summary: Summary) {
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
            startActivity(
                Intent(context, ProfileActivity::class.java)
            )
        }
    }

    private fun handlePedagogicalContentButton() {
        binding.pedagogicalContent.title.text = getString(R.string.pedagogical_content)
        binding.pedagogicalContent.root.elevation = 0F
        binding.pedagogicalContent.root.background = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.new_bg_rounded_shadow_orange_opacity_50
        )

        binding.pedagogicalContent.root.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    PedagoListActivity::class.java
                )
            )
        }
    }

    private fun handleOnClickCounters() {
        with(binding) {
            meetingCard.setOnClickListener {
                Utils.showAlertDialogWithoutActions(
                    requireContext(),
                    getString(R.string.create_encounters),
                    getString(R.string.participate_to_events),
                    R.drawable.new_illu_header_group
                )
            }
            groupCard.root.setOnClickListener {
                userSummary?.let {
                    if (it.neighborhoodParticipationsCount == 0)
                        ViewPagerDefaultPageController.shouldSelectDiscoverGroups = true
                }
                val bottomNavigationView =
                    requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                bottomNavigationView.selectedItemId = R.id.navigation_groups
            }

            eventCard.root.setOnClickListener {
                userSummary?.let {
                    if (it.outingParticipationsCount == 0)
                        ViewPagerDefaultPageController.shouldSelectDiscoverEvents = true
                }
                val bottomNavigationView =
                    requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                bottomNavigationView.selectedItemId = R.id.navigation_events
            }
        }
    }

    private fun initializeHelpSection() {
        with(binding) {
            moderator.root.title.text = userSummary?.moderator?.displayName
            moderator.root.description.text = getString(R.string.moderator_subtitle)

            userSummary?.moderator?.imageURL?.let {
                Glide.with(requireContext())
                    .load(Uri.parse(it))
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(moderator.root.icon_card)
            } ?: kotlin.run {
                Glide.with(requireContext())
                    .load(R.drawable.placeholder_user)
                    .into(moderator.root.icon_card)
            }

            moderator.root.setOnClickListener {
                userSummary?.moderator?.id?.let {
                    requireContext().startActivity(
                        Intent(context, UserProfileActivity::class.java).putExtra(
                            Const.USER_ID,
                            it
                        )
                    )
                }
            }

            solidarityPlaces.root.title.text = getString(R.string.solidarity_places_map)
            solidarityPlaces.root.description.visibility = View.GONE
            Glide.with(requireContext())
                .load(R.drawable.new_solidarity_map)
                .into(solidarityPlaces.root.icon_card)

            solidarityPlaces.root.setOnClickListener {
                val intent = Intent(requireContext(), GDSMainActivity::class.java)
                requireActivity().startActivity(intent)
            }
        }
    }

    private fun setRecommendationsList(recommendationsList: MutableList<HomeAction>) {
        binding.recommendations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RecommendationsListAdapter(recommendationsList,
                object : OnItemClickListener {
                    override fun onItemClick(recommendation: HomeAction) {
                        if (recommendation.homeType != null && recommendation.action != null && recommendation.params != null) {
                            Navigation.navigate(
                                context,
                                parentFragmentManager,
                                recommendation.homeType!!,
                                recommendation.action!!,
                                recommendation.params!!
                            )
                        }
                    }
                })
        }
    }

    private fun showCongratDialog() {
        homePresenter.summary.value?.congratulations?.let {
            if (it.isEmpty()) return
            HomeCongratPopFragment.newInstance(it as ArrayList<HomeAction>).show(parentFragmentManager, HomeCongratPopFragment.TAG)
        }
    }
}