package social.entourage.android.groups

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import social.entourage.android.groups.list.DiscoverGroupsListFragment
import social.entourage.android.groups.list.MyGroupsListFragment

private const val NB_TABS = 2

class GroupsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NB_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == MY_GROUPS_TAB) {
            MyGroupsListFragment()
        } else {
            DiscoverGroupsListFragment()
        }

    }
}