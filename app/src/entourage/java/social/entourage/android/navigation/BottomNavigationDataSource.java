package social.entourage.android.navigation;

import social.entourage.android.PlusFragment;
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
        add(R.id.bottom_bar_newsfeed, new MapEntourageWithTourFragment(), MapEntourageWithTourFragment.TAG);
        add(R.id.bottom_bar_guide, new GuideMapEntourageFragment(), GuideMapEntourageFragment.TAG);
        add(R.id.bottom_bar_plus, new PlusFragment(), PlusFragment.TAG);
        add(R.id.bottom_bar_mymessages, new MyEntouragesFragment(), MyEntouragesFragment.TAG);
        add(R.id.bottom_bar_profile, new SideMenuFragment(), SideMenuFragment.TAG);

        guideTabIndex = R.id.bottom_bar_guide;
        myMessagesTabIndex=R.id.bottom_bar_mymessages;
        actionTabIndex = R.id.bottom_bar_plus;
    }
}
