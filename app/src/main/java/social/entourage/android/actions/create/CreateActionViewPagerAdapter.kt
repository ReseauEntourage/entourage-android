package social.entourage.android.actions.create

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

const val NB_TABS = 3

class CreateActionViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NB_TABS
    }
    // NOTE : FirST STEP FRAGMENT IS TITLE AND DESC FRAGMENT, SECOND STEP FRAGMENT IS CATEGORY FRAGMENT.
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CreateActionStepTwoFragment()
            1 -> CreateActionStepOneFragment()
            else -> CreateActionStepThreeFragment()
        }
    }
}
