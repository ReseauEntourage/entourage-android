package social.entourage.android.authentification.login.slideshow;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import social.entourage.android.authentification.login.slideshow.pages.LoginSlideshowPage1Fragment;
import social.entourage.android.authentification.login.slideshow.pages.LoginSlideshowPage2Fragment;
import social.entourage.android.authentification.login.slideshow.pages.LoginSlideshowPage3Fragment;

/**
 * Created by Mihai Ionescu on 12/09/2018.
 */
public class LoginSlideshowPageAdapter extends FragmentStatePagerAdapter {

    public static final int NUM_PAGES = 3;

    public LoginSlideshowPageAdapter(final FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 0:
                return new LoginSlideshowPage1Fragment();
            case 1:
                return new LoginSlideshowPage2Fragment();
            case 2:
                return new LoginSlideshowPage3Fragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
