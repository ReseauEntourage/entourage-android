package social.entourage.android.new_v8.groups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.new_rules_item.*
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentGroupsBinding
import social.entourage.android.new_v8.groups.create.CreateGroupActivity
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import kotlin.math.abs


class GroupsFragment : Fragment() {
    private var _binding: NewFragmentGroupsBinding? = null
    val binding: NewFragmentGroupsBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_GROUP_SHOW)
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
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                AnalyticsEvents.logEvent(
                    if (position == 0)
                        AnalyticsEvents.ACTION_GROUP_MY_GROUP
                    else
                        AnalyticsEvents.ACTION_GROUP_DISCOVER)
            }}
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        if (RefreshController.shouldRefreshFragment) {
            initializeTab()
            RefreshController.shouldRefreshFragment = false
        }
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
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_PLUS)
            startActivity(
                Intent(context, CreateGroupActivity::class.java)
            )
        }
    }
}