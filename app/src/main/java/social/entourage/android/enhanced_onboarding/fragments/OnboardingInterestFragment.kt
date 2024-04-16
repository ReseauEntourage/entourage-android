package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingInterestsLayoutBinding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.enhanced_onboarding.InterestForAdapter

class OnboardingInterestFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingInterestsLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingInterestsLayoutBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        viewModel.interests.observe( viewLifecycleOwner, ::handleInterestLoad)
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
            viewModel.setOnboardingThirdStep(true)}
    }

    private fun setupRecyclerView() {
        binding.rvInterests.layoutManager = GridLayoutManager(requireContext(), 2)

    }

    private fun loadAndSendInterests() {
        val interests = listOf(
            InterestForAdapter(
                icon = getIconForInterest("sport"),
                title = getString(R.string.interest_sport),
                isSelected = false,
                id = "sport"
            ),
            InterestForAdapter(
                icon = getIconForInterest("bien_etre"),
                title = getString(R.string.interest_bien_etre),
                isSelected = false,
                id = "bien_etre"
            ),
            InterestForAdapter(
                icon = getIconForInterest("cuisine"),
                title = getString(R.string.interest_cuisine),
                isSelected = false,
                id = "cuisine"
            ),
            InterestForAdapter(
                icon = getIconForInterest("art"),
                title = getString(R.string.interest_culture),
                isSelected = false,
                id = "art"
            ),
            InterestForAdapter(
                icon = getIconForInterest("nature"),
                title = getString(R.string.interest_nature),
                isSelected = false,
                id = "nature"
            ),
            InterestForAdapter(
                icon = getIconForInterest("jeux"),
                title = getString(R.string.interest_jeux),
                isSelected = false,
                id = "jeux"
            ),
            InterestForAdapter(
                icon = getIconForInterest("activite_manuelle"),
                title = getString(R.string.interest_activites),
                isSelected = false,
                id = "activite_manuelle"
            ),
            InterestForAdapter(
                icon = getIconForInterest("autre"),
                title = getString(R.string.interest_other),
                isSelected = false,
                id = "autre"
            )
        )
        viewModel.setInterests(interests)
    }



    fun getIconForInterest(id: String): Int {
        return when (id) {
            "sport" -> R.drawable.ic_onboarding_interest_sport
            "bien_etre" -> R.drawable.ic_onboarding_interest_name_bien_etre
            "cuisine" -> R.drawable.ic_onboarding_interest_name_cuisine
            "art" -> R.drawable.ic_onboarding_interest_name_art
            "nature" -> R.drawable.ic_onboarding_interest_name_nature
            "jeux" -> R.drawable.ic_onboarding_interest_name_jeux
            "activite_manuelle" -> R.drawable.ic_onboarding_interest_name_activite_manuelle
            "autre" -> R.drawable.ic_onboarding_interest_name_autre
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
        viewModel.updateInterest(interest)
    }
}

