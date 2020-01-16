package social.entourage.android.navigation;

import androidx.fragment.app.Fragment;

import social.entourage.android.PlusFragment;
import social.entourage.android.R;
import social.entourage.android.guide.GuideMapFragment;
import social.entourage.android.map.MapWithTourFragment;
import social.entourage.android.entourage.my.MyEntouragesFragment;
import social.entourage.android.sidemenu.SideMenuFragment;

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
public class BottomNavigationDataSource extends BaseBottomNavigationDataSource {

    public BottomNavigationDataSource() {
        add(R.id.bottom_bar_newsfeed, MapWithTourFragment.TAG);
        add(R.id.bottom_bar_guide, GuideMapFragment.TAG);
        add(R.id.bottom_bar_plus, PlusFragment.TAG);
        add(R.id.bottom_bar_mymessages, MyEntouragesFragment.TAG);
        add(R.id.bottom_bar_profile, SideMenuFragment.TAG);

        guideTabIndex = R.id.bottom_bar_guide;
    }

    public Fragment getFragmentAtIndex(int menuId) {
        switch(menuId) {
            case R.id.bottom_bar_newsfeed:
                return new MapWithTourFragment();
            case R.id.bottom_bar_guide:
                return new GuideMapFragment();
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
