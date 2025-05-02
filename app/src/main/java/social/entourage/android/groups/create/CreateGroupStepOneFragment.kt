package social.entourage.android.groups.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.FragmentCreateGroupStepOneBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.user.edit.place.UserEditActionZoneFragment

class CreateGroupStepOneFragment : Fragment(), UserEditActionZoneFragment.FragmentListener {

    private var _binding: FragmentCreateGroupStepOneBinding? = null
    val binding: FragmentCreateGroupStepOneBinding get() = _binding!!

    private val viewModel: CommunicationHandlerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupStepOneBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_NEW_GROUP_STEP1)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetValues()
        handleNextButtonState()
        initializeDescriptionCounter()
        handleChooseLocationGroup()
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isGroupNameValid() && isGroupDescriptionValid() && isGroupLocationValid()) {
                binding.layout.error.root.visibility = View.GONE
                viewModel.isCondition.value = true
                viewModel.group.name(binding.layout.groupName.text.toString())
                viewModel.group.description(binding.layout.groupDescription.text.toString())
                viewModel.clickNext.removeObservers(viewLifecycleOwner)
            } else {
                binding.layout.error.root.visibility = View.VISIBLE
                binding.layout.error.errorMessage.text = getString(R.string.error_mandatory_fields)
                viewModel.isCondition.value = false
            }
        }
    }

    private fun handleNextButtonState() {
        handleEditTextChangedTextListener(binding.layout.groupDescription)
        handleEditTextChangedTextListener(binding.layout.groupName)
    }

    private fun handleEditTextChangedTextListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.isButtonClickable.value = isGroupNameValid() && isGroupDescriptionValid()
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.canExitGroupCreation = canExitGroupCreation()
            }
        })
    }

    private fun initializeDescriptionCounter() {
        binding.layout.counter.text = String.format(
            getString(R.string.description_counter),
            binding.layout.groupDescription.text?.length.toString()
        )
        binding.layout.groupDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.layout.counter.text = String.format(
                    getString(R.string.description_counter),
                    s.length.toString()
                )
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetValues()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        viewModel.isButtonClickable.value = isGroupNameValid() && isGroupDescriptionValid()
        binding.layout.location.text = viewModel.group.displayAddress
    }

    fun isGroupNameValid(): Boolean {
        return binding.layout.groupName.text.length >= Const.GROUP_NAME_MIN_LENGTH && binding.layout.groupName.text.isNotBlank()
    }

    fun isGroupDescriptionValid(): Boolean {
        return binding.layout.groupDescription.text.length >= Const.GROUP_DESCRIPTION_MIN_LENGTH && binding.layout.groupDescription.text.isNotBlank()
    }

    fun isGroupLocationValid(): Boolean {
        return viewModel.group.latitude != 0.0 && viewModel.group.longitude != 0.0
    }

    fun canExitGroupCreation(): Boolean {
        return binding.layout.groupName.text.isEmpty() && binding.layout.groupDescription.text.isEmpty()
    }

    private fun handleChooseLocationGroup() {
        binding.layout.groupLocation.setOnClickListener {
            val action =
                CreateGroupFragmentDirections.actionCreateGroupFragmentToEditActionZoneFragment(true)
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.error.root.visibility = View.GONE
    }

    override fun onUserEditActionZoneFragmentDismiss() {
    }

    override fun onUserEditActionZoneFragmentAddressSaved() {
        findNavController().popBackStack()
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        findNavController().popBackStack()
    }
}