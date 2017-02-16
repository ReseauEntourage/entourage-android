package social.entourage.android.carousel;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import social.entourage.android.carousel.pages.CarouselPage1Fragment;
import social.entourage.android.carousel.pages.CarouselPage2Fragment;
import social.entourage.android.carousel.pages.CarouselPage3Fragment;
import social.entourage.android.carousel.pages.CarouselPage4Fragment;

/**
 * Created by mihaiionescu on 14/02/2017.
 */

public class CarouselPageAdapter extends FragmentStatePagerAdapter {

    protected static final int NUM_PAGES = 4;

    public CarouselPageAdapter(final FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 0:
                return new CarouselPage1Fragment();
            case 1:
                return new CarouselPage2Fragment();
            case 2:
                return new CarouselPage3Fragment();
            case 3:
                return new CarouselPage4Fragment();
            default:
                return new CarouselPage1Fragment();
        }

    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
