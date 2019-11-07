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
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_pin_drop_white_24dp), homeFragment, MapEntourageWithTourFragment.TAG);
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_map_white_24dp), new GuideMapEntourageFragment(), GuideMapEntourageFragment.TAG);
        add(new NavigationItem(R.string.empty_description, R.drawable.ic_add_circle_outline_white_24dp), homeFragment, MapEntourageWithTourFragment.TAG);
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_mail_outline_black_24dp), new MyEntouragesFragment(), MyEntouragesFragment.TAG);
        add(new NavigationItem(R.string.action_empty, R.drawable.ic_account_circle_outline_white_24dp), new SideMenuFragment(), SideMenuFragment.TAG);

        guideTabIndex = 1;
        myMessagesTabIndex=3;
        actionTabIndex = 2;
    }

}
