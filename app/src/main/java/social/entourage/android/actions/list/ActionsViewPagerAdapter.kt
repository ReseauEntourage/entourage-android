package social.entourage.android.actions.list

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.actions.list.me.MyActionsListFragment

class ActionsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    private val NB_TABS = 3

    override fun getItemCount(): Int {
        return NB_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            ActionListFragment.newInstance(true, false)
        } else if (position == 1) {
            ActionListFragment.newInstance(false,false)
        }else{
            MyActionsListFragment()
        }
    }
}