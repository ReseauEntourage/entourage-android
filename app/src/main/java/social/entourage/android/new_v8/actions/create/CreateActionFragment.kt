package social.entourage.android.new_v8.actions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateActionBinding
import social.entourage.android.new_v8.RefreshController
import social.entourage.android.new_v8.actions.ActionsPresenter
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.nextPage
import social.entourage.android.new_v8.utils.previousPage
import social.entourage.android.tools.log.AnalyticsEvents

class CreateActionFragment : Fragment() {

    private var _binding: NewFragmentCreateActionBinding? = null
    val binding: NewFragmentCreateActionBinding get() = _binding!!
    private val viewModel: CommunicationActionHandlerViewModel by activityViewModels()
    private var viewPager: ViewPager2? = null
    private val actionPresenter: ActionsPresenter by lazy { ActionsPresenter() }

    private var isDemand = false
    private var actionEdited:Action? = null
    private var isAlreadySend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isDemand = CreateActionFragmentArgs.fromBundle(it).isActionDemand
            actionEdited = CreateActionFragmentArgs.fromBundle(it).actionObj
            viewModel.isDemand = isDemand
            viewModel.actionEdited = actionEdited
        }

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                // Show your dialog and handle navigation
                    onBackButton()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.actionEdited != null) {
            binding.uiHeaderTitle.text = if (isDemand) getString(R.string.action_edit_demand_title) else getString(R.string.action_edit_contrib_title)
        }
        else {
            binding.uiHeaderTitle.text = if (isDemand) getString(R.string.action_create_demand_title) else getString(R.string.action_create_contrib_title)
        }


        val currentItemPager = viewPager?.currentItem
        initializeViewPager(currentItemPager)


        binding.iconBack.setOnClickListener {
            onBackButton()
        }

        if ((viewPager?.currentItem ?: 0) > 0) binding.previous.visibility = View.VISIBLE

        //Listener for choose item
        viewModel.sectionsList.observe(viewLifecycleOwner, Observer { list ->
            val _t = viewModel.sectionsList.value?.firstOrNull { it.isSelected }
            handleButtonState(_t != null)
        })

        actionPresenter.isActionUpdated.observe(viewLifecycleOwner, ::isActionUpdated)
        actionPresenter.newActionCreated.observe(viewLifecycleOwner, ::handleCreateActionResponse)
    }

    private fun handleCreateActionResponse(actionCreated: Action?) {
        if (actionCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.action_error_create_action, if (isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib)))
        } else {
            actionPresenter.newActionCreated.value?.let {
                if (actionEdited == null) {
                    if (isDemand) {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_demand_end)
                    }
                    else {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_contrib_end)
                    }
                }
                val action =
                    CreateActionFragmentDirections.actionCreateActionFragmentToCreateActionSuccessFragment(
                        it.id!!,it.title!!,it.isDemand()
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun isActionUpdated(updated: Boolean) {
        if (updated) {
            Utils.showToast(requireContext(), getString(R.string.action_updated))
            activity?.finish()
            RefreshController.shouldRefreshEventFragment = true
        } else {
            isAlreadySend = false
            Utils.showToast(
                requireContext(),
                getString(
                    R.string.action_error_edit_action,
                    if (isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib)
                )
            )
        }
    }

    private fun initializeViewPager(currentPos:Int?) {
        viewPager = binding.viewPager
        val adapter = CreateActionViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager?.adapter = adapter
        TabLayoutMediator(binding.tabLayout, viewPager!!) { tab: TabLayout.Tab, _: Int ->
            tab.view.isClickable = false
        }.attach()
        viewPager?.currentItem = currentPos ?: 0

        binding.next.text = getString(if (currentPos == NB_TABS - 1) R.string.create else R.string.new_next)

        setNextClickListener()
        setPreviousClickListener()
        handleNextButtonState()
    }

    private fun setNextClickListener() {
        handleValidate()
        viewModel.isCondition.observe(
            viewLifecycleOwner,
            ::handleIsCondition
        )
        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.next.text =
                    getString(if (position == NB_TABS - 1) {
                        if (viewModel.actionEdited != null) {
                            R.string.edit
                        } else R.string.create
                    } else R.string.new_next)
            }
        })
    }

    private fun handleNextButtonState() {
        viewModel.isButtonClickable.observe(
            viewLifecycleOwner,
            ::handleButtonState
        )
    }

    private fun handleButtonState(isButtonActive: Boolean) {
        val background = ContextCompat.getDrawable(
            requireContext(),
            if (isButtonActive) R.drawable.new_rounded_button_light_orange else R.drawable.new_bg_rounded_inactive_button_light_orange
        )
        binding.next.background = background
    }

    private fun setPreviousClickListener() {
        binding.previous.setOnClickListener {
            viewModel.resetValues()
            viewPager?.previousPage(true)
            if (viewPager?.currentItem == 0) {
                binding.previous.visibility = View.INVISIBLE
            }
        }
    }

    private fun handleIsCondition(isCondition: Boolean) {

        if (isCondition) {
            if (viewPager?.currentItem == NB_TABS - 1) {
                if (viewModel.actionEdited != null) {
                    if (isAlreadySend) return
                    isAlreadySend = true
                    updateAction()
                    return
                }

                if (viewModel.isButtonClickable.value == true) {
                    if (isAlreadySend) return
                    isAlreadySend = true
                    viewModel.prepareCreateAction()
                    actionPresenter.createAction(viewModel.action, isDemand, viewModel.imageURI, requireContext())
                }
                else {
                    viewModel.clickNext.value = true
                }

            } else {
                viewPager?.nextPage(true)
                if ((viewPager?.currentItem ?: 0) > 0) binding.previous.visibility = View.VISIBLE
                viewModel.resetValues()
            }
        }
    }

    private fun updateAction() {
        viewModel.prepareUpdateAction()
        actionPresenter.updateAction(viewModel.actionEdited, viewModel.action, isDemand,viewModel.imageURI,requireContext())
    }

    private fun onBackButton() {
        if (viewModel.canExitActionCreation)
            requireActivity().finish()
        else {
            Utils.showAlertDialogButtonClicked(
                requireContext(),
                getString(R.string.back_create_action_title),
                getString(R.string.back_create_action_content,if (isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib)),
                getString(R.string.exit), {}
            ) {
                requireActivity().finish()
            }
        }
    }

    private fun handleValidate() {
        binding.next.setOnClickListener {
            if (binding.viewPager.currentItem == NB_TABS - 1) {
                viewModel.isCondition.value = true
            }
            else {
                viewModel.clickNext.value = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetValues()
    }
}