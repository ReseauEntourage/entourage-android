package social.entourage.android.new_v8.group

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


class CreateGroupStepOneFragment : Fragment() {


    private var _binding: NewFragmentCreateGroupStepOneBinding? = null
    val binding: NewFragmentCreateGroupStepOneBinding get() = _binding!!

    private val viewModel: ErrorHandlerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        handleNextButtonState()
        initializeDescriptionCounter()
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (binding.groupName.text.length < 2) {
                binding.error.root.visibility = View.VISIBLE
                binding.error.errorMessage.text = getString(R.string.error_mandatory_fields)
            } else {
                binding.error.root.visibility = View.GONE
                viewModel.isCondition.value = true
            }
        }
    }

    private fun handleNextButtonState() {
        binding.groupName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.isButtonClickable.value = s.length >= 2
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun initializeDescriptionCounter() {
        binding.counter.text = String.format(
            getString(R.string.description_counter),
            binding.groupDescription.text?.length.toString()
        )
        binding.groupDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.counter.text = String.format(
                    getString(R.string.description_counter),
                    s.length.toString()
                )
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.isCondition.value = false
        viewModel.isButtonClickable.value = false
        viewModel.clickNext.value = false
        binding.error.root.visibility = View.GONE
    }

}