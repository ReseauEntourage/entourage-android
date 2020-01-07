package social.entourage.android.navigation;

import androidx.fragment.app.Fragment;

import social.entourage.android.PlusFragment;
import social.entourage.android.R;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.entourage.my.MyEntouragesFragment;
import social.entourage.android.map.MapVoisinageFragment;
import social.entourage.android.sidemenu.SideMenuFragment;

/**
 * Created by Mihai Ionescu on 23/04/2018.
 */
public class BottomNavigationDataSource extends BaseBottomNavigationDataSource {

    public BottomNavigationDataSource() {
        add(R.id.bottom_bar_newsfeed, MapVoisinageFragment.TAG);
        add(R.id.bottom_bar_plus, PlusFragment.TAG);
        add(R.id.bottom_bar_mymessages, MyEntouragesFragment.TAG);
        add(R.id.bottom_bar_profile, SideMenuFragment.TAG);

        defaultSelectedTab = R.id.bottom_bar_mymessages; // My messages tab
    }

    public Fragment getFragmentAtIndex(int menuId) {
        switch(menuId) {
            case R.id.bottom_bar_newsfeed:
                return new MapVoisinageFragment();
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
