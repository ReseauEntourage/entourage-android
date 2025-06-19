package social.entourage.android.actions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.FragmentActionCreateStepFourLayoutBinding
import social.entourage.android.databinding.FragmentCreateActionStepCategoryBinding
import social.entourage.android.groups.GroupPresenter
import timber.log.Timber

class CreateActionStepFourFragment : Fragment() {

    private lateinit var binding:FragmentActionCreateStepFourLayoutBinding
    private val viewModel: CommunicationActionHandlerViewModel by activityViewModels()
    private var wantToShareInGroup:Boolean = false
    private lateinit var groupPresenter: GroupPresenter
    private val actionCreateViewModel: CommunicationActionHandlerViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupPresenter = ViewModelProvider(requireActivity()).get(GroupPresenter::class.java)
        groupPresenter.getGroup.observe(viewLifecycleOwner, { group ->
            handleResponseGetGroups(group)
        })
        groupPresenter.getDefaultGroup()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActionCreateStepFourLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initButtons()


    }

    fun initButtons(){
        val quicksandBold = ResourcesCompat.getFont(requireContext(), R.font.quicksand_bold)
        val nunitoSansRegular = ResourcesCompat.getFont(requireContext(), R.font.nunitosans_regular)
        binding.btnYes.setOnClickListener {
            wantToShareInGroup = true
            binding.yesImg.setImageResource(R.drawable.radio_button_image_action_creation_full)
            binding.noImg.setImageResource(R.drawable.radio_button_image_action_creation_empty)
            binding.yesButton.typeface = quicksandBold
            binding.noButton.typeface = nunitoSansRegular
            viewModel.autoPostAtCreate = wantToShareInGroup
            viewModel.isButtonClickable.postValue(true)
        }
        binding.btnNo.setOnClickListener {
            wantToShareInGroup = false
            binding.yesImg.setImageResource(R.drawable.radio_button_image_action_creation_empty)
            binding.noImg.setImageResource(R.drawable.radio_button_image_action_creation_full)
            binding.yesButton.typeface = nunitoSansRegular
            binding.noButton.typeface = quicksandBold
            viewModel.autoPostAtCreate = wantToShareInGroup
            viewModel.isButtonClickable.postValue(true)
        }
    }

    private fun handleResponseGetGroups(groups:Group) {
        if (groups == null ) {
            return
        }
        groups.let {
            if(actionCreateViewModel.isDemand){
                binding.title.text = String.format(getString(R.string.share_request_group),getString(R.string.action_name_demand), it.name)
                binding.infos.text = String.format(getString(R.string.share_request_description),getString(R.string.action_name_demand))
            }else{
                binding.title.text = String.format(getString(R.string.share_request_group),getString(R.string.action_name_contrib), it.name)
                binding.infos.text = String.format(getString(R.string.share_request_description),getString(R.string.action_name_contrib))
            }
        }
    }
}