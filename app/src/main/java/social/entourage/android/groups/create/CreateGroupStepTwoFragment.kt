package social.entourage.android.groups.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Interest
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentCreateGroupStepTwoBinding
import social.entourage.android.profile.editProfile.InterestsListAdapter
import social.entourage.android.profile.editProfile.OnItemCheckListener
import social.entourage.android.tools.log.AnalyticsEvents

class CreateGroupStepTwoFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupStepTwoBinding? = null
    val binding: NewFragmentCreateGroupStepTwoBinding get() = _binding!!

    private var interestsList: MutableList<Interest> = mutableListOf()
    private var selectedInterestIdList: MutableList<String> = mutableListOf()
    private val viewModel: CommunicationHandlerViewModel by activityViewModels()

    private lateinit var interestsListAdapter: InterestsListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetValues()
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        initializeInterests()
        binding.layout.egs2Error.root.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepTwoBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_NEW_GROUP_STEP2)
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
        binding.layout.egs2RecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun initializeInterests() {
        interestsListAdapter = InterestsListAdapter(interestsList, object : OnItemCheckListener {
            override fun onItemCheck(item: Interest) {
                item.id?.let {
                    selectedInterestIdList.add(it)
                    viewModel.isButtonClickable.value = interestHaveBeenSelected()
                }
            }

            override fun onItemUncheck(item: Interest) {
                selectedInterestIdList.remove(item.id)
                viewModel.isButtonClickable.value = interestHaveBeenSelected()
            }
        }, true)
        binding.layout.egs2RecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = interestsListAdapter
        }
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            when {
                selectedInterestIdList.isEmpty() -> {
                    binding.layout.egs2Error.root.visibility = View.VISIBLE
                    binding.layout.egs2Error.errorMessage.text =
                        getString(R.string.error_categories_create_group)
                    viewModel.isCondition.value = false
                }
                else -> {
                    binding.layout.egs2Error.root.visibility = View.GONE
                    viewModel.isCondition.value = true
                    viewModel.group.interests(selectedInterestIdList)
                    interestsListAdapter.getOtherInterestCategory()
                        ?.let { if (it.isNotEmpty()) viewModel.group.otherInterest(it) }
                    viewModel.clickNext.removeObservers(viewLifecycleOwner)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetValues()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        viewModel.isButtonClickable.value = interestHaveBeenSelected()
    }

    fun interestHaveBeenSelected(): Boolean {
        return selectedInterestIdList.isNotEmpty()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.egs2Error.root.visibility = View.GONE
    }
}