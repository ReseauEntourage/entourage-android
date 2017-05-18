package social.entourage.android.base;

import android.app.Dialog;
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

import social.entourage.android.R;

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
    public void onActivityCreated(final Bundle savedInstanceState) {
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
        return R.style.CustomDialogFragmentSlide;
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

}
