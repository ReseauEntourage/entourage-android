package social.entourage.android.map;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

/**
 * Created by Mihai Ionescu on 10/10/2017.
 */

public class MapEntourageFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    @Override
    public void onFragmentAttached(final FragmentManager fm, final Fragment f, final Context context) {
        super.onFragmentAttached(fm, f, context);
        if (f != null) {
            fragmentList.add(f);
        }
    }

    @Override
    public void onFragmentDetached(final FragmentManager fm, final Fragment f) {
        super.onFragmentDetached(fm, f);
        if (f != null) {
            fragmentList.remove(f);
        }
    }

    public Fragment getTopFragment() {
        if (fragmentList.size() > 0) {
            return fragmentList.get(fragmentList.size() - 1);
        }
        return null;
    }

}
