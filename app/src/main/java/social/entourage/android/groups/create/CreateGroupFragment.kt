package social.entourage.android.groups.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.NewFragmentCreateGroupBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.nextPage
import social.entourage.android.tools.utils.previousPage

class CreateGroupFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupBinding? = null
    val binding: NewFragmentCreateGroupBinding get() = _binding!!
    private val viewModel: CommunicationHandlerViewModel by activityViewModels()
    private lateinit var viewPager: ViewPager2
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    private var isAlreadySend = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupBinding.inflate(inflater, container, false)

        updatePaddingTopForEdgeToEdge(binding.header.layout)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewPager()
        handleBackButton()
        handleValidate()
        groupPresenter.newGroupCreated.observe(viewLifecycleOwner, ::handleCreateGroupResponse)
    }

    private fun handleCreateGroupResponse(groupCreated: Group?) {
        if (groupCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.error_create_group))
        } else {
            groupPresenter.newGroupCreated.value?.id?.let {
                CreatePostGroupActivity.idGroupForPost = it
                val action =
                    CreateGroupFragmentDirections.actionCreateGroupFragmentToCreateGroupSuccessFragment(
                        it
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun initializeViewPager() {
        viewPager = binding.viewPager
        val adapter = CreateGroupViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, viewPager) { tab: TabLayout.Tab, _: Int ->
            tab.view.isClickable = false
        }.attach()
        setNextClickListener()
        setPreviousClickListener()
        handleNextButtonState()
    }

    private fun setNextClickListener() {
        binding.next.setOnClickListener {
            viewModel.clickNext.value = true
        }
        viewModel.isCondition.observe(
            viewLifecycleOwner,
            ::handleIsCondition
        )
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.next.text =
                    getString(if (position == NB_TABS - 1) R.string.create else R.string.new_next)
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
        AnalyticsEvents.logEvent(
            AnalyticsEvents.ACTION_NEW_GROUP_NEXT
        )
        binding.next.background = background
    }

    private fun setPreviousClickListener() {
        binding.previous.setOnClickListener {
            viewModel.resetValues()
            viewPager.previousPage(true)
            if (viewPager.currentItem == 0) {
                binding.previous.visibility = View.INVISIBLE
            } else {
                AnalyticsEvents.logEvent(
                    AnalyticsEvents.ACTION_NEW_GROUP_PREVIOUS
                )
            }
        }
    }

    private fun handleIsCondition(isCondition: Boolean) {
        if (isCondition) {
            if (viewPager.currentItem == NB_TABS - 1) {
                if (isAlreadySend) return
                isAlreadySend = true
                groupPresenter.createGroup(viewModel.group)
            } else {
                viewPager.nextPage(true)
                if (viewPager.currentItem > 0) binding.previous.visibility = View.VISIBLE
                viewModel.resetValues()
            }
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_NEW_GROUP_BACK_ARROW
            )
            if (viewModel.canExitGroupCreation)
                requireActivity().finish()
            else {
                AnalyticsEvents.logEvent(
                    AnalyticsEvents.VIEW_NEW_GROUP_CANCEL_POP
                )
                CustomAlertDialog.showWithCancelFirst(
                    requireContext(),
                    getString(R.string.back_create_group_title),
                    getString(R.string.back_create_group_content),
                    getString(R.string.exit),
                    {
                        AnalyticsEvents.logEvent(
                            AnalyticsEvents.ACTION_NEW_GROUP_CANCEL_POP_CANCEL
                        )
                    }
                ) {
                    AnalyticsEvents.logEvent(
                        AnalyticsEvents.ACTION_NEW_GROUP_CANCEL_POP_LEAVE
                    )
                    requireActivity().finish()
                }
            }

        }
    }

    private fun handleValidate() {
        if (binding.viewPager.currentItem == NB_TABS - 1)
            binding.next.setOnClickListener {
                findNavController().navigate(R.id.action_create_group_fragment_to_create_group_success_fragment)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetValues()
    }
}