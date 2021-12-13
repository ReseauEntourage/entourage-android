package social.entourage.android.base.newsfeed

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.*

/**
 * Created by Mihai Ionescu on 10/10/2017.
 */

class NewsfeedFragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {

    private val fragmentList = ArrayList<Fragment>()

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        fragmentList.add(f)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        fragmentList.remove(f)
    }
}
