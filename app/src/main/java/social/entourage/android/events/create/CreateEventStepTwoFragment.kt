package social.entourage.android.events.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateEventStepTwoBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.utils.transformIntoDatePicker
import social.entourage.android.tools.log.AnalyticsEvents
import java.text.SimpleDateFormat
import java.util.*

class CreateEventStepTwoFragment : Fragment() {
    private var _binding: NewFragmentCreateEventStepTwoBinding? = null
    val binding: NewFragmentCreateEventStepTwoBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeDateEditText()
        setView()
        handleNextButtonState()
        setRecurrence()
        adjustTextViewsForRTL(binding.layout.root)
        if (CommunicationHandler.eventEdited == null) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventStepTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initializeDateEditText() {
        binding.layout.eventDate.transformIntoDatePicker(
            requireContext(),
            getString(R.string.events_date),
            minDate = Date()
        )
    }

    private fun adjustTextViewsForRTL(view: View) {
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        if (isRTL) {
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    adjustTextViewsForRTL(child) // Récursion pour parcourir toutes les sous-vues
                }
            } else if (view is TextView) {
                // Ajuster la gravité et la direction du texte pour RTL
                view.gravity = View.TEXT_ALIGNMENT_VIEW_END
                view.textDirection = View.TEXT_DIRECTION_RTL
            }
        }
    }

    private fun handleNextButtonState() {
        handleEditTextChangedTextListener(binding.layout.eventDate)
        handleEditTextChangedTextListener(binding.layout.eventTime.getStartEditText())
        handleEditTextChangedTextListener(binding.layout.eventTime.getEndEditText())
    }

    private fun setRecurrence() {
        binding.layout.recurrence.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.once -> CommunicationHandler.event.recurrence = null
                R.id.every_week -> CommunicationHandler.event.recurrence =
                    Recurrence.EVERY_WEEK.value
                R.id.every_two_week -> CommunicationHandler.event.recurrence =
                    Recurrence.EVERY_TWO_WEEKS.value
            }
        }
    }

    private fun handleEditTextChangedTextListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                CommunicationHandler.isButtonClickable.value =
                    isEndTimeValid() && isStartTimeValid() && isDateValid() && binding.layout.eventTime.checkTimeValidity() == true
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        CommunicationHandler.resetValues()
        CommunicationHandler.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        CommunicationHandler.isButtonClickable.value =
            isDateValid() && isStartTimeValid() && isEndTimeValid() && binding.layout.eventTime.checkTimeValidity() == true
    }

    override fun onDestroy() {
        super.onDestroy()
        if(_binding!=null) {
            binding.layout.error.root.visibility = View.GONE
        }
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isEndTimeValid() && isStartTimeValid() && isDateValid() && binding.layout.eventTime.checkTimeValidity() == true) {
                binding.layout.error.root.visibility = View.GONE
                CommunicationHandler.isCondition.value = true
                CommunicationHandler.clickNext.removeObservers(viewLifecycleOwner)
                val dateFormatterToDate =
                    SimpleDateFormat(getString(R.string.event_date_formatter_to_date))
                val dateFormatterToString =
                    SimpleDateFormat(getString(R.string.event_date_formatter_to_string))
                val startDate =
                    dateFormatterToDate.parse(
                        binding.layout.eventDate.text.toString() + " " +
                                binding.layout.eventTime.getStartTime()

                    )
                val endDate =
                    dateFormatterToDate.parse(
                        binding.layout.eventDate.text.toString() + " " +
                                binding.layout.eventTime.getEndTime()
                    )
                val startDateString = dateFormatterToString.format(startDate)
                val endDateString = dateFormatterToString.format(endDate)

                CommunicationHandler.event.metadata?.startsAt(startDateString)
                CommunicationHandler.event.metadata?.endsAt(endDateString)
            } else {
                binding.layout.error.root.visibility = View.VISIBLE
                binding.layout.error.errorMessage.text =
                    getString(R.string.error_mandatory_fields)
                if (binding.layout.eventTime.checkTimeValidity() == false) binding.layout.error.errorMessage.text =
                    getString(R.string.error_event_end_time)
                CommunicationHandler.isCondition.value = false
            }
        }
    }

    private fun isEndTimeValid(): Boolean {
        return binding.layout.eventTime.isEndDateValid() && binding.layout.eventDate.text.isNotBlank()
    }

    private fun isStartTimeValid(): Boolean {
        return binding.layout.eventTime.isStartTimeValid() && binding.layout.eventDate.text.isNotBlank()
    }

    private fun isDateValid(): Boolean {
        return binding.layout.eventDate.text.isNotEmpty() && binding.layout.eventDate.text.isNotBlank()
    }

    private fun setView() {
        val me = EntourageApplication.me(requireContext())
        if(me?.roles?.isEmpty() == true){
            binding.layout.recurrenceTitle.root.isVisible = false
            binding.layout.recurrence.isVisible = false
        }else{
            binding.layout.recurrenceTitle.root.isVisible = true
            binding.layout.recurrence.isVisible = true
        }
        with(CommunicationHandler.eventEdited) {
            this?.let {
                var locale = LanguageManager.getLocaleFromPreferences(requireContext())
                val sdfDate = SimpleDateFormat(getString(R.string.events_date), locale)
                val sdfTime = SimpleDateFormat(getString(R.string.events_time), locale)
                binding.layout.eventDate.setText(this.metadata?.startsAt?.let { it1 ->
                    sdfDate.format(
                        it1
                    )
                })
                binding.layout.eventTime.setStartTime(this.metadata?.startsAt?.let { it1 ->
                    sdfTime.format(
                        it1
                    )
                })
                binding.layout.eventTime.setEndTime(this.metadata?.endsAt?.let { it1 ->
                    sdfTime.format(
                        it1
                    )
                })
            }
        }
    }
}