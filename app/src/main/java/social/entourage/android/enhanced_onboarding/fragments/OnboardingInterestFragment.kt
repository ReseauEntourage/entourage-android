package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingInterestsLayoutBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class OnboardingInterestFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingInterestsLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingInterestsLayoutBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        viewModel.interests.observe( viewLifecycleOwner, ::handleInterestLoad)
        AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_interests_view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAndSendInterests()
        binding.buttonConfigureLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_interests_config_later_clic)
            viewModel.registerAndQuit()
        }
        binding.buttonStart.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_interests_next_clic)
            viewModel.setOnboardingFourthStep(true)}
        binding.tvTitle.text = getString(R.string.onboarding_interest_title)
        binding.tvDescription.text = getString(R.string.onboarding_interest_content)
        binding.parentNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == 0) {
                viewModel.toggleBtnBack(true)
            } else {
                viewModel.toggleBtnBack(false)
            }
        })
        if(EnhancedOnboarding.isFromSettingsinterest) {
            binding.buttonStart.text = getString(R.string.onboarding_btn_register)
        }else{
            binding.buttonStart.text = getString(R.string.onboarding_btn_next)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(true)
    }

    private fun setupRecyclerView() {
        binding.rvInterests.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvInterests.isNestedScrollingEnabled = false

    }

    private fun loadAndSendInterests() {
        val user = viewModel.user
        val interests = listOf(
            user?.interests?.contains("sport")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("sport"),
                    title = getString(R.string.interest_sport),
                    isSelected = it,
                    id = "sport",
                    subtitle = ""
                )
            },
            user?.interests?.contains("animaux")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("animaux"),
                    title = getString(R.string.interest_animaux),
                    isSelected = it,
                    id = "animaux",
                    subtitle = ""
                )
            },
            user?.interests?.contains("marauding")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("rencontre"),
                    title = getString(R.string.interest_marauding),
                    isSelected = it,
                    id = "marauding",
                    subtitle = ""
                )
            },
            user?.interests?.contains("bien-etre")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("bien_etre"),
                    title = getString(R.string.interest_bien_etre),
                    isSelected = it,
                    id = "bien-etre",
                    subtitle = ""
                )
            },
            user?.interests?.contains("cuisine")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("cuisine"),
                    title = getString(R.string.interest_cuisine),
                    isSelected = it,
                    id = "cuisine",
                    subtitle = ""
                )
            },
            user?.interests?.contains("culture")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("art"),
                    title = getString(R.string.interest_culture),
                    isSelected = it,
                    id = "culture",
                    subtitle = ""
                )
            },
            user?.interests?.contains("nature")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("nature"),
                    title = getString(R.string.interest_nature),
                    isSelected = it,
                    id = "nature",
                    subtitle = ""
                )
            },
            user?.interests?.contains("jeux")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("jeux"),
                    title = getString(R.string.interest_jeux),
                    isSelected = it,
                    id = "jeux",
                    subtitle = ""
                )
            },
            user?.interests?.contains("activites")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("activite_manuelle"),
                    title = getString(R.string.interest_activites_onboarding),
                    isSelected = it,
                    id = "activites",
                    subtitle = ""
                )
            },
            user?.interests?.contains("other")?.let {
                InterestForAdapter(
                    icon = getIconForInterest("autre"),
                    title = getString(R.string.interest_other),
                    isSelected = it,
                    id = "other",
                    subtitle = ""
                )
            }
        ).filterNotNull() // Filtre pour enlever les éléments nulls si jamais `contains` renvoie null
        viewModel.setInterests(interests)
    }


    fun getIconForInterest(id: String): Int {
        return when (id) {
            "sport" -> R.drawable.ic_onboarding_interest_sport
            "animaux" -> R.drawable.ic_onboarding_interest_name_animaux
            "marauding" -> R.drawable.ic_onboarding_interest_name_rencontre_nomade
            "bien-etre" -> R.drawable.ic_onboarding_interest_name_bien_etre
            "cuisine" -> R.drawable.ic_onboarding_interest_name_cuisine
            "culture" -> R.drawable.ic_onboarding_interest_name_art
            "nature" -> R.drawable.ic_onboarding_interest_name_nature
            "jeux" -> R.drawable.ic_onboarding_interest_name_jeux
            "activites" -> R.drawable.ic_onboarding_interest_name_activite_manuelle
            "other" -> R.drawable.ic_onboarding_interest_name_autre
            else -> R.drawable.ic_onboarding_interest_name_autre
        }
    }

    private fun handleInterestLoad(interests: List<InterestForAdapter>) {
        // Vérifie si l'adapter est déjà défini
        if (binding.rvInterests.adapter == null) {
            binding.rvInterests.adapter = OnboardingInterestsAdapter(requireContext(), true, interests, ::onInterestClicked)
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
        if (interest.isSelected) {
            // Si déjà sélectionné, on retire l'ID
            MainFilterActivity.savedGroupInterestsFromOnboarding.remove(interest.id)
        } else {
            // Sinon, on ajoute l'ID
            MainFilterActivity.savedGroupInterestsFromOnboarding.add(interest.id)
        }
    }
}

