package social.entourage.android.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.login.LoginActivity;

public class UserActivity extends EntourageSecuredActivity {

    @Inject
    UserPresenter presenter;

    @InjectView(R.id.user_photo)
    View userPhoto;

    @InjectView(R.id.user_name)
    TextView userName;

    @InjectView(R.id.user_email)
    TextView userEmail;

    @InjectView(R.id.user_tours_count)
    TextView userTourCount;

    @InjectView(R.id.user_encounters_count)
    TextView userEncountersCount;

    @InjectView(R.id.user_association)
    TextView userAssociation;

    @InjectView(R.id.user_button_confirm_changes)
    Button buttonConfirm;

    @InjectView(R.id.user_button_unsubscribe)
    Button buttonUnsubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);
        configureToolbar();
        ButterKnife.inject(this);

        if (!getAuthenticationController().isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        User user = getAuthenticationController().getUser();
        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());
        userTourCount.setText(getString(R.string.user_tours_count, user.getStats().getTourCount()));
        userEncountersCount.setText(getString(R.string.user_encounters_count, user.getStats().getEncounterCount()));
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

    private void configureToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.user_button_confirm_changes)
    void confirmChanges() {

    }

    @OnClick(R.id.user_button_unsubscribe)
    void unsubscribe() {

    }

}
