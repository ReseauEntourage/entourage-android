package social.entourage.android.events.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentCreateEventStepTwoBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.log.AnalyticsEvents
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Étape 2 : date, heures de début et de fin, récurrence. */
class CreateEventStepTwoFragment : Fragment() {

    private var _binding: FragmentCreateEventStepTwoBinding? = null
    val binding: FragmentCreateEventStepTwoBinding get() = _binding!!

    private val viewModel: CreateEventViewModel by activityViewModels()

    private val locale get() = LanguageManager.getLocaleFromPreferences(requireContext())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventStepTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderDateAndTimes()
        setRecurrence()
        handlePickers()
        observeViewModel()
        if (!viewModel.isEdition) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_2)
        }
    }

    // --- Affichage ------------------------------------------------------------------------

    private fun renderDateAndTimes() {
        val form = viewModel.form
        binding.eventDate.text = CreateEventForm.parseDate(form.date)?.let { formatDate(it) }
        binding.startTime.text = CreateEventForm.parseTime(form.startTime)?.let { formatTime(it) }
        binding.endTime.text = CreateEventForm.parseTime(form.endTime)?.let { formatTime(it) }
    }

    private fun formatDate(date: LocalDate): String =
        DateTimeFormatter.ofPattern(getString(R.string.events_date), locale).format(date)

    private fun formatTime(time: LocalTime): String =
        DateTimeFormatter.ofPattern(getString(R.string.events_time), locale).format(time)

    // --- Sélecteurs -----------------------------------------------------------------------

    private fun handlePickers() {
        binding.eventDate.setOnClickListener { pickDate() }
        binding.startTime.setOnClickListener { pickTime(isStart = true) }
        binding.endTime.setOnClickListener { pickTime(isStart = false) }
    }

    private fun pickDate() {
        val current = CreateEventForm.parseDate(viewModel.form.date) ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                viewModel.form.date = LocalDate.of(year, month + 1, day).toString()
                renderDateAndTimes()
                viewModel.onFormChanged()
            },
            current.year, current.monthValue - 1, current.dayOfMonth
        ).run {
            datePicker.minDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            show()
        }
    }

    /**
     * Ouvre le sélecteur d'heure. Sans valeur saisie, il propose une durée par défaut de
     * [DEFAULT_DURATION_HOURS] heures autour de l'autre heure, comme avant la refonte.
     */
    private fun pickTime(isStart: Boolean) {
        val form = viewModel.form
        val own = CreateEventForm.parseTime(if (isStart) form.startTime else form.endTime)
        val other = CreateEventForm.parseTime(if (isStart) form.endTime else form.startTime)
        val initial = own ?: other?.plusHours(if (isStart) -DEFAULT_DURATION_HOURS else DEFAULT_DURATION_HOURS)
            ?: LocalTime.now()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                val value = LocalTime.of(hour, minute).toString()
                if (isStart) form.startTime = value else form.endTime = value
                renderDateAndTimes()
                viewModel.onFormChanged()
            },
            initial.hour, initial.minute, true
        ).show()
    }

    // --- Récurrence -----------------------------------------------------------------------

    private fun setRecurrence() {
        // La récurrence reste réservée aux utilisateurs autorisés (ceux qui ont un rôle).
        val me = EntourageApplication.me(requireContext())
        val allowed = me?.roles?.isEmpty() != true
        binding.recurrenceTitle.isVisible = allowed
        binding.recurrence.isVisible = allowed

        binding.recurrence.check(
            when (viewModel.form.recurrence) {
                Recurrence.EVERY_WEEK.value -> R.id.every_week
                Recurrence.EVERY_TWO_WEEKS.value -> R.id.every_two_week
                else -> R.id.once
            }
        )
        binding.recurrence.setOnCheckedChangeListener { _, checkedId ->
            viewModel.form.recurrence = when (checkedId) {
                R.id.every_week -> Recurrence.EVERY_WEEK.value
                R.id.every_two_week -> Recurrence.EVERY_TWO_WEEKS.value
                else -> Recurrence.NO_RECURRENCE.value
            }
            viewModel.onFormChanged()
        }
    }

    // --- Erreurs --------------------------------------------------------------------------

    private fun observeViewModel() {
        viewModel.errors.observe(viewLifecycleOwner) { errors ->
            val date = errors[CreateEventField.DATE]
            binding.eventDateError.bindError(date)
            binding.eventDate.bindErrorState(date != null)

            val start = errors[CreateEventField.START_TIME]
            val end = errors[CreateEventField.END_TIME]
            binding.eventTimeError.bindError(firstErrorOf(errors, CreateEventField.START_TIME, CreateEventField.END_TIME))
            binding.startTime.bindErrorState(start != null)
            binding.endTime.bindErrorState(end != null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val DEFAULT_DURATION_HOURS = 3L
    }
}
