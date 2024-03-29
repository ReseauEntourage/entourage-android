package social.entourage.android.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import social.entourage.android.profile.myProfile.MyProfileFragment
import social.entourage.android.profile.settings.SettingsFragment

private const val NB_TABS = 2

class ProfileViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NB_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) MyProfileFragment() else SettingsFragment()
    }
}