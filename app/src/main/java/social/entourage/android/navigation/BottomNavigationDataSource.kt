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
import social.entourage.android.newsfeed.v2.NewHomeFeedFragment

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
class BottomNavigationDataSource {
    private val navigationFragmentTags = SparseArray<String>()

    val defaultSelectedTab: Int
        @IdRes get() = R.id.bottom_bar_newsfeed

    val feedTabIndex
        @IdRes get() = R.id.bottom_bar_newsfeed
    val myMessagesTabIndex
        @IdRes get() = R.id.bottom_bar_mymessages
    val actionMenuId
        @IdRes get() = R.id.bottom_bar_plus
    val guideTabIndex
        @IdRes get() = R.id.bottom_bar_guide
    val profilTabIndex
        @IdRes get() = R.id.bottom_bar_profile


    private fun add(@IdRes menuId: Int, tag: String) {
        navigationFragmentTags.put(menuId, tag)
    }

    fun getFragmentTagAtIndex(menuId: Int): String {
        return navigationFragmentTags[menuId]
    }

    fun getFragmentAtIndex(menuId: Int): Fragment? {
        return when (menuId) {
            R.id.bottom_bar_newsfeed -> NewHomeFeedFragment()
            R.id.bottom_bar_guide -> GuideHubFragment()
            R.id.bottom_bar_plus ->  PlusFragment()
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

        //Use to reset navigation stack home tab
        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",false)
        editor.putString("navType",null)
        editor.apply()
    }
}