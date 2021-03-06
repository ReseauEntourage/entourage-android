package social.entourage.android.newsfeed

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import timber.log.Timber

import java.util.ArrayList

/**
 * Created by Mihai Ionescu on 10/10/2017.
 */

class NewsfeedFragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {

    private val fragmentList = ArrayList<Fragment>()

    val topFragment: Fragment?
        get() = if (fragmentList.size > 0) {
            fragmentList[fragmentList.size - 1]
        } else null

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        fragmentList.add(f)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        fragmentList.remove(f)
    }

    fun dismissAllDialogs() {
        var count = fragmentList.size - 1
        while (count >= 0) {
            val fragment = fragmentList[count]
            try{
                if (fragment is DialogFragment) {
                    fragment.dismiss()
                }
            } catch(e: IllegalStateException) {
                Timber.w(e)
            }
            count--
        }
    }

}
