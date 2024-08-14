package social.entourage.android.events.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateEventStepThreeBinding
import social.entourage.android.tools.log.AnalyticsEvents

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
        setViewMultiSelect()
        handleNextButtonState()
        adjustTextViewsForRTL(binding.layout.root)
        binding.layout.location.setOnClickListener {
            findNavController().navigate(R.id.action_create_event_fragment_to_edit_action_zone_fragment)
        }
        setView()

        if (CommunicationHandler.eventEdited == null) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_3)
        }
    }

    private fun setViewMultiSelect() {
        setPlaceSelection()
        setLimitedPlacesSelection()
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
                    if (isEventFaceToFace) R.drawable.new_location else R.drawable.new_web
                )
            )
            CommunicationHandler.event.online(!isEventFaceToFace)
            if (isEventFaceToFace) {
                binding.layout.eventUrl.text.clear()
            } else {
                binding.layout.location.text.clear()
            }
            CommunicationHandler.isButtonClickable.value =
                isPlaceValid() && isLimitedPlaceValid()
        }
    }

    private fun setLimitedPlacesSelection() {
        binding.layout.eventLimitedPlace.setOnCheckedChangeListener { _, checkedId ->
            val limitedPlaces = checkedId == R.id.yes
            binding.layout.eventLimitedPlaceCountTitle.root.isVisible = limitedPlaces
            binding.layout.eventLimitedPlaceCount.isVisible = limitedPlaces
            if (!limitedPlaces) binding.layout.eventLimitedPlaceCount.text.clear()
            CommunicationHandler.isButtonClickable.value =
                isPlaceValid() && isLimitedPlaceValid()
        }
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isPlaceValid() && isLimitedPlaceValid()) {
                binding.layout.error.root.visibility = View.GONE
                CommunicationHandler.isCondition.value = true
                if (binding.layout.faceToFace.isChecked) CommunicationHandler.event.metadata?.streetAddress(
                    binding.layout.location.text.toString()
                )
                if (binding.layout.online.isChecked) CommunicationHandler.event.eventUrl(binding.layout.eventUrl.text.toString())
                if (binding.layout.yes.isChecked) CommunicationHandler.event.metadata?.placeLimit(
                    binding.layout.eventLimitedPlaceCount.text.toString().toInt()
                )
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
        if (CommunicationHandler.event.metadata?.streetAddress?.isNotEmpty() == true)
            binding.layout.location.setText(CommunicationHandler.event.metadata?.streetAddress)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding?.layout?.error?.root?.visibility = View.GONE
    }

    private fun handleNextButtonState() {
        handleEditTextChangedTextListener(binding.layout.eventUrl)
        handleEditTextChangedTextListener(binding.layout.location)
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
        if (binding.layout.no.isChecked) return true
        return if (binding.layout.yes.isChecked) {
            binding.layout.eventLimitedPlaceCount.text.isNotEmpty() && binding.layout.eventLimitedPlaceCount.text.isNotBlank()
        } else true
    }

    private fun setView() {
        with(CommunicationHandler.eventEdited) {
            this?.let {
                if (this.online == true) {
                    binding.layout.online.isChecked = true
                    binding.layout.eventUrl.setText(this.eventUrl)
                } else {
                    binding.layout.faceToFace.isChecked = true
                    binding.layout.location.setText(this.metadata?.displayAddress)
                }

                if (this.metadata?.placeLimit != null && this.metadata.placeLimit != 0) {
                    binding.layout.yes.isChecked = true
                    binding.layout.eventLimitedPlaceCount.setText(this.metadata.placeLimit.toString())
                } else {
                    binding.layout.no.isChecked = true
                    binding.layout.eventLimitedPlaceCount.visibility = View.GONE
                }
            }
        }
    }
}