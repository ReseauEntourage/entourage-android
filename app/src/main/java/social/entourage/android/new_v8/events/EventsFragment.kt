package social.entourage.android.new_v8.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentEventsBinding
import social.entourage.android.new_v8.events.create.CreateEventActivity
import social.entourage.android.new_v8.events.list.EventsViewPagerAdapter
import kotlin.math.abs


class EventsFragment : Fragment() {
    private var _binding: NewFragmentEventsBinding? = null
    val binding: NewFragmentEventsBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createEvent()
        initializeTab()
        handleImageViewAnimation()
    }

    private fun initializeTab() {
        val viewPager = binding.viewPager
        val adapter = EventsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val tabLayout = binding.tabLayout
        val tabs = arrayOf(
            requireContext().getString(R.string.my_events),
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

    private fun createEvent() {
        binding.createEvent.setOnClickListener {
            startActivity(
                Intent(context, CreateEventActivity::class.java)
            )
        }
    }
}