package social.entourage.android.navigation;

import social.entourage.android.R;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.entourage.my.MyEntouragesFragment;
import social.entourage.android.sidemenu.SideMenuFragment;

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
public class BottomNavigationDataSource extends BaseBottomNavigationDataSource {

    public BottomNavigationDataSource() {
        add(R.id.bottom_bar_item1, new MapEntourageFragment(), MapEntourageFragment.TAG);
        add(R.id.bottom_bar_item2, new MyEntouragesFragment(), MyEntouragesFragment.TAG);
        add(R.id.bottom_bar_item3, new SideMenuFragment(), SideMenuFragment.TAG);

        defaultSelectedTab = R.id.bottom_bar_item2; // My messages tab
    }

}
