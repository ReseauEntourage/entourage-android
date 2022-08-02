package social.entourage.android.new_v8.events.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateEventStepTwoBinding
import social.entourage.android.new_v8.utils.transformIntoDatePicker
import social.entourage.android.new_v8.utils.transformIntoTimePicker
import java.util.*


class CreateEventStepTwoFragment : Fragment() {
    private var _binding: NewFragmentCreateEventStepTwoBinding? = null
    val binding: NewFragmentCreateEventStepTwoBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()
        handleNextButtonState()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventStepTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setView() {
        binding.layout.eventDate.transformIntoDatePicker(
            requireContext(),
            getString(R.string.events_date),
            minDate = Date()
        )
        binding.layout.startTime.transformIntoTimePicker(
            requireContext(),
            getString(R.string.events_time)
        )
        binding.layout.endTime.transformIntoTimePicker(
            requireContext(),
            getString(R.string.events_time)
        )
    }


    private fun handleNextButtonState() {
        handleEditTextChangedTextListener(binding.layout.eventDate)
        handleEditTextChangedTextListener(binding.layout.startTime)
        handleEditTextChangedTextListener(binding.layout.endTime)
    }


    private fun handleEditTextChangedTextListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                CommunicationHandler.isButtonClickable.value =
                    isEndTimeValid() && isStartTimeValid() && isDateValid()
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
            isDateValid() && isStartTimeValid() && isEndTimeValid()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.error.root.visibility = View.GONE
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isEndTimeValid() && isStartTimeValid() && isDateValid()) {
                binding.layout.error.root.visibility = View.GONE
                CommunicationHandler.isCondition.value = true
                CommunicationHandler.clickNext.removeObservers(viewLifecycleOwner)
            } else {
                binding.layout.error.root.visibility = View.VISIBLE
                binding.layout.error.errorMessage.text =
                    getString(R.string.error_mandatory_fields)
                CommunicationHandler.isCondition.value = false
            }
        }
    }

    private fun isEndTimeValid(): Boolean {
        return binding.layout.endTime.text.isNotEmpty() && binding.layout.eventDate.text.isNotBlank()
    }

    private fun isStartTimeValid(): Boolean {
        return binding.layout.startTime.text.isNotEmpty() && binding.layout.eventDate.text.isNotBlank()
    }

    private fun isDateValid(): Boolean {
        return binding.layout.eventDate.text.isNotEmpty() && binding.layout.eventDate.text.isNotBlank()
    }
}