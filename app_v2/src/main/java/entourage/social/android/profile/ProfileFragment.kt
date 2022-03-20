package entourage.social.android.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import entourage.social.android.R
import entourage.social.android.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    val binding: FragmentProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTab()
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.my_profile),
            requireContext().getString(R.string.settings)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }
}