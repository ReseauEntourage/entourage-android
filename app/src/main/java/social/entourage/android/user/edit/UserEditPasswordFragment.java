package social.entourage.android.user.edit;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.base.EntourageDialogFragment;

public class UserEditPasswordFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_password_fragment";
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 6;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.user_old_password)
    EditText oldPasswordEditText;

    @BindView(R.id.user_new_password)
    EditText newPasswordEditText;

    @BindView(R.id.user_confirm_password)
    EditText confirmPasswordEditText;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View toReturn = inflater.inflate(R.layout.fragment_user_edit_password, container, false);
        ButterKnife.bind(this, toReturn);
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_09_4);
        configureView();

        return toReturn;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButton() {
        dismiss();
    }

    @OnClick(R.id.user_edit_password_save_button)
    protected void onSaveButton() {
        if (validatePassword()) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_09_4_SUBMIT);
            UserEditFragment userEditFragment = (UserEditFragment) getFragmentManager().findFragmentByTag(UserEditFragment.TAG);
            if (userEditFragment != null) {
                userEditFragment.saveNewPassword(newPasswordEditText.getText().toString().trim());
            }

            dismiss();
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureView() {
        oldPasswordEditText.setTypeface(Typeface.DEFAULT);
        oldPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

        newPasswordEditText.setTypeface(Typeface.DEFAULT);
        newPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

        confirmPasswordEditText.setTypeface(Typeface.DEFAULT);
        confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

        oldPasswordEditText.requestFocus();
    }

    private boolean validatePassword() {
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String userPassword = "";
        UserEditFragment userEditFragment = (UserEditFragment) getFragmentManager().findFragmentByTag(UserEditFragment.TAG);
        if (userEditFragment != null) {
            User user = userEditFragment.getEditedUser();
            userPassword = user.getSmsCode();
        }
        if (!oldPassword.equals(userPassword)) {
            displayToast(R.string.user_edit_password_invalid_current_password);
            return false;
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            displayToast(R.string.user_edit_password_new_password_too_short);
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            displayToast(R.string.user_edit_password_not_match);
            return false;
        }

        return true;
    }

    private void displayToast(@StringRes int stringId) {
        Toast.makeText(getActivity(), stringId, Toast.LENGTH_SHORT).show();
    }

}
