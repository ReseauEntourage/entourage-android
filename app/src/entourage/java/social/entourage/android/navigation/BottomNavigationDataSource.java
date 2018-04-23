package social.entourage.android.navigation;

import social.entourage.android.R;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.entourage.my.MyEntouragesFragment;
import social.entourage.android.sidemenu.SideMenuFragment;

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
public class BottomNavigationDataSource extends BaseBottomNavigationDataSource {

    public BottomNavigationDataSource() {
        add(new NavigationItem(R.string.action_map, R.drawable.navigation_tab_item_map), new MapEntourageFragment(), MapEntourageFragment.TAG);
        add(new NavigationItem(R.string.action_my_messages, 0), new MyEntouragesFragment(), MyEntouragesFragment.TAG);
        add(new NavigationItem(R.string.action_menu, R.drawable.navigation_tab_item_menu), new SideMenuFragment(), SideMenuFragment.TAG);
    }

}
