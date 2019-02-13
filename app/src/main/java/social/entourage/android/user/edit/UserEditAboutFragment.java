package social.entourage.android.user.edit;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.user.UserFragment;

/**
 * Fragment to edit an user's about text
 */

public class UserEditAboutFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = UserEditAboutFragment.class.getSimpleName();

    private static final int ABOUT_MAX_CHAR_COUNT = 200;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.user_edit_about)
    EditText aboutEditText;

    @BindView(R.id.user_edit_about_char_count)
    TextView aboutCharCountTextView;

    private User user;
    private UserEditFragment userEditFragment;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditAboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        View view = inflater.inflate(R.layout.fragment_user_edit_about, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureView();
    }

    @Override
    public void dismiss() {
        if (userEditFragment != null) {
            userEditFragment.scrollToOriginalPosition();
        }
        super.dismiss();
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.user_edit_about_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.user_edit_about_save_button)
    protected void onSaveClicked() {
        String about = aboutEditText.getText().toString();
        if (about.length() > ABOUT_MAX_CHAR_COUNT) {
            Toast.makeText(getContext(), R.string.user_edit_about_error, Toast.LENGTH_SHORT).show();
        } else {
            if (user != null) {
                user.setAbout(about.trim());
                if (userEditFragment != null) {
                    userEditFragment.configureView();
                } else {
                    if (getFragmentManager() != null) {
                        UserFragment userFragment = (UserFragment) getFragmentManager().findFragmentByTag(UserFragment.TAG);
                        if (userFragment != null) {
                            userFragment.saveAccount(user);
                        }
                    }
                }
            }
            dismiss();
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureView() {
        if (getFragmentManager() != null) {
            userEditFragment = (UserEditFragment) getFragmentManager().findFragmentByTag(UserEditFragment.TAG);
            if (userEditFragment == null) {
                //Try to see if we can find the User View fragment
                UserFragment userFragment = (UserFragment) getFragmentManager().findFragmentByTag(UserFragment.TAG);
                if (userFragment != null) {
                    user = userFragment.getEditedUser();
                }
            } else {
                user = userEditFragment.getEditedUser();
            }
        }
        if (user == null) {
            return;
        }

        aboutEditText.setText(user.getAbout());
        aboutEditText.setSelection(aboutEditText.length());
        aboutEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                String charCountString = getContext().getString(R.string.entourage_create_title_char_count_format, s.length(), ABOUT_MAX_CHAR_COUNT);
                aboutCharCountTextView.setText(charCountString);
                if (s.length() > ABOUT_MAX_CHAR_COUNT) {
                    aboutCharCountTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.entourage_error, null));
                } else {
                    aboutCharCountTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.entourage_ok, null));
                }
            }
        });

        String charCountString = getContext().getString(R.string.entourage_create_title_char_count_format, aboutEditText.length(), ABOUT_MAX_CHAR_COUNT);
        aboutCharCountTextView.setText(charCountString);
    }

}
