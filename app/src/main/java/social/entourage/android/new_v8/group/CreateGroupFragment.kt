package social.entourage.android.new_v8.group

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
import social.entourage.android.api.request.GroupWrapper
import social.entourage.android.databinding.NewFragmentCreateGroupBinding
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.nextPage
import social.entourage.android.new_v8.utils.previousPage
import timber.log.Timber

class CreateGroupFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupBinding? = null
    val binding: NewFragmentCreateGroupBinding get() = _binding!!
    private val viewModel: CommunicationHandlerViewModel by activityViewModels()
    private lateinit var viewPager: ViewPager2
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewPager()
        handleBackButton()
        handleValidate()
        groupPresenter.isGroupCreated.observe(viewLifecycleOwner, ::handleCreateGroupResponse)
    }

    private fun handleCreateGroupResponse(isGroupCreated: Boolean) {
        if (isGroupCreated) {
            findNavController().navigate(R.id.action_create_group_fragment_to_create_group_success_fragment)
        } else {
            Utils.showToast(requireContext(), getString(R.string.error_create_group))
        }
    }

    private fun initializeViewPager() {
        viewPager = binding.viewPager
        val adapter = CreateGroupAdapter(childFragmentManager, lifecycle)
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
                if (position == 0) {
                    binding.next.text = getString(R.string.new_next)
                }
                if (position == 1) {
                    binding.next.text = getString(R.string.new_next)
                }
                if (position == 2) {
                    binding.next.text = getString(R.string.create)
                }
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
            viewPager.previousPage(true)
            if (viewPager.currentItem == 0) binding.previous.visibility = View.INVISIBLE
        }
    }

    private fun handleIsCondition(isCondition: Boolean) {
        if (isCondition) {
            if (viewPager.currentItem == 2) {
                viewModel.group.latitude(2.5)
                viewModel.group.longitude(2.5)
                Timber.e(viewModel.group.toString())
                groupPresenter.createGroup(viewModel.group)
            } else {
                viewPager.nextPage(true)
                if (viewPager.currentItem > 0) binding.previous.visibility = View.VISIBLE
            }
        }
    }


    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            Utils.showAlertDialogButtonClicked(
                requireView(),
                getString(R.string.back_create_group_title),
                getString(R.string.back_create_group_content),
                getString(R.string.exit)
            ) { findNavController().popBackStack() }
        }
    }


    private fun handleValidate() {
        if (binding.viewPager.currentItem == 2)
            binding.next.setOnClickListener {
                findNavController().navigate(R.id.action_create_group_fragment_to_create_group_success_fragment)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetStepOne()
    }
}