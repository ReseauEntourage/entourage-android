package social.entourage.android.new_v8.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import social.entourage.android.new_v8.models.Recommandation
import social.entourage.android.new_v8.models.Summary
import social.entourage.android.new_v8.profile.ProfileActivity
import social.entourage.android.new_v8.utils.Utils

class HomeFragment : Fragment() {
    private var _binding: NewFragmentHomeBinding? = null
    val binding: NewFragmentHomeBinding get() = _binding!!
    private val homePresenter: HomePresenter by lazy { HomePresenter() }

    private var user: User? = null
    private var userSummary: Summary? = null

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
        homePresenter.getSummary()
        homePresenter.summary.observe(requireActivity(), ::updateContributionsView)
        updateView()
        handleProfileButton()
        handlePedagogicalContentButton()
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
            }

            summary.neighborhoodParticipationsCount?.let {
                groupCard.value.text = it.toString()
                groupCard.isEmpty.isVisible = it <= 0
                groupCard.isNotEmpty.isVisible = it > 0
            }
        }
        summary.recommendations?.let { setRecommendationsList(it) }
        initializeHelpSection()
        handleOnClickCounters()
    }

    private fun updateView() {
        with(binding) {
            imageUser.let { photoView ->
                user?.avatarURL?.let { avatarURL ->
                    Glide.with(requireActivity())
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(photoView)
                } ?: run {
                    photoView.setImageResource(R.drawable.ic_user_photo_small)
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
            //To be deleted
            moderator.root.title.text = "Barbara, modératrice"
            moderator.root.description.text = getString(R.string.moderator_subtitle)
            Glide.with(requireActivity())
                //To be changed
                .load("")
                .placeholder(R.drawable.perso_violet)
                .circleCrop()
                .into(moderator.root.icon_card)
            solidarityPlaces.root.title.text = getString(R.string.solidarity_places_map)
            solidarityPlaces.root.description.visibility = View.GONE
            Glide.with(requireActivity())
                //To be changed
                .load("")
                .placeholder(R.drawable.new_solidarity_map)
                .circleCrop()
                .into(solidarityPlaces.root.icon_card)

            solidarityPlaces.root.setOnClickListener {
                val intent = Intent(requireContext(), GDSMainActivity::class.java)
                requireActivity().startActivity(intent)
            }
        }
    }

    private fun setRecommendationsList(recommendationsList: MutableList<Recommandation>) {
        binding.recommendations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                RecommendationsListAdapter(recommendationsList, object : OnItemClickListener {
                    override fun onItemClick(recommendation: Recommandation) {
                        if (recommendation.type != null && recommendation.action != null && recommendation.params != null)
                            Navigation.navigate(
                                context,
                                parentFragmentManager,
                                recommendation.type,
                                recommendation.action,
                                recommendation.params
                            )
                    }
                })
        }
    }
}