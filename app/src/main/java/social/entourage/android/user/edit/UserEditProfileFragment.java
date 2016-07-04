package social.entourage.android.user.edit;


import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.User;

public class UserEditProfileFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_profile_fragment";
    public static final int EDIT_UNKNOWN = 0;
    public static final int EDIT_NAME = 1;
    public static final int EDIT_EMAIL = 2;

    private static final String KEY_EDIT_TYPE = "user_edit_type";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Bind(R.id.user_edit_profile_firstname)
    EditText userFirstname;

    @Bind(R.id.user_edit_profile_lastname)
    EditText userLastname;

    @Bind(R.id.user_edit_profile_email)
    EditText userEmail;

    private User user;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditProfileFragment() {
        // Required empty public constructor
    }

    public static UserEditProfileFragment newInstance(int editType) {
        UserEditProfileFragment fragment = new UserEditProfileFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_EDIT_TYPE, editType);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View toReturn = inflater.inflate(R.layout.fragment_user_edit_profile, container, false);
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureView();
    }

    private void configureView() {
        UserEditFragment userEditFragment = (UserEditFragment) getFragmentManager().findFragmentByTag(UserEditFragment.TAG);
        if (userEditFragment == null) {
            return;
        }
        user = userEditFragment.getEditedUser();
        userFirstname.setText(user.getFirstName());
        userLastname.setText(user.getLastName());
        userEmail.setText(user.getEmail());

        Bundle args = getArguments();
        if (args != null) {
            int editType = args.getInt(KEY_EDIT_TYPE, EDIT_UNKNOWN);
            if (editType == EDIT_NAME) {
                userFirstname.requestFocus();
            }
            else if (editType == EDIT_EMAIL) {
                userEmail.requestFocus();
            }
        }
    }

    @OnClick(R.id.user_edit_profile_save)
    protected void onSaveProfile() {
        String firstname = userFirstname.getText().toString().trim();
        String lastname = userLastname.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        if (firstname.length() == 0) {
            displayToast(R.string.user_edit_profile_invalid_firstname);
            userFirstname.requestFocus();
            return;
        }
        if (lastname.length() == 0) {
            displayToast(R.string.user_edit_profile_invalid_lastname);
            userLastname.requestFocus();
            return;
        }
        if (email.length() == 0) {
            displayToast(R.string.user_edit_profile_invalid_email);
            userEmail.requestFocus();
            return;
        }
        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setEmail(email);
        UserEditFragment userEditFragment = (UserEditFragment) getFragmentManager().findFragmentByTag(UserEditFragment.TAG);
        if (userEditFragment != null) {
            userEditFragment.configureView();
        }

        dismiss();
    }

    private void displayToast(@StringRes int stringId) {
        Toast.makeText(getActivity(), stringId, Toast.LENGTH_SHORT).show();
    }

}
