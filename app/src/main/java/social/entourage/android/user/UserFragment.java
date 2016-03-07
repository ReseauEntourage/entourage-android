package social.entourage.android.user;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.api.tape.Events.*;

public class UserFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String TERMS_AND_CONDITIONS_URL= "http://www.entourage.social/cgu";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private View toReturn;

    @Inject
    UserPresenter presenter;

    @Bind(R.id.user_photo)
    ImageView userPhoto;

    @Bind(R.id.user_name)
    TextView userName;

    @Bind(R.id.user_email)
    TextView userEmail;

    @Bind(R.id.user_tours_count)
    TextView userTourCount;

    @Bind(R.id.user_encounters_count)
    TextView userEncountersCount;

    @Bind(R.id.user_tours_switch)
    Switch userToursSwitch;

    @Bind(R.id.organization_photo)
    ImageView organizationPhoto;

    @Bind(R.id.user_organization)
    TextView userOrganization;

    @Bind(R.id.user_edit_email)
    EditText userEditEmail;

    @Bind(R.id.user_edit_code)
    EditText userEditCode;

    @Bind(R.id.user_edit_confirmation)
    EditText userEditConfirmation;

    @Bind(R.id.user_button_confirm_changes)
    Button buttonConfirmChanges;

    @Bind(R.id.user_button_unsubscribe)
    Button buttonUnsubscribe;

    @Bind(R.id.user_terms_and_conditions)
    TextView termsAndConditions;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static UserFragment newInstance(int userId) {
        UserFragment userFragment = new UserFragment();
        Bundle args = new Bundle();
        args.putSerializable(User.KEY_USER_ID, userId);
        userFragment.setArguments(args);
        return userFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_user, container, false);
        }
        ButterKnife.bind(this, toReturn);
        FlurryAgent.logEvent(Constants.EVENT_PROFILE_FROM_MENU);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        configureView();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerUserComponent.builder()
                .entourageComponent(entourageComponent)
                .userModule(new UserModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.activity_display_user_title);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureView() {
        if (getActivity() != null) {
            Resources res = getResources();
            User user = presenter.getUser();
            int tourCount = user.getStats().getTourCount();
            int encountersCount = user.getStats().getEncounterCount();

            Picasso.with(getActivity()).load(R.drawable.ic_user_photo)
                    .transform(new CropCircleTransformation())
                    .into(userPhoto);

            Picasso.with(getActivity()).load(R.drawable.ic_organisation_notfound)
                    .transform(new CropCircleTransformation())
                    .into(organizationPhoto);

            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());
            userTourCount.setText(res.getQuantityString(R.plurals.tours_count, tourCount, tourCount));
            userEncountersCount.setText(res.getQuantityString(R.plurals.encounters_count, encountersCount, encountersCount));
            userOrganization.setText(user.getOrganization().getName());
            if (presenter.isUserToursOnly()) {
                userToursSwitch.setChecked(true);
            }
            userEditEmail.setText(user.getEmail());
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void displayToast(String message) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateView(String email) {
        resetLoginButton();
        userEmail.setText(email);
        userEditEmail.setText("");
        userEditCode.setText("");
        userEditConfirmation.setText("");
        userEditEmail.setText(email);
    }

    public void startLoader() {
        buttonConfirmChanges.setText(R.string.button_loading);
        buttonConfirmChanges.setEnabled(false);
    }

    public void resetLoginButton() {
        buttonConfirmChanges.setText(R.string.user_button_confirm_changes);
        buttonConfirmChanges.setEnabled(true);
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.user_tours_switch)
    void setUsersToursOnly() {
        if (userToursSwitch.isChecked()) {
            presenter.saveUserToursOnly(true);
            BusProvider.getInstance().post(new OnUserChoiceEvent(true));
        } else {
            presenter.saveUserToursOnly(false);
            BusProvider.getInstance().post(new OnUserChoiceEvent(false));
        }
    }

    @OnClick(R.id.user_button_confirm_changes)
    void confirmChanges() {
        String emailEdit = userEditEmail.getText().toString();
        String codeEdit = userEditCode.getText().toString();
        String confirmationEdit = userEditConfirmation.getText().toString();

        String email = null;
        String code = null;

        if (!emailEdit.equals("")) {
            email = emailEdit;
        }

        if ((!codeEdit.equals("") && codeEdit.length() == 6) &&
                (!confirmationEdit.equals("") && confirmationEdit.length() == 6)) {
            if (codeEdit.equals(confirmationEdit)) {
                code = codeEdit;
            } else {
                displayToast("Erreur de confirmation du code");
            }
        }

        if (email != null || code != null) {
            presenter.updateUser(email, code);
        }
    }

    @OnClick(R.id.user_button_unsubscribe)
    void unsubscribe() {
        if (getActivity() != null) {
            Snackbar.make(getActivity().getCurrentFocus(), getResources().getString(R.string.unsubscribe_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.user_terms_and_conditions)
    void displayTermsAndConditions() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_AND_CONDITIONS_URL));
        startActivity(browserIntent);
    }

}
