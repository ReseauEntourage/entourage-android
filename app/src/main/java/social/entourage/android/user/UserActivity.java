package social.entourage.android.user;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.login.LoginActivity;

@SuppressWarnings("WeakerAccess")
public class UserActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    UserPresenter presenter;

    @InjectView(R.id.user_photo)
    ImageView userPhoto;

    @InjectView(R.id.user_name)
    TextView userName;

    @InjectView(R.id.user_email)
    TextView userEmail;

    @InjectView(R.id.user_tours_count)
    TextView userTourCount;

    @InjectView(R.id.user_encounters_count)
    TextView userEncountersCount;

    @InjectView(R.id.organization_photo)
    ImageView organizationPhoto;

    @InjectView(R.id.user_organization)
    TextView userOrganization;

    @InjectView(R.id.user_edit_email)
    EditText userEditEmail;

    @InjectView(R.id.user_edit_code)
    EditText userEditCode;

    @InjectView(R.id.user_edit_confirmation)
    EditText userEditConfirmation;

    @InjectView(R.id.user_button_confirm_changes)
    Button buttonConfirmChanges;

    @InjectView(R.id.user_button_unsubscribe)
    Button buttonUnsubscribe;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);
        configureToolbar();
        ButterKnife.inject(this);

        if (!getAuthenticationController().isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        Resources res = getResources();
        User user = getAuthenticationController().getUser();
        int tourCount = user.getStats().getTourCount();
        int encountersCount = user.getStats().getEncounterCount();

        Picasso.with(this).load(R.drawable.ic_user_photo)
                .transform(new CropCircleTransformation())
                .into(userPhoto);

        Picasso.with(this).load(R.drawable.ic_croix_rouge)
                .transform(new CropCircleTransformation())
                .into(organizationPhoto);

        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());
        userTourCount.setText(res.getQuantityString(R.plurals.tours_count, tourCount, tourCount));
        userEncountersCount.setText(res.getQuantityString(R.plurals.encounters_count, encountersCount, encountersCount));
        userOrganization.setText(user.getOrganization().getName());
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

    private void configureToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
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

    }

}
