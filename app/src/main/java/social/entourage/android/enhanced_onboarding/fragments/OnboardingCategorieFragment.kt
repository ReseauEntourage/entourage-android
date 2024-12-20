package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingActionWishesLayoutBinding
import social.entourage.android.databinding.FragmentOnboardingInterestsLayoutBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingCategorieFragment: Fragment() {

    private lateinit var binding: FragmentOnboardingActionWishesLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingActionWishesLayoutBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_donations_categories_view)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        viewModel.categories.observe( viewLifecycleOwner, ::handleInterestLoad)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAndSendCategories()
        binding.buttonConfigureLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_donations_categories_config_later_clic)
            viewModel.registerAndQuit()
        }
        binding.buttonStart.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_donations_categories_next_clic)
            if(EnhancedOnboarding.isFromSettingsActionCategorie) {
                viewModel.registerAndQuit()
            }else{
                viewModel.completeDisponibilityStep()
            }
        }
        binding.tvTitle.text = getString(R.string.onboarding_category_title)
        binding.tvDescription.text = getString(R.string.onboarding_category_content)
    }
    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(true)
        if(EnhancedOnboarding.isFromSettingsActionCategorie) {
            binding.buttonStart.text = getString(R.string.validate)
            binding.buttonConfigureLater.text = getString(R.string.cancel)
        }else{
            binding.buttonStart.text = getString(R.string.onboarding_btn_next)
        }
    }

    private fun setupRecyclerView() {
        binding.rvInterests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInterests.isNestedScrollingEnabled = false

    }

    private fun loadAndSendCategories() {
        val user = viewModel.user  // Obtenir les données de l'utilisateur

        val categories = listOf(
            user?.concerns?.contains("sharing_time")?.let {
                InterestForAdapter(
                    icon = getIconForCategory("sharing_time"),
                    title = getString(R.string.onboarding_category_sharing_time),
                    isSelected = it,
                    id = "sharing_time",
                    subtitle = getString(R.string.sharing_time_subtitle)
                )
            },
            user?.concerns?.contains("material_donations")?.let {
                InterestForAdapter(
                    icon = getIconForCategory("material_donations"),
                    title = getString(R.string.onboarding_category_donation),
                    isSelected = it,
                    id = "material_donations",
                    subtitle = ""
                )
            },
            user?.concerns?.contains("services")?.let {
                InterestForAdapter(
                    icon = getIconForCategory("services"),
                    title = getString(R.string.onboarding_category_services),
                    isSelected = it,
                    id = "services",
                    subtitle = getString(R.string.onboarding_category_services_details)
                )
            }
        )
        viewModel.setcategories(categories.filterNotNull())
    }


    fun getIconForCategory(id: String): Int {
        return when (id) {
            "sharing_time" -> R.drawable.ic_name_moment_de_partage
            "material_donations" -> R.drawable.ic_name_don_materiel
            "services" -> R.drawable.ic_name_coup_de_main
            else -> R.drawable.ic_onboarding_interest_name_autre // Une icône par défaut si aucun id ne correspond
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
        viewModel.updateCategories(interest)
    }


}

