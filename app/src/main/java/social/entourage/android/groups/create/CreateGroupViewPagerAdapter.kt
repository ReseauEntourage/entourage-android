package social.entourage.android.groups.create

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

const val NB_TABS = 3

class CreateGroupViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NB_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CreateGroupStepOneFragment()
            1 -> CreateGroupStepTwoFragment()
            2 -> CreateGroupStepThreeFragment()
            else -> CreateGroupStepTwoFragment()
        }
    }
}
