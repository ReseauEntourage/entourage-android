package social.entourage.android.navigation

import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import social.entourage.android.EntourageApplication
import social.entourage.android.PlusFragment
import social.entourage.android.R
import social.entourage.android.entourage.my.MyEntouragesFragment
import social.entourage.android.guide.GuideHubFragment
import social.entourage.android.mainprofile.MainProfileFragment
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.newsfeed.NewsFeedFragment
import social.entourage.android.newsfeed.NewsFeedWithTourFragment
import social.entourage.android.tools.log.EntourageEvents

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
class BottomNavigationDataSource {
    private val navigationFragmentTags = SparseArray<String>()
    var isEngaged = false

    val defaultSelectedTab: Int
        @IdRes get() = if(isEngaged) R.id.bottom_bar_newsfeed else R.id.bottom_bar_guide

    val feedTabIndex
        @IdRes get() = R.id.bottom_bar_newsfeed
    val myMessagesTabIndex
        @IdRes get() = R.id.bottom_bar_mymessages
    val agirTabIndex
        @IdRes get()= R.id.bottom_bar_plus
    val actionMenuId
        @IdRes get() = R.id.bottom_bar_plus
    val guideTabIndex
        @IdRes get() = R.id.bottom_bar_guide

    private fun add(@IdRes menuId: Int, tag: String) {
        navigationFragmentTags.put(menuId, tag)
    }

    fun getFragmentTagAtIndex(menuId: Int): String {
        return navigationFragmentTags[menuId]
    }

    fun getFragmentAtIndex(menuId: Int): Fragment? {
        return when (menuId) {
            R.id.bottom_bar_newsfeed -> if(EntourageApplication.get().me()?.isPro == true) NewsFeedWithTourFragment() else NewsFeedFragment()
            R.id.bottom_bar_guide -> GuideHubFragment()//GuideMapFragment()
            R.id.bottom_bar_plus ->  {
                EntourageEvents.logEvent(EntourageEvents.ACTION_PLUS_AGIR)
                PlusFragment()
            }
            R.id.bottom_bar_mymessages -> MyEntouragesFragment()
            R.id.bottom_bar_profile -> MainProfileFragment()
            else -> null
        }
    }

    init {
        add(R.id.bottom_bar_newsfeed, BaseNewsfeedFragment.TAG)
        add(R.id.bottom_bar_guide, GuideHubFragment.TAG)
        add(R.id.bottom_bar_plus, PlusFragment.TAG)
        add(R.id.bottom_bar_mymessages, MyEntouragesFragment.TAG)
        add(R.id.bottom_bar_profile, MainProfileFragment.TAG)
    }
}