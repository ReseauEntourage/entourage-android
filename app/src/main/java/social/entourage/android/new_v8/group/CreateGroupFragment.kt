package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.databinding.NewFragmentCreateGroupBinding
import social.entourage.android.new_v8.utils.nextPage
import social.entourage.android.new_v8.utils.previousPage

class CreateGroupFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupBinding? = null
    val binding: NewFragmentCreateGroupBinding get() = _binding!!
    private val viewModel: ErrorHandlerViewModel by activityViewModels()
    private lateinit var viewPager: ViewPager2

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
    }

    private fun setNextClickListener() {
        binding.next.setOnClickListener {
            viewModel.onClickNext.value = true
            viewModel.isTextOk.observe(viewLifecycleOwner, ::handleIsTextOk)
        }
    }

    private fun setPreviousClickListener() {
        binding.previous.setOnClickListener {
            viewPager.previousPage(true)
            if (viewPager.currentItem == 0) binding.previous.visibility = View.GONE
        }
    }

    private fun handleIsTextOk(isTextOk: Boolean) {
        if (isTextOk) {
            viewPager.nextPage(true)
            if (viewPager.currentItem > 0) binding.previous.visibility = View.VISIBLE
            viewModel.isTextOk.value = false
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onClickNext.value = false
    }
}