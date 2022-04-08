package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentCreateGroupBinding
import social.entourage.android.new_v8.utils.nextPage
import social.entourage.android.new_v8.utils.previousPage

class CreateGroupFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupBinding? = null
    val binding: NewFragmentCreateGroupBinding get() = _binding!!

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
    }

    private fun initializeViewPager() {
        val viewPager = binding.viewPager
        val adapter = CreateGroupAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        binding.next.setOnClickListener {
            viewPager.nextPage(true)
        }
        binding.previous.setOnClickListener {
            viewPager.previousPage(true)

        }
    }
}