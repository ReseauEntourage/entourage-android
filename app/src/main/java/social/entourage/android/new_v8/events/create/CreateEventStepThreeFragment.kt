package social.entourage.android.new_v8.events.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateEventStepThreeBinding


class CreateEventStepThreeFragment : Fragment() {
    private var _binding: NewFragmentCreateEventStepThreeBinding? = null
    val binding: NewFragmentCreateEventStepThreeBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventStepThreeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()
        handleNextButtonState()
        binding.layout.location.setOnClickListener {
            findNavController().navigate(R.id.action_create_event_fragment_to_edit_action_zone_fragment)
        }
    }

    private fun setView() {
        setPlaceSelection()
        setLimitedPlacesSelection()
    }

    private fun setPlaceSelection() {
        binding.layout.eventType.setOnCheckedChangeListener { _, checkedId ->
            val isEventFaceToFace = checkedId == R.id.face_to_face
            binding.layout.location.isVisible = isEventFaceToFace
            binding.layout.eventUrl.isVisible = !isEventFaceToFace
            binding.layout.eventPlaceTitle.title.text =
                getString(if (isEventFaceToFace) R.string.add_location else R.string.add_url)
            binding.layout.image.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    if (isEventFaceToFace) R.drawable.new_location_event else R.drawable.new_web
                )
            )
        }
    }

    private fun setLimitedPlacesSelection() {
        binding.layout.eventLimitedPlace.setOnCheckedChangeListener { _, checkedId ->
            binding.layout.eventLimitedPlaceCountTitle.root.isVisible = checkedId == R.id.yes
            binding.layout.eventLimitedPlaceCount.isVisible = checkedId == R.id.yes
        }
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isPlaceValid() && isLimitedPlaceValid()) {
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


    override fun onResume() {
        super.onResume()
        CommunicationHandler.resetValues()
        CommunicationHandler.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        CommunicationHandler.isButtonClickable.value =
            isPlaceValid() && isLimitedPlaceValid()
        binding.layout.location.setText(CommunicationHandler.event.displayAddress)
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.layout.error.root.visibility = View.GONE
    }

    private fun handleNextButtonState() {
        handleEditTextChangedTextListener(binding.layout.eventUrl)
        handleEditTextChangedTextListener(binding.layout.eventLimitedPlaceCount)
    }


    private fun handleEditTextChangedTextListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                CommunicationHandler.isButtonClickable.value =
                    isPlaceValid() && isLimitedPlaceValid()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun isPlaceValid(): Boolean {
        return (binding.layout.location.text.isNotEmpty() && binding.layout.location.text.isNotBlank())
                || (binding.layout.eventUrl.text.isNotEmpty() && binding.layout.eventUrl.text.isNotBlank())
    }

    private fun isLimitedPlaceValid(): Boolean {
        return if (binding.layout.eventLimitedPlaceCount.isVisible)
            binding.layout.eventLimitedPlaceCount.text.isNotEmpty() && binding.layout.eventLimitedPlaceCount.text.isNotBlank()
        else true
    }
}