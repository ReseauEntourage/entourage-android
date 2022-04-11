package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import social.entourage.android.databinding.NewFragmentCreateGroupStepOneBinding


class CreateGroupStepOneFragment : Fragment() {


    private var _binding: NewFragmentCreateGroupStepOneBinding? = null
    val binding: NewFragmentCreateGroupStepOneBinding get() = _binding!!

    private val viewModel: ErrorHandlerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onClickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (binding.groupName.text.isEmpty()) {
                binding.error.root.visibility = View.VISIBLE
            } else {
                binding.error.root.visibility = View.GONE
                viewModel.isTextOk.value = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isTextOk.value = false
        binding.error.root.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }
}