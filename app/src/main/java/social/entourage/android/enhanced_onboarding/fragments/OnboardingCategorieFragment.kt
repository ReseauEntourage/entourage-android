package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.new_lined_edit_text.linearLayout
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingActionWishesLayoutBinding
import social.entourage.android.databinding.FragmentOnboardingInterestsLayoutBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.enhanced_onboarding.OnboardingViewModel

class OnboardingCategorieFragment: Fragment() {

    private lateinit var binding: FragmentOnboardingActionWishesLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingActionWishesLayoutBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        viewModel.categories.observe( viewLifecycleOwner, ::handleInterestLoad)
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
            viewModel.setOnboardingFourthStep(true)}
    }

    private fun setupRecyclerView() {
        binding.rvInterests.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun loadAndSendInterests() {
        val categories = listOf(
            InterestForAdapter(R.drawable.ic_name_moment_de_partage, getString(R.string.onboarding_category_sharing_time), false),
            InterestForAdapter(R.drawable.ic_name_don_materiel, getString(R.string.onboarding_category_donation), false),
            InterestForAdapter(R.drawable.ic_name_coup_de_main, getString(R.string.onboarding_category_services), false),

        )
        viewModel.setcategories(categories)
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
        viewModel.updateCategories(interest)
    }
}

