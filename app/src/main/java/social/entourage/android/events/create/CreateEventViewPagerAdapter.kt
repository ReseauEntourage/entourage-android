package social.entourage.android.events.create

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

const val NB_TABS = 5

class CreateEventViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments: MutableList<Fragment> = mutableListOf(
        CreateEventStepOneFragment(),
        CreateEventStepTwoFragment(),
        CreateEventStepThreeFragment(),
        CreateEventStepFourFragment(),
        CreateEventStepFiveFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}