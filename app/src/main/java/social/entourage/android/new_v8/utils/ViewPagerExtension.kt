package social.entourage.android.new_v8.utils

import androidx.viewpager2.widget.ViewPager2
import timber.log.Timber

fun ViewPager2.nextPage(smoothScroll: Boolean = true): Boolean {
    Timber.e("in next")
    if ((currentItem + 1) < adapter?.itemCount ?: 0) {
        setCurrentItem(currentItem + 1, smoothScroll)
        return true
    }
    //can't move to next page, maybe current page is last or adapter not set.
    return false
}

fun ViewPager2.previousPage(smoothScroll: Boolean = true): Boolean {
    if ((currentItem - 1) >= 0) {
        setCurrentItem(currentItem - 1, smoothScroll)
        return true
    }
    //can't move to previous page, maybe current page is first or adapter not set.
    return false
}