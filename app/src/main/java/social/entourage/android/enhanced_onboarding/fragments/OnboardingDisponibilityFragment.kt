package social.entourage.android.enhanced_onboarding.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import social.entourage.android.R
import social.entourage.android.databinding.FragmentEnhancedOnboardingTimeDisponibilityLayoutBinding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class OnboardingDisponibilityFragment : Fragment() {

    private lateinit var binding: FragmentEnhancedOnboardingTimeDisponibilityLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEnhancedOnboardingTimeDisponibilityLayoutBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_view)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChipGroups()
        binding.buttonConfigureLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_configure_later_click)
            viewModel.registerAndQuit()
        }
        binding.buttonNext.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_next_click)
            viewModel.setOnboardingFifthStep(true)
        }
        binding.tvTitle.text = getString(R.string.enhanced_onboarding_time_disponibility_title)
        binding.tvDescription.text = getString(R.string.enhanced_onboarding_time_disponibility_description)
    }

    private fun setupChipGroups() {
        // Configure les jours de la semaine
        binding.chipGroupDays.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedDays = mutableListOf<String>()
            checkedIds.forEach { id ->
                when (id) {
                    R.id.chipMonday -> selectedDays.add("Lundi")
                    R.id.chipTuesday -> selectedDays.add("Mardi")
                    R.id.chipWednesday -> selectedDays.add("Mercredi")
                    R.id.chipThursday -> selectedDays.add("Jeudi")
                    R.id.chipFriday -> selectedDays.add("Vendredi")
                    R.id.chipSaturday -> selectedDays.add("Samedi")
                    R.id.chipSunday -> selectedDays.add("Dimanche")
                }
            }
            viewModel.updateSelectedDays(selectedDays)
        }

        // Configure les tranches horaires
        binding.chipGroupTimeSlots.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedTimeSlots = mutableListOf<String>()
            checkedIds.forEach { id ->
                when (id) {
                    R.id.chipMorning -> selectedTimeSlots.add("Matin")
                    R.id.chipAfternoon -> selectedTimeSlots.add("Après-midi")
                    R.id.chipEvening -> selectedTimeSlots.add("Soir")
                }
            }
            viewModel.updateSelectedTimeSlots(selectedTimeSlots)
        }
    }



    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(true)
    }
}
