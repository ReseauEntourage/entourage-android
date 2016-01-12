package social.entourage.android.user;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import butterknife.OnTouch;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.UserChoiceEvent;

public class UserActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String TERMS_AND_CONDITIONS_URL= "http://www.entourage.social/cgu";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);
        configureToolbar();
        ButterKnife.bind(this);

        if (!getAuthenticationController().isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        FlurryAgent.logEvent(Constants.EVENT_PROFILE_FROM_MENU);
        BusProvider.getInstance().register(this);

        configureView();

    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerUserComponent.builder()
                .entourageComponent(entourageComponent)
                .userModule(new UserModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, DrawerActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.activity_display_user_title);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DrawerActivity.class));
        super.onBackPressed();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configureView() {
        Resources res = getResources();
        User user = getAuthenticationController().getUser();
        int tourCount = user.getStats().getTourCount();
        int encountersCount = user.getStats().getEncounterCount();

        Picasso.with(this).load(R.drawable.ic_user_photo)
                .transform(new CropCircleTransformation())
                .into(userPhoto);

        Picasso.with(this).load(R.drawable.ic_organisation_notfound)
                .transform(new CropCircleTransformation())
                .into(organizationPhoto);

        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());
        userTourCount.setText(res.getQuantityString(R.plurals.tours_count, tourCount, tourCount));
        userEncountersCount.setText(res.getQuantityString(R.plurals.encounters_count, encountersCount, encountersCount));
        userOrganization.setText(user.getOrganization().getName());
        if (getAuthenticationController().isUserToursOnly()) {
            userToursSwitch.setChecked(true);
        }
        userEditEmail.setText(user.getEmail());
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
            getAuthenticationController().saveUserToursOnly(true);
            BusProvider.getInstance().post(new UserChoiceEvent(true));
        } else {
            getAuthenticationController().saveUserToursOnly(false);
            BusProvider.getInstance().post(new UserChoiceEvent(false));
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
                Toast.makeText(this, "Erreur de confirmation du code", Toast.LENGTH_SHORT).show();
            }
        }

        if (email != null || code != null) {
            presenter.updateUser(email, code);
        }
    }

    @OnClick(R.id.user_button_unsubscribe)
    void unsubscribe() {
        Snackbar.make(getCurrentFocus(), getResources().getString(R.string.unsubscribe_error), Snackbar.LENGTH_LONG).show();
    }

    @OnClick(R.id.user_terms_and_conditions)
    void displayTermsAndConditions() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_AND_CONDITIONS_URL));
        startActivity(browserIntent);
    }
}
