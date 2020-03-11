package social.entourage.android.base;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import social.entourage.android.MainActivity;
import social.entourage.android.R;
import social.entourage.android.deeplinks.DeepLinksManager;

/**
 * Base DialogFragment with no title and full screen
 * Created by mihaiionescu on 17/06/16.
 */
public class EntourageDialogFragment extends DialogFragment {

    private boolean isStopped = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity() instanceof MainActivity) {
            DeepLinksManager.Companion.getInstance().handleCurrentDeepLink((MainActivity)getActivity());
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        if (getDialog() == null) {
            //TODO should we use setShowsDialog(false) here
            setShowsDialog(false);
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
                if (window.getAttributes() != null) {
                    window.getAttributes().windowAnimations = getSlideStyle();
                }
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

    protected void showKeyboard() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    protected void hideKeyboard() {
        if (getDialog() != null && getDialog().getCurrentFocus() !=null && getActivity()!= null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm!=null) {
                imm.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }
}
