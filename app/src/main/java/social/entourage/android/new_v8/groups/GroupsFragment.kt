package social.entourage.android.new_v8.groups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentGroupsBinding
import social.entourage.android.new_v8.groups.create.CreateGroupActivity
import kotlin.math.abs


class GroupsFragment : Fragment() {
    private var _binding: NewFragmentGroupsBinding? = null
    val binding: NewFragmentGroupsBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createGroup()
        initializeTab()
        handleImageViewAnimation()
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = GroupsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.my_groups),
            requireContext().getString(R.string.discover_groups)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.img.alpha = 1f - res
        })
    }

    private fun createGroup() {
        binding.createGroup.setOnClickListener {
            startActivity(
                Intent(context, CreateGroupActivity::class.java)
            )
        }
    }
}