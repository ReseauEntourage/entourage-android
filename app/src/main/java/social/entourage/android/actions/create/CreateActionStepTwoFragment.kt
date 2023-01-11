package social.entourage.android.actions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateActionStepTwoBinding
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents

class CreateActionStepTwoFragment : Fragment() {

    private var _binding: NewFragmentCreateActionStepTwoBinding? = null
    val binding: NewFragmentCreateActionStepTwoBinding get() = _binding!!

    private val viewModel: CommunicationActionHandlerViewModel by activityViewModels()

    private lateinit var sectionsListAdapter: CreateActionSectionsListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetValues()
        if (viewModel.sectionsList.value == null) {
            viewModel.initSectionList()
        }
        initializeInterests()
        setupViewWithEdit()
        binding.title.setText(getString(R.string.action_create_cat_choose_category,
            if (viewModel.isDemand) getString(R.string.action_name_demand)
            else getString(R.string.action_name_contrib)))

        if (viewModel.actionEdited == null) {
            if (viewModel.isDemand) {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_demand_2)
            }
            else {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_contrib_2)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateActionStepTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initializeInterests() {
        sectionsListAdapter = CreateActionSectionsListAdapter(viewModel.sectionsList.value!!, viewModel.isDemand, object : OnItemCheckListener {
            override fun onItemCheck(position: Int) {
                if (position == -1) {
                    //TODO: show infos
                    Utils.showToast(requireContext(), getString(R.string.not_implemented))
                    return
                }
                viewModel.sectionsList.value?.forEach { it.isSelected = false }
                viewModel.sectionsList.value?.get(position)?.isSelected = true
                sectionsListAdapter.notifyDataSetChanged()
                viewModel.sectionsList.postValue(viewModel.sectionsList.value)
            }
        } )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionsListAdapter
        }
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            val _t = viewModel.sectionsList.value?.firstOrNull { it.isSelected }

            if (_t == null) {
                binding.error.root.visibility = View.VISIBLE
                binding.error.errorMessage.text = getString(R.string.error_categories_create_group)
                viewModel.isCondition.value = false
            }
            else {
                viewModel.isCondition.value = true
                binding.error.root.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetValues()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        viewModel.isButtonClickable.value = sectionHaveBeenSelected()
    }

    fun sectionHaveBeenSelected(): Boolean {
        val _t = viewModel.sectionsList.value?.firstOrNull { it.isSelected }
        return _t != null
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.error.root.visibility = View.GONE
    }

    private fun setupViewWithEdit() {
        viewModel.actionEdited?.let {
            for ( i in 0 until (viewModel.sectionsList.value?.size ?: 0)) {
                if(viewModel.sectionsList.value?.get(i)?.id == viewModel.actionEdited?.sectionName) {
                    viewModel.sectionsList.value?.get(i)?.isSelected = true
                    break
                }
            }
        }
    }
}