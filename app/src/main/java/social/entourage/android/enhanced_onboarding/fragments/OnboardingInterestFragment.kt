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
            InterestForAdapter(R.drawable.ic_onboarding_interest_sport, getString(R.string.interest_sport), false),
            InterestForAdapter(R.drawable.ic_onboarding_interest_name_bien_etre, getString(R.string.interest_bien_etre), false),
            InterestForAdapter(R.drawable.ic_onboarding_interest_name_cuisine, getString(R.string.interest_cuisine), false),
            InterestForAdapter(R.drawable.ic_onboarding_interest_name_art, getString(R.string.interest_culture), false),
            InterestForAdapter(R.drawable.ic_onboarding_interest_name_nature, getString(R.string.interest_nature), false),
            InterestForAdapter(R.drawable.ic_onboarding_interest_name_jeux, getString(R.string.interest_jeux), false),
            InterestForAdapter(R.drawable.ic_onboarding_interest_name_activite_manuelle, getString(R.string.interest_activites), false),
            InterestForAdapter(R.drawable.ic_onboarding_interest_name_autre, getString(R.string.interest_other), false)
        )
        viewModel.setInterests(interests)
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

