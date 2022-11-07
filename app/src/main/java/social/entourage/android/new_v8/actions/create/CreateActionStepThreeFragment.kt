package social.entourage.android.new_v8.actions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateActionStepThreeBinding
import social.entourage.android.new_v8.models.MetadataActionLocation
import social.entourage.android.user.edit.place.UserEditActionZoneFragment


class CreateActionStepThreeFragment : Fragment(), UserEditActionZoneFragment.FragmentListener {

    private var _binding: NewFragmentCreateActionStepThreeBinding? = null
    val binding: NewFragmentCreateActionStepThreeBinding get() = _binding!!

    private val viewModel: CommunicationActionHandlerViewModel by activityViewModels()

    private var isFirstLaunch = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateActionStepThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetValues()
        handleChooseLocationGroup()

        if (viewModel.metadata.value == null) {
            viewModel.metadata.value = MetadataActionLocation()
        }

        viewModel.metadata.observe(viewLifecycleOwner, Observer { _meta ->
            _meta?.let {
                if (it.streetAddress?.isEmpty() == true) {
                    binding.location.setText("")
                }
                else {
                    binding.location.setText(it.streetAddress)
                }
            }
            viewModel.isButtonClickable.value = viewModel.metadata.value?.streetAddress?.isNotBlank()
        })
        binding.title.text = getString(R.string.action_create_location_title, if (viewModel.isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib))
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isPlaceValid()) {
                binding.error.root.visibility = View.GONE
                viewModel.isCondition.value = true
                viewModel.clickNext.removeObservers(viewLifecycleOwner)
            } else {
                binding.error.root.visibility = View.VISIBLE
                binding.error.errorMessage.text = getString(R.string.error_mandatory_fields)
                viewModel.isCondition.value = false
            }
        }
    }

    private fun isPlaceValid(): Boolean {
        return (binding.location.text.isNotEmpty() && binding.location.text.isNotBlank())
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetValues()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)

        if (isFirstLaunch) {
            isFirstLaunch = false
            setupViewWithEdit()
        }
        else {
            binding.location.text = viewModel.metadata.value?.streetAddress
            viewModel.isButtonClickable.value = viewModel.metadata.value?.streetAddress?.isNotBlank()
        }
    }

    private fun handleChooseLocationGroup() {
        binding.groupLocation.setOnClickListener {
            val action =
                CreateActionFragmentDirections.actionCreateActionFragmentToEditActionPlaceFragment(false)
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.error.root.visibility = View.GONE
    }

    override fun onUserEditActionZoneFragmentDismiss() {
    }

    override fun onUserEditActionZoneFragmentAddressSaved() {
        findNavController().popBackStack()
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        findNavController().popBackStack()
    }

    private fun setupViewWithEdit() {
        viewModel.actionEdited?.let {
            binding.location.setText(it.metadata?.displayAddress)
            viewModel.isButtonClickable.value = true
        } ?: kotlin.run { viewModel.isButtonClickable.value = viewModel.metadata.value?.streetAddress?.isNotBlank() }
    }
}