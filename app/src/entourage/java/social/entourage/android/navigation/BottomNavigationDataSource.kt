package social.entourage.android.navigation

import androidx.fragment.app.Fragment
import social.entourage.android.EntourageApplication
import social.entourage.android.PlusFragment
import social.entourage.android.R
import social.entourage.android.entourage.my.MyEntouragesFragment
import social.entourage.android.guide.GuideMapFragment
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.newsfeed.NewsFeedFragment
import social.entourage.android.newsfeed.NewsFeedWithTourFragment
import social.entourage.android.sidemenu.SideMenuFragment

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
class BottomNavigationDataSource : BaseBottomNavigationDataSource() {
    override fun getFragmentAtIndex(menuId: Int): Fragment? {
        return when (menuId) {
            R.id.bottom_bar_newsfeed -> if(EntourageApplication.get().me()?.isPro() == true) NewsFeedWithTourFragment() else NewsFeedFragment()
            R.id.bottom_bar_guide -> GuideMapFragment()
            R.id.bottom_bar_plus -> PlusFragment()
            R.id.bottom_bar_mymessages -> MyEntouragesFragment()
            R.id.bottom_bar_profile -> SideMenuFragment()
            else -> null
        }
    }

    init {
        add(R.id.bottom_bar_newsfeed, BaseNewsfeedFragment.TAG)
        add(R.id.bottom_bar_guide, GuideMapFragment.TAG)
        add(R.id.bottom_bar_plus, PlusFragment.TAG)
        add(R.id.bottom_bar_mymessages, MyEntouragesFragment.TAG)
        add(R.id.bottom_bar_profile, SideMenuFragment.TAG)
        guideTabIndex = R.id.bottom_bar_guide
        actionMenuId = R.id.bottom_bar_plus
    }
}