package social.entourage.android.old_v7.about.carousel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import social.entourage.android.old_v7.about.carousel.pages.CarouselPage1Fragment
import social.entourage.android.old_v7.about.carousel.pages.CarouselPage2Fragment
import social.entourage.android.old_v7.about.carousel.pages.CarouselPage3Fragment
import social.entourage.android.old_v7.about.carousel.pages.CarouselPage4Fragment

/**
 * Created by mihaiionescu on 14/02/2017.
 */
class CarouselPageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> CarouselPage2Fragment()
            2 -> CarouselPage3Fragment()
            3 -> CarouselPage4Fragment()
            0 -> CarouselPage1Fragment()
            else -> CarouselPage1Fragment()
        }
    }

    override fun getCount(): Int {
        return NUM_PAGES
    }

    companion object {
        const val NUM_PAGES = 4
    }
}