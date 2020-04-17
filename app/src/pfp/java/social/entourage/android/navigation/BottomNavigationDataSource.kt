package social.entourage.android.navigation

import androidx.fragment.app.Fragment
import social.entourage.android.R
import social.entourage.android.entourage.my.MyEntouragesFragment
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.map.MapVoisinageFragment
import social.entourage.android.mainprofile.MainProfileFragment

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
class BottomNavigationDataSource : BaseBottomNavigationDataSource() {
    override fun getFragmentAtIndex(menuId: Int): Fragment? {
        return when (menuId) {
            R.id.bottom_bar_newsfeed -> MapVoisinageFragment()
            R.id.bottom_bar_mymessages -> MyEntouragesFragment()
            R.id.bottom_bar_profile -> MainProfileFragment()
            else -> null
        }
    }

    init {
        add(R.id.bottom_bar_newsfeed, BaseNewsfeedFragment.TAG)
        add(R.id.bottom_bar_mymessages, MyEntouragesFragment.TAG)
        add(R.id.bottom_bar_profile, MainProfileFragment.TAG)
        defaultSelectedTab = R.id.bottom_bar_mymessages // My messages tab
    }
}