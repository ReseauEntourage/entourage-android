package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingActionWishesLayoutBinding
import social.entourage.android.databinding.FragmentOnboardingInterestsLayoutBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.enhanced_onboarding.OnboardingViewModel

class OnboardingActionWishesFragment:Fragment() {

    private lateinit var binding: FragmentOnboardingActionWishesLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingActionWishesLayoutBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        viewModel.actionsWishes.observe( viewLifecycleOwner, ::handleInterestLoad)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAndSendInterests()
        binding.buttonConfigureLater.setOnClickListener {
            viewModel.registerAndQuit()
        }
        binding.buttonStart.setOnClickListener {
            viewModel.setOnboardingFifthStep(true)}
    }

    private fun setupRecyclerView() {
        binding.rvInterests.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun loadAndSendInterests() {
        val actionWishes = listOf(
            InterestForAdapter(
                icon = getIconForActionWish("sensibilisation"),
                title = getString(R.string.onboarding_action_wish_pedago),
                isSelected = false,
                id = "sensibilisation"
            ),
            InterestForAdapter(
                icon = getIconForActionWish("convivialite"),
                title = getString(R.string.onboarding_action_wish_event),
                isSelected = false,
                id = "convivialite"
            ),
            InterestForAdapter(
                icon = getIconForActionWish("coup_de_pouce"),
                title = getString(R.string.onboarding_action_wish_services),
                isSelected = false,
                id = "coup_de_pouce"
            ),
            // Ajoute d'autres souhaits d'action ici...
            InterestForAdapter(
                icon = getIconForActionWish("discussion"),
                title = getString(R.string.onboarding_action_wish_network),
                isSelected = false,
                id = "discussion"
            )
        )
        viewModel.setActionsWishes(actionWishes)
    }

    fun getIconForActionWish(id: String): Int {
        return when (id) {
            "sensibilisation" -> R.drawable.ic_onboarding_action_wish_sensibilisation
            "convivialite" -> R.drawable.ic_onboarding_action_wish_convivialite
            "coup_de_pouce" -> R.drawable.ic_onboarding_action_wish_coup_de_pouce
            "discussion" -> R.drawable.ic_onboarding_action_wish_discussion
            else -> R.drawable.ic_onboarding_interest_name_autre
        }
    }


    private fun handleInterestLoad(interests: List<InterestForAdapter>) {
        // Vérifie si l'adapter est déjà défini
        if (binding.rvInterests.adapter == null) {
            binding.rvInterests.adapter = OnboardingInterestsAdapter(requireContext(), interests, ::onInterestClicked)
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







