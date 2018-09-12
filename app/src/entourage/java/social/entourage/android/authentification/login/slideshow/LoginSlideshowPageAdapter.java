package social.entourage.android.authentification.login.slideshow;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Mihai Ionescu on 12/09/2018.
 */
public class LoginSlideshowPageAdapter extends FragmentStatePagerAdapter {

    public static final int NUM_PAGES = 0;

    public LoginSlideshowPageAdapter(final FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
