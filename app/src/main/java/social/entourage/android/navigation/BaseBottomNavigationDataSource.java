package social.entourage.android.navigation;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Bottom navigation data source
 * It provides the icons, texts and fragments related to the bottom navigation bar
 * It must be subclassed in each app target
 * Created by Mihai Ionescu on 23/04/2018.
 */
public abstract class BaseBottomNavigationDataSource {

    ArrayList<NavigationItem> navigationItems = new ArrayList();
    ArrayList<Fragment> navigationFragments = new ArrayList<>();
    ArrayList<String> navigationFragmentTags = new ArrayList<>();

    protected int defaultSelectedTab = 0;

    public BaseBottomNavigationDataSource() {

    }

    protected void add(NavigationItem navigationItem, Fragment fragment, String tag) {
        navigationItems.add(navigationItem);
//        navigationClassFragments.add(fragmentClass);
        navigationFragments.add(fragment);
        navigationFragmentTags.add(tag);
    }

    public int getItemCount() {
        return navigationItems.size();
    }

    public NavigationItem getNavigationItemAtIndex(int index) {
        if (index < 0 || index >= navigationItems.size()) return null;
        return navigationItems.get(index);
    }

    public Fragment getFragmentAtIndex(int index) {
        if (index < 0 || index >= navigationFragments.size()) return null;
        Fragment fragment = navigationFragments.get(index);
        return fragment;
    }

    public String getFragmentTagAtIndex(int index) {
        if (index < 0 || index >= navigationFragmentTags.size()) return null;
        return navigationFragmentTags.get(index);
    }

    public int getDefaultSelectedTab() {
        return defaultSelectedTab;
    }

    public static class NavigationItem {
        private @StringRes int text;
        private @DrawableRes int icon;

        public NavigationItem(@StringRes int text, @DrawableRes int icon) {
            this.text = text;
            this.icon = icon;
        }

        public int getText() {
            return text;
        }

        public int getIcon() {
            return icon;
        }
    }
}
