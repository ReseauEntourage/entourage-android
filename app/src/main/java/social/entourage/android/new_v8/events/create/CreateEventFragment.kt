package social.entourage.android.new_v8.events.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateEventBinding
import social.entourage.android.new_v8.utils.nextPage
import social.entourage.android.new_v8.utils.previousPage


class CreateEventFragment : Fragment() {

    private var _binding: NewFragmentCreateEventBinding? = null
    val binding: NewFragmentCreateEventBinding get() = _binding!!

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewPager()
    }


    private fun initializeViewPager() {
        viewPager = binding.viewPager
        val adapter = CreateEventViewPagerAdapter(childFragmentManager, lifecycle)
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
            CommunicationHandler.clickNext.value = true
        }
        CommunicationHandler.isCondition.observe(
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


    private fun handleIsCondition(isCondition: Boolean) {
        if (isCondition) {
            if (viewPager.currentItem == NB_TABS - 1) {
                // Create event here
            } else {
                viewPager.nextPage(true)
                if (viewPager.currentItem > 0) binding.previous.visibility = View.VISIBLE
                CommunicationHandler.resetValues()
            }
        }
    }

    private fun handleNextButtonState() {
        CommunicationHandler.isButtonClickable.observe(
            viewLifecycleOwner,
            ::handleButtonState
        )
    }


    private fun setPreviousClickListener() {
        binding.previous.setOnClickListener {
            CommunicationHandler.resetValues()
            viewPager.previousPage(true)
            if (viewPager.currentItem == 0) {
                binding.previous.visibility = View.INVISIBLE
            }
        }
    }

    private fun handleButtonState(isButtonActive: Boolean) {
        val background = ContextCompat.getDrawable(
            requireContext(),
            if (isButtonActive) R.drawable.new_rounded_button_light_orange else R.drawable.new_bg_rounded_inactive_button_light_orange
        )
        binding.next.background = background
    }
}