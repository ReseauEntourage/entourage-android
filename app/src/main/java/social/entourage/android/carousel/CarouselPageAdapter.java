package social.entourage.android.carousel;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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

    @NonNull
    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 1:
                return new CarouselPage2Fragment();
            case 2:
                return new CarouselPage3Fragment();
            case 3:
                return new CarouselPage4Fragment();
            case 0:
            default:
                return new CarouselPage1Fragment();
        }

    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
