package social.entourage.android.navigation;

import social.entourage.android.R;
import social.entourage.android.guide.GuideMapEntourageFragment;
import social.entourage.android.map.MapEntourageWithTourFragment;
import social.entourage.android.entourage.my.MyEntouragesFragment;
import social.entourage.android.sidemenu.SideMenuFragment;

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
public class BottomNavigationDataSource extends BaseBottomNavigationDataSource {

    public BottomNavigationDataSource() {
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_navigation_map), new MapEntourageWithTourFragment(), MapEntourageWithTourFragment.TAG);
        //TODO add proper tag
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_navigation_guide), new GuideMapEntourageFragment(), GuideMapEntourageFragment.TAG);
        //add(new NavigationItem(R.string.action_empty, R.drawable.ic_bar_plus), new MapEntourageFragment(), MapEntourageFragment.TAG);
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_navigation_my_messages), new MyEntouragesFragment(), MyEntouragesFragment.TAG);
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_navigation_menu), new SideMenuFragment(), SideMenuFragment.TAG);

        guideTabIndex = 1;
        myMessagesTabIndex=2;
    }

}
