package social.entourage.android.map;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

/**
 * Created by Mihai Ionescu on 10/10/2017.
 */

public class MapEntourageFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    @Override
    public void onFragmentAttached(@NonNull final FragmentManager fm, @NonNull final Fragment f, @NonNull final Context context) {
        super.onFragmentAttached(fm, f, context);
        if (f != null) {
            fragmentList.add(f);
        }
    }

    @Override
    public void onFragmentDetached(@NonNull final FragmentManager fm, @NonNull final Fragment f) {
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

    public void dismissAllDialogs() {
        int count = fragmentList.size() - 1;
        while (count >= 0) {
            Fragment fragment = fragmentList.get(count);
            if (fragment instanceof DialogFragment) {
                ((DialogFragment) fragment).dismissAllowingStateLoss();
            }
            count--;
        }
    }

}
