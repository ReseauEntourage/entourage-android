package social.entourage.android.enhanced_onboarding.fragments

import android.content.res.ColorStateList
import android.graphics.Typeface
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
import social.entourage.android.api.model.User
import social.entourage.android.databinding.FragmentEnhancedOnboardingTimeDisponibilityLayoutBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
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
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_configure_later_clic)
            viewModel.registerAndQuit()
        }
        binding.buttonNext.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_disponibility_next_clic)
            if(EnhancedOnboarding.isFromSettingsDisponibility) {
                viewModel.registerAndQuit()
            }else{
                viewModel.setOnboardingFifthStep(true)
            }
        }
        binding.tvTitle.text = getString(R.string.enhanced_onboarding_time_disponibility_title)
        binding.tvDescription.text = getString(R.string.enhanced_onboarding_time_disponibility_description)


    }

    private fun populateAvailability(user: User) {
        // Définir une map pour faire correspondre les jours de la semaine aux IDs de chips
        val dayChipMap = mapOf(
            "1" to binding.chipMonday,
            "2" to binding.chipTuesday,
            "3" to binding.chipWednesday,
            "4" to binding.chipThursday,
            "5" to binding.chipFriday,
            "6" to binding.chipSaturday,
            "7" to binding.chipSunday
        )

        // Définir une map pour faire correspondre les créneaux horaires aux IDs de chips
        
        val timeSlotChipMap = mapOf(
            "09:00-12:00" to binding.chipMorning,
            "14:00-18:00" to binding.chipAfternoon,
            "18:00-21:00" to binding.chipEvening
        )

        // Parcourir les disponibilités de l'utilisateur et cocher les chips correspondants
        user.availability.forEach { (day, timeSlots) ->
            // Cocher le chip correspondant au jour
            dayChipMap[day]?.isChecked = true

            // Cocher les chips correspondants aux créneaux horaires
            timeSlots.forEach { timeSlot ->
                timeSlotChipMap[timeSlot]?.isChecked = true
            }
        }
    }


    private fun setupChipGroups() {
        // Configure les jours de la semaine
        binding.chipGroupDays.setOnCheckedStateChangeListener { group, checkedIds ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as? Chip
                chip?.let {
                    it.setTypeface(null, if (it.isChecked) Typeface.BOLD else Typeface.NORMAL)
                    //it.setChipIconVisible(it.isChecked) // Affiche ou masque l'icône selon l'état du chip
                    it.setChipIconVisible(false)
                }
            }
            // Mettre à jour le ViewModel avec les jours sélectionnés
            val selectedDays = checkedIds.mapNotNull { id ->
                when (id) {
                    R.id.chipMonday -> "Lundi"
                    R.id.chipTuesday -> "Mardi"
                    R.id.chipWednesday -> "Mercredi"
                    R.id.chipThursday -> "Jeudi"
                    R.id.chipFriday -> "Vendredi"
                    R.id.chipSaturday -> "Samedi"
                    R.id.chipSunday -> "Dimanche"
                    else -> null
                }
            }
            viewModel.updateSelectedDays(selectedDays)
        }

        // Configure les tranches horaires
        binding.chipGroupTimeSlots.setOnCheckedStateChangeListener { group, checkedIds ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as? Chip
                chip?.let {
                    it.setTypeface(null, if (it.isChecked) Typeface.BOLD else Typeface.NORMAL)
                    //it.setChipIconVisible(it.isChecked) // Affiche ou masque l'icône selon l'état du chip
                    it.setChipIconVisible(false) // Affiche ou masque l'icône selon l'état du chip
                }
            }
            // Mettre à jour le ViewModel avec les tranches horaires sélectionnées
            val selectedTimeSlots = checkedIds.mapNotNull { id ->
                when (id) {
                    R.id.chipMorning -> "Matin"
                    R.id.chipAfternoon -> "Après-midi"
                    R.id.chipEvening -> "Soir"
                    else -> null
                }
            }
            viewModel.updateSelectedTimeSlots(selectedTimeSlots)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(true)
        if(EnhancedOnboarding.isFromSettingsDisponibility) {
            binding.buttonNext.text = getString(R.string.validate)
            binding.buttonConfigureLater.text = getString(R.string.cancel)
        }else{
            binding.buttonNext.text = getString(R.string.onboarding_btn_next)
        }
        if(viewModel.user != null){
            populateAvailability(viewModel.user!!)
        }
    }
}
