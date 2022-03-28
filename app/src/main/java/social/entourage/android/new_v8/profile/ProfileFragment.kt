package social.entourage.android.new_v8.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: NewFragmentProfileBinding? = null
    val binding: NewFragmentProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = NewFragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTab()
        initializeEditButton()

        Glide.with(requireContext())
            .load(R.drawable.new_profile).circleCrop()
            .into(binding.imageProfile)
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

    private fun initializeEditButton() {
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_fragment_to_edit_profile_fragment)
        }
    }
}