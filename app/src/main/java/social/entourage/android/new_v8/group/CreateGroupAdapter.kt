package social.entourage.android.new_v8.group

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NB_TABS = 1

class CreateGroupAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NB_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return CreateGroupStepThreeFragment()
    }
}
