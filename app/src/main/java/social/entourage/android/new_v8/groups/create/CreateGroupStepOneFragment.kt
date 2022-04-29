package social.entourage.android.new_v8.groups.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateGroupStepOneBinding
import social.entourage.android.new_v8.utils.Const


class CreateGroupStepOneFragment : Fragment() {


    private var _binding: NewFragmentCreateGroupStepOneBinding? = null
    val binding: NewFragmentCreateGroupStepOneBinding get() = _binding!!

    private val viewModel: CommunicationHandlerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleNextButtonState()
        initializeDescriptionCounter()
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isGroupNameValid()) {
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
        binding.layout.groupName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.isButtonClickable.value = isGroupNameValid()
            }

            override fun afterTextChanged(s: Editable) {}
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
        viewModel.resetStepOne()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        viewModel.isButtonClickable.value = isGroupNameValid()
    }

    fun isGroupNameValid(): Boolean {
        return binding.layout.groupName.text.length >= Const.GROUP_NAME_MIN_LENGTH
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.error.root.visibility = View.GONE
    }
}