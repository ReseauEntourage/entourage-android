package social.entourage.android.base;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.crashlytics.android.Crashlytics;

import social.entourage.android.R;
import social.entourage.android.deeplinks.DeepLinksManager;

/**
 * Base DialogFragment with no title and full screen
 * Created by mihaiionescu on 17/06/16.
 */
public class EntourageDialogFragment extends DialogFragment {

    private boolean isStopped = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }

        return null;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DeepLinksManager.getInstance().handleCurrentDeepLink(this.getActivity());
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        if (getDialog() == null ) {  // Returns mDialog
            // Tells DialogFragment to not use the fragment as a dialog, and so won't try to use mDialog
            setShowsDialog( false );
            //TODO check if this is working and how often
            Crashlytics.log("Quickfix to avoid crash in DialogFragment.onActivityCreated");
        }
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null && window.getAttributes() != null) {
                window.getAttributes().windowAnimations = getSlideStyle();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                window.setBackgroundDrawable(getBackgroundDrawable());
            }
        }
        isStopped = false;
    }

    @Override
    public void onStop() {
        super.onStop();

        isStopped = true;
    }

    public boolean isStopped() {
        return isStopped;
    }

    protected @StyleRes int getSlideStyle() {
        return R.style.CustomDialogFragmentFromRight;
    }

    protected ColorDrawable getBackgroundDrawable() {
        return new ColorDrawable(Color.TRANSPARENT);
    }

    public void showKeyboard() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    protected void hideKeyboard() {
        View view = getDialog().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
