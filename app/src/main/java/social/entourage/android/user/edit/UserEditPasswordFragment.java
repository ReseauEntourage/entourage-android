package social.entourage.android.user.edit;


import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;

public class UserEditPasswordFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_password_fragment";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Bind(R.id.user_old_password)
    EditText oldPassword;

    @Bind(R.id.user_new_password)
    EditText newPassword;

    @Bind(R.id.user_confirm_password)
    EditText confirmPassword;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View toReturn = inflater.inflate(R.layout.fragment_user_edit_password, container, false);
        ButterKnife.bind(this, toReturn);
        configureView();

        return toReturn;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.user_edit_password_close_button)
    protected void onCloseButton() {
        dismiss();
    }

    @OnClick(R.id.user_edit_password_save_button)
    protected void onSaveButton() {
        if (validatePassword()) {
            dismiss();
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureView() {
        oldPassword.setTypeface(Typeface.DEFAULT);
        oldPassword.setTransformationMethod(new PasswordTransformationMethod());

        newPassword.setTypeface(Typeface.DEFAULT);
        newPassword.setTransformationMethod(new PasswordTransformationMethod());

        confirmPassword.setTypeface(Typeface.DEFAULT);
        confirmPassword.setTransformationMethod(new PasswordTransformationMethod());

        oldPassword.requestFocus();
    }

    private boolean validatePassword() {
        return false;
    }

}
