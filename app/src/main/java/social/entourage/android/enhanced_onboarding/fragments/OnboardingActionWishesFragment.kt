package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingActionWishesLayoutBinding
import social.entourage.android.databinding.FragmentOnboardingInterestsLayoutBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class OnboardingActionWishesFragment:Fragment() {

    private lateinit var binding: FragmentOnboardingActionWishesLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingActionWishesLayoutBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_actions_view)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        viewModel.actionsWishes.observe( viewLifecycleOwner, ::handleInterestLoad)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAndSendActionWishes()
        binding.buttonConfigureLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_actions_config_later_clic)
        viewModel.registerAndQuit()
        }
        binding.buttonStart.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_actions_next_clic)
            if(EnhancedOnboarding.isFromSettingsWishes) {
                viewModel.registerAndQuit()
            }else{
                viewModel.setOnboardingThirdStep(true)
            }
        }
        binding.tvTitle.text = getString(R.string.onboarding_action_wish_title)
        binding.tvDescription.text = getString(R.string.onboarding_action_wish_content)
    }

    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(true)
        if(EnhancedOnboarding.isFromSettingsWishes) {
            binding.buttonStart.text = getString(R.string.validate)
            binding.buttonConfigureLater.text = getString(R.string.cancel)
        }else{
            binding.buttonStart.text = getString(R.string.onboarding_btn_next)
        }
        loadAndSendActionWishes()
    }

    private fun setupRecyclerView() {
        binding.rvInterests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInterests.isNestedScrollingEnabled = false


    }

    private fun loadAndSendActionWishes() {
        val user = viewModel.user  // Obtenir les données de l'utilisateur
        if(EnhancedOnboarding.preference == "contribution") {
            val actionWishes = listOf(

                user?.involvements?.contains("outings")?.let {
                    InterestForAdapter(
                        icon = getIconForActionWish("outings"),
                        title = getString(R.string.onboarding_action_wish_event_contrib),
                        isSelected = it,
                        id = "outings",
                        subtitle = ""
                    )
                },
                user?.involvements?.contains("both_actions")?.let {
                    InterestForAdapter(
                        icon = getIconForActionWish("actions"),
                        title = getString(R.string.onboarding_action_wish_services_contrib),
                        isSelected = it,
                        id = "both_actions",
                        subtitle = ""
                    )
                },
                user?.involvements?.contains("neighborhoods")?.let {
                    InterestForAdapter(
                        icon = getIconForActionWish("neighborhoods"),
                        title = getString(R.string.onboarding_action_wish_network_contrib),
                        isSelected = it,
                        id = "neighborhoods",
                        subtitle = ""
                    )
                },user?.involvements?.contains("resources")?.let {
                    InterestForAdapter(
                        icon = R.drawable.ic_onboarding_interest_name_rencontre_nomade,
                        title = getString(R.string.onboarding_action_wish_pedago_contrib),
                        isSelected = it,
                        id = "pois",
                        subtitle = ""
                    )
                },
            ).filterNotNull() // Filtre pour enlever les éléments nulls si jamais `contains` renvoie null
            viewModel.setActionsWishes(actionWishes)
        }else{
            val actionWishes = listOf(
                user?.involvements?.contains("resources")?.let {
                    InterestForAdapter(
                        icon = getIconForActionWish("resources"),
                        title = getString(R.string.onboarding_action_wish_pedago),
                        isSelected = it,
                        id = "resources",
                        subtitle = ""
                    )
                },
                user?.involvements?.contains("outings")?.let {
                    InterestForAdapter(
                        icon = getIconForActionWish("outings"),
                        title = getString(R.string.onboarding_action_wish_event),
                        isSelected = it,
                        id = "outings",
                        subtitle = ""
                    )
                },
                user?.involvements?.contains("both_actions")?.let {
                    InterestForAdapter(
                        icon = getIconForActionWish("actions"),
                        title = getString(R.string.onboarding_action_wish_services),
                        isSelected = it,
                        id = "both_actions",
                        subtitle = ""
                    )
                },
                user?.involvements?.contains("neighborhoods")?.let {
                    InterestForAdapter(
                        icon = getIconForActionWish("neighborhoods"),
                        title = getString(R.string.onboarding_action_wish_network),
                        isSelected = it,
                        id = "neighborhoods",
                        subtitle = ""
                    )
                }
            ).filterNotNull() // Filtre pour enlever les éléments nulls si jamais `contains` renvoie null
            viewModel.setActionsWishes(actionWishes)
        }

    }


    fun getIconForActionWish(id: String): Int {
        return when (id) {
            "resources" -> R.drawable.ic_onboarding_action_wish_sensibilisation
            "outings" -> R.drawable.ic_onboarding_action_wish_convivialite
            "actions" -> R.drawable.ic_onboarding_action_wish_coup_de_pouce
            "neighborhoods" -> R.drawable.ic_onboarding_action_wish_discussion
            else -> R.drawable.ic_onboarding_interest_name_autre
        }
    }


    private fun handleInterestLoad(interests: List<InterestForAdapter>) {
        // Vérifie si l'adapter est déjà défini
        if (binding.rvInterests.adapter == null) {
            binding.rvInterests.adapter = OnboardingInterestsAdapter(requireContext(), false, interests, ::onInterestClicked)
        } else {
            // Si l'adapter existe déjà, mets à jour simplement les données
            (binding.rvInterests.adapter as? OnboardingInterestsAdapter)?.let { adapter ->
                adapter.interests = interests
                adapter.notifyDataSetChanged()
            }
        }
    }
    private fun onInterestClicked(interest: InterestForAdapter) {
        viewModel.updateActionsWishes(interest)
    }

}







