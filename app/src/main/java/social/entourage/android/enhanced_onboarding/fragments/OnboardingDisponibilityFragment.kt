package social.entourage.android.enhanced_onboarding.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.FragmentEnhancedOnboardingTimeDisponibilityLayoutBinding
import social.entourage.android.databinding.ItemAvailabilityDayRowBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingDisponibilityFragment : Fragment() {

    private lateinit var binding: FragmentEnhancedOnboardingTimeDisponibilityLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    private val availabilityState = mutableMapOf<String, MutableSet<String>>()

    private val days = listOf(
        "1" to "Lun", "2" to "Mar", "3" to "Mer",
        "4" to "Jeu", "5" to "Ven", "6" to "Sam", "7" to "Dim"
    )

    private val timeSlotKeys = listOf("09:00-12:00", "14:00-18:00", "18:00-21:00")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEnhancedOnboardingTimeDisponibilityLayoutBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_view)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buildGrid()

        binding.buttonConfigureLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_configure_later_clic)
            viewModel.registerAndQuit()
        }
        binding.buttonNext.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_next_clic)
            if (EnhancedOnboarding.isFromSettingsDisponibility) {
                viewModel.registerAndQuit()
            } else {
                viewModel.setOnboardingFifthStep(true)
            }
        }
    }

    private fun buildGrid() {
        binding.availabilityGridContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for ((dayKey, dayLabel) in days) {
            val rowBinding = ItemAvailabilityDayRowBinding.inflate(inflater, binding.availabilityGridContainer, false)
            rowBinding.tvDayLabel.text = dayLabel

            val chips = listOf(rowBinding.chipMorning, rowBinding.chipAfternoon, rowBinding.chipEvening)
            chips.forEachIndexed { index, chip ->
                val slot = timeSlotKeys[index]
                chip.isChecked = availabilityState[dayKey]?.contains(slot) == true
                updateChipStyle(chip)
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        availabilityState.getOrPut(dayKey) { mutableSetOf() }.add(slot)
                    } else {
                        availabilityState[dayKey]?.remove(slot)
                    }
                    updateChipStyle(chip)
                    updateDayLabelStyle(rowBinding.tvDayLabel, dayKey)
                    pushAvailabilityToViewModel()
                }
            }

            updateDayLabelStyle(rowBinding.tvDayLabel, dayKey)
            binding.availabilityGridContainer.addView(rowBinding.root)
        }
    }

    private fun updateChipStyle(chip: Chip) {
        chip.setTypeface(null, if (chip.isChecked) Typeface.BOLD else Typeface.NORMAL)
        chip.setChipIconVisible(false)
    }

    private fun updateDayLabelStyle(label: android.widget.TextView, dayKey: String) {
        val hasSlots = availabilityState[dayKey]?.isNotEmpty() == true
        label.setTypeface(null, if (hasSlots) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun pushAvailabilityToViewModel() {
        val availability = availabilityState
            .filter { it.value.isNotEmpty() }
            .mapValues { it.value.toList() }
        viewModel.updateAvailability(availability)
    }

    private fun populateAvailability(user: User) {
        availabilityState.clear()
        user.availability.forEach { (day, slots) ->
            availabilityState[day] = slots.toMutableSet()
        }
        buildGrid()
        pushAvailabilityToViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(true)
        if (EnhancedOnboarding.isFromSettingsDisponibility) {
            binding.buttonNext.text = getString(R.string.validate)
            binding.buttonConfigureLater.text = getString(R.string.cancel)
        } else {
            binding.buttonNext.text = getString(R.string.onboarding_btn_next)
        }
        if (viewModel.user != null) {
            populateAvailability(viewModel.user!!)
        }
    }
}
