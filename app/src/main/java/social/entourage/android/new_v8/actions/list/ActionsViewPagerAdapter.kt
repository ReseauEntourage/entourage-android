package social.entourage.android.new_v8.actions.list

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ActionsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    private val NB_TABS = 2

    override fun getItemCount(): Int {
        return NB_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            ActionListFragment.newInstance(true)
        } else {
            ActionListFragment.newInstance(false)
        }
    }
}