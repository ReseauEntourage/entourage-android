package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentCreateGroupStepTwoBinding
import social.entourage.android.new_v8.profile.editProfile.InterestsListAdapter
import social.entourage.android.new_v8.profile.editProfile.InterestsTypes
import social.entourage.android.new_v8.profile.editProfile.OnItemCheckListener
import social.entourage.android.new_v8.profile.models.Interest
import social.entourage.android.new_v8.utils.Const


class CreateGroupStepTwoFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupStepTwoBinding? = null
    val binding: NewFragmentCreateGroupStepTwoBinding get() = _binding!!

    private var interestsList: MutableList<Interest> = mutableListOf()
    private var selectedInterestIdList: MutableList<String> = mutableListOf()
    private val viewModel: ErrorHandlerViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        initializeInterests()
        viewModel.clickNext.value = false
        viewModel.isButtonClickable.value = false
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        tags?.interests?.forEach { interest ->
            interestsList.add(
                Interest(
                    interest.id,
                    interest.name,
                    false
                )
            )
        }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun initializeInterests() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = InterestsListAdapter(interestsList, object : OnItemCheckListener {
                override fun onItemCheck(item: Interest) {
                    item.id?.let {
                        selectedInterestIdList.add(it)
                        viewModel.isButtonClickable.value =
                            !selectedInterestIdList.contains(InterestsTypes.TYPE_OTHER.label)
                    }
                }

                override fun onItemUncheck(item: Interest) {
                    selectedInterestIdList.remove(item.id)
                    viewModel.isButtonClickable.value =
                        !(selectedInterestIdList.isEmpty() || selectedInterestIdList.contains(
                            InterestsTypes.TYPE_OTHER.label
                        ))
                }
            })
        }
    }


    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (selectedInterestIdList.isEmpty()) {
                binding.error.root.visibility = View.VISIBLE
                binding.error.errorMessage.text = getString(R.string.error_categories_create_group)
            } else {
                binding.error.root.visibility = View.GONE
                viewModel.isCondition.value = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isCondition.value = false
        viewModel.isButtonClickable.value = false
        viewModel.clickNext.value = false
        binding.error.root.visibility = View.GONE
    }
}