package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.FragmentOnboardingActionWishesLayoutBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingActionWishesFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingActionWishesLayoutBinding
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var adapter: OnboardingInterestsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingActionWishesLayoutBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_actions_view)

        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        viewModel.actionsWishes.observe(viewLifecycleOwner, ::handleInterestLoad)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.View__OnboardingActionWishesFragment)
        setupRecyclerView()
        loadAndSendActionWishes()

        binding.buttonConfigureLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_actions_config_later_clic)
            viewModel.registerAndQuit()
        }

        binding.buttonStart.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_actions_next_clic)
            if (EnhancedOnboarding.isFromSettingsWishes) {
                viewModel.registerAndQuit()
            } else {
                viewModel.setOnboardingThirdStep(true)
            }
        }

        if (isAssociationMode()) {
            binding.tvTitle.text = getString(R.string.enhanced_onboarding_asso_wishes_title)
            binding.tvDescription.text = getString(R.string.enhanced_onboarding_asso_wishes_description)
        } else {
            binding.tvTitle.text = getString(R.string.onboarding_action_wish_title)
            binding.tvDescription.text = getString(R.string.onboarding_action_wish_content)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(true)

        if (EnhancedOnboarding.isFromSettingsWishes) {
            binding.buttonStart.text = getString(R.string.validate)
            binding.buttonConfigureLater.text = getString(R.string.cancel)
        } else {
            binding.buttonStart.text = getString(R.string.onboarding_btn_next)
        }

        loadAndSendActionWishes()
    }

    private fun isAssociationMode(): Boolean {
        if (EnhancedOnboarding.isAssociationFromSummary == true) return true
        val goal = viewModel.user?.goal
        return goal != null && goal.equals(User.USER_GOAL_ASSO, ignoreCase = true)
    }

    private fun setupRecyclerView() {
        adapter = OnboardingInterestsAdapter(
            isFromInterest = false,
            onInterestClicked = ::onInterestClicked
        )

        binding.rvInterests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@OnboardingActionWishesFragment.adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadAndSendActionWishes() {
        val user = viewModel.user ?: run {
            viewModel.setActionsWishes(emptyList())
            return
        }

        // On récupère les listes existantes pour éviter les null check répétitifs
        val userOrientations = user.orientations ?: emptyList()
        val userInvolvements = user.involvements ?: emptyList()

        val actionWishes = if (isAssociationMode()) {
            // --- MODE ASSOCIATION ---
            // On vérifie la présence des clés dans 'userOrientations'
            buildList {
                add(
                    InterestForAdapter(
                        icon = getIconForActionWish("resources"),
                        title = getString(R.string.enhanced_onboarding_asso_wish_outings), // Relayer événements
                        isSelected = userOrientations.contains("share"),
                        id = "share",
                        subtitle = ""
                    )
                )

                add(
                    InterestForAdapter(
                        icon = getIconForActionWish("outings"),
                        title = getString(R.string.enhanced_onboarding_asso_wish_neighborhoods), // Orienter bénéficiaires
                        isSelected = userOrientations.contains("guide"),
                        id = "guide",
                        subtitle = ""
                    )
                )

                add(
                    InterestForAdapter(
                        icon = getIconForActionWish("actions"),
                        title = getString(R.string.enhanced_onboarding_asso_wish_both_actions), // Coup de pouce
                        // Le JSON indique l'ID "help", on vérifie donc "help".
                        // On garde "both_actions" en fallback au cas où l'API renverrait l'ancien format.
                        isSelected = userOrientations.contains("help") || userOrientations.contains("both_actions"),
                        id = "help",
                        subtitle = ""
                    )
                )
            }
        } else {
            // --- MODE PARTICULIER ---
            // On vérifie la présence des clés dans 'userInvolvements'
            buildList {
                if (EnhancedOnboarding.preference == "contribution") {
                    // Ordre spécifique : Contribution
                    add(InterestForAdapter(
                        icon = getIconForActionWish("outings"),
                        title = getString(R.string.onboarding_action_wish_event_contrib),
                        isSelected = userInvolvements.contains("outings"),
                        id = "outings", subtitle = ""
                    ))
                    add(InterestForAdapter(
                        icon = getIconForActionWish("actions"),
                        title = getString(R.string.onboarding_action_wish_services_contrib),
                        isSelected = userInvolvements.contains("both_actions"),
                        id = "both_actions", subtitle = ""
                    ))
                    add(InterestForAdapter(
                        icon = getIconForActionWish("neighborhoods"),
                        title = getString(R.string.onboarding_action_wish_network_contrib),
                        isSelected = userInvolvements.contains("neighborhoods"),
                        id = "neighborhoods", subtitle = ""
                    ))
                    add(InterestForAdapter(
                        icon = R.drawable.ic_onboarding_interest_name_rencontre_nomade,
                        title = getString(R.string.onboarding_action_wish_pedago_contrib),
                        isSelected = userInvolvements.contains("resources"),
                        id = "resources", subtitle = ""
                    ))
                } else {
                    // Ordre spécifique : Standard
                    add(InterestForAdapter(
                        icon = getIconForActionWish("resources"),
                        title = getString(R.string.onboarding_action_wish_pedago),
                        isSelected = userInvolvements.contains("resources"),
                        id = "resources", subtitle = ""
                    ))
                    add(InterestForAdapter(
                        icon = getIconForActionWish("outings"),
                        title = getString(R.string.onboarding_action_wish_event),
                        isSelected = userInvolvements.contains("outings"),
                        id = "outings", subtitle = ""
                    ))
                    add(InterestForAdapter(
                        icon = getIconForActionWish("actions"),
                        title = getString(R.string.onboarding_action_wish_services),
                        isSelected = userInvolvements.contains("both_actions"),
                        id = "both_actions", subtitle = ""
                    ))
                    add(InterestForAdapter(
                        icon = getIconForActionWish("neighborhoods"),
                        title = getString(R.string.onboarding_action_wish_network),
                        isSelected = userInvolvements.contains("neighborhoods"),
                        id = "neighborhoods", subtitle = ""
                    ))
                }
            }
        }

        viewModel.setActionsWishes(actionWishes)
    }

    private fun getIconForActionWish(id: String): Int {
        return when (id) {
            "resources" -> R.drawable.ic_onboarding_action_wish_sensibilisation
            "outings" -> R.drawable.ic_onboarding_action_wish_convivialite
            "actions" -> R.drawable.ic_onboarding_action_wish_coup_de_pouce
            "neighborhoods" -> R.drawable.ic_onboarding_action_wish_discussion
            else -> R.drawable.ic_onboarding_interest_name_autre
        }
    }

    private fun handleInterestLoad(interests: List<InterestForAdapter>) {
        adapter.submitList(interests)
    }

    private fun onInterestClicked(interest: InterestForAdapter) {
        viewModel.updateActionsWishes(interest)
    }
}
