package social.entourage.android.new_v8.events.create

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

const val NB_TABS = 3

class CreateEventViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NB_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CreateEventStepOneFragment()
            1 -> CreateEventStepTwoFragment()
            2 -> CreateEventStepThreeFragment()
            else -> CreateEventStepOneFragment()
        }
    }
}