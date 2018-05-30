package social.entourage.android.navigation;

import social.entourage.android.R;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.entourage.my.MyEntouragesFragment;
import social.entourage.android.sidemenu.SideMenuFragment;

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
public class BottomNavigationDataSource extends BaseBottomNavigationDataSource {

    public static final int TAB_MAP = 0;

    public BottomNavigationDataSource() {
        add(new NavigationItem(R.string.action_map, R.drawable.ic_navigation_map), new MapEntourageFragment(), MapEntourageFragment.TAG);
        add(new NavigationItem(R.string.action_my_messages, R.drawable.ic_navigation_my_messages), new MyEntouragesFragment(), MyEntouragesFragment.TAG);
        add(new NavigationItem(R.string.action_menu, R.drawable.ic_navigation_menu), new SideMenuFragment(), SideMenuFragment.TAG);
    }

}
