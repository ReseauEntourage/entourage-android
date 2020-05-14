package social.entourage.android.navigation

import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import social.entourage.android.R

/**
 * Bottom navigation data source
 * It provides the icons, texts and fragments related to the bottom navigation bar
 * It must be subclassed in each app target
 * Created by Mihai Ionescu on 23/04/2018.
 */
abstract class BaseBottomNavigationDataSource internal constructor() {
    private val navigationFragmentTags = SparseArray<String>()

    @IdRes
    var defaultSelectedTab = R.id.bottom_bar_newsfeed

    @IdRes
    var feedTabIndex = R.id.bottom_bar_newsfeed

    @IdRes
    var myMessagesTabIndex = R.id.bottom_bar_mymessages

    //optional menu
    @IdRes
    var actionMenuId = -1

    @IdRes
    var guideTabIndex = -1
    protected fun add(@IdRes menuId: Int, tag: String) {
        navigationFragmentTags.put(menuId, tag)
    }

    open fun getFragmentAtIndex(menuId: Int): Fragment? {
        return null
    }

    fun getFragmentTagAtIndex(menuId: Int): String {
        return navigationFragmentTags[menuId]
    }

}