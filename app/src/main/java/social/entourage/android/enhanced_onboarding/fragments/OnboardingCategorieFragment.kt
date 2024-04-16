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
            InterestForAdapter(
                icon = getIconForCategory("moment_de_partage"),
                title = getString(R.string.onboarding_category_sharing_time),
                isSelected = false,
                id = "moment_de_partage"
            ),
            InterestForAdapter(
                icon = getIconForCategory("don_materiel"),
                title = getString(R.string.onboarding_category_donation),
                isSelected = false,
                id = "don_materiel"
            ),
            InterestForAdapter(
                icon = getIconForCategory("coup_de_main"),
                title = getString(R.string.onboarding_category_services),
                isSelected = false,
                id = "coup_de_main"
            )
        )
        viewModel.setcategories(categories)
    }

    fun getIconForCategory(id: String): Int {
        return when (id) {
            "moment_de_partage" -> R.drawable.ic_name_moment_de_partage
            "don_materiel" -> R.drawable.ic_name_don_materiel
            "coup_de_main" -> R.drawable.ic_name_coup_de_main
            else -> R.drawable.ic_onboarding_interest_name_autre // Une icône par défaut si aucun id ne correspond
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
        viewModel.updateCategories(interest)
    }
}

