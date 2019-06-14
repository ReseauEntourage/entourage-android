package social.entourage.android.authentification.login.slideshow

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import social.entourage.android.authentification.login.slideshow.pages.LoginSlideshowPage1Fragment
import social.entourage.android.authentification.login.slideshow.pages.LoginSlideshowPage2Fragment
import social.entourage.android.authentification.login.slideshow.pages.LoginSlideshowPage3Fragment

/**
 * Created by Mihai Ionescu on 12/09/2018.
 */
class LoginSlideshowPageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return LoginSlideshowPage1Fragment()
            1 -> return LoginSlideshowPage2Fragment()
            2 -> return LoginSlideshowPage3Fragment()
            else -> return LoginSlideshowPage1Fragment()
        }
    }

    override fun getCount(): Int {
        return NUM_PAGES
    }

    companion object {
        val NUM_PAGES = 3
    }
}
