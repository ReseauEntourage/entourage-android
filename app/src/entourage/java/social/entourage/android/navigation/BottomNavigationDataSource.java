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
        MapEntourageWithTourFragment homeFragment = new MapEntourageWithTourFragment();
        add(R.id.bottom_bar_item1, homeFragment, MapEntourageWithTourFragment.TAG);
        add(R.id.bottom_bar_item2, new GuideMapEntourageFragment(), GuideMapEntourageFragment.TAG);
        add(R.id.bottom_bar_item3, homeFragment, MapEntourageWithTourFragment.TAG);
        add(R.id.bottom_bar_item4, new MyEntouragesFragment(), MyEntouragesFragment.TAG);
        add(R.id.bottom_bar_item5, new SideMenuFragment(), SideMenuFragment.TAG);

        guideTabIndex = R.id.bottom_bar_item2;
        myMessagesTabIndex=R.id.bottom_bar_item4;
        actionTabIndex = R.id.bottom_bar_item3;
    }
}
