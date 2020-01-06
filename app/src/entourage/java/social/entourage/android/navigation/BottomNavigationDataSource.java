package social.entourage.android.navigation;

import androidx.fragment.app.Fragment;

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
        add(R.id.bottom_bar_newsfeed, MapEntourageWithTourFragment.TAG);
        add(R.id.bottom_bar_guide, GuideMapEntourageFragment.TAG);
        add(R.id.bottom_bar_plus, PlusFragment.TAG);
        add(R.id.bottom_bar_mymessages, MyEntouragesFragment.TAG);
        add(R.id.bottom_bar_profile, SideMenuFragment.TAG);

        guideTabIndex = R.id.bottom_bar_guide;
        myMessagesTabIndex=R.id.bottom_bar_mymessages;
        actionTabIndex = R.id.bottom_bar_plus;
    }

    public Fragment getFragmentAtIndex(int menuId) {
        switch(menuId) {
            case R.id.bottom_bar_newsfeed:
                return new MapEntourageWithTourFragment();
            case R.id.bottom_bar_guide:
                return new GuideMapEntourageFragment();
            case R.id.bottom_bar_plus:
                return new PlusFragment();
            case R.id.bottom_bar_mymessages:
                return new MyEntouragesFragment();
            case R.id.bottom_bar_profile:
                return new SideMenuFragment();
            default:
                return null;
        }
    }
}
