package social.entourage.android.navigation;

import android.util.SparseArray;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;

import social.entourage.android.R;

/**
 * Bottom navigation data source
 * It provides the icons, texts and fragments related to the bottom navigation bar
 * It must be subclassed in each app target
 * Created by Mihai Ionescu on 23/04/2018.
 */
public abstract class BaseBottomNavigationDataSource {

    private SparseArray<Fragment> navigationFragments = new SparseArray<>();
    private SparseArray<String> navigationFragmentTags = new SparseArray<>();

    @IdRes int defaultSelectedTab = R.id.bottom_bar_newsfeed;
    @IdRes int feedTabIndex = R.id.bottom_bar_newsfeed;
    @IdRes int myMessagesTabIndex = R.id.bottom_bar_mymessages;
    //unused menu
    @IdRes int actionTabIndex =-1;
    @IdRes int guideTabIndex = -1;

    BaseBottomNavigationDataSource() {}

    protected void add(@IdRes int menuId, Fragment fragment, String tag) {
        navigationFragments.put(menuId, fragment);
        navigationFragmentTags.put(menuId, tag);
    }

    public Fragment getFragmentAtIndex(int menuId) {
        return navigationFragments.get(menuId);
    }

    public String getFragmentTagAtIndex(int menuId) {
        return navigationFragmentTags.get(menuId);
    }

    public int getDefaultSelectedTab() {
        return defaultSelectedTab;
    }

    public int getFeedTabIndex() {
        return feedTabIndex;
    }

    public int getMyMessagesTabIndex() {
        return myMessagesTabIndex;
    }

    public int getGuideTabIndex() {
        return guideTabIndex;
    }

    public int getActionMenuId() {
        return actionTabIndex;
    }
}
