package social.entourage.android.authentication.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
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
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.message.push.RegisterGCMService;

/**
 * Activity providing the login steps
 */
@SuppressWarnings("WeakerAccess")
public class LoginActivity extends EntourageActivity implements LoginInformationFragment.OnInformationFragmentFinish {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String ANDROID_DEVICE = "android";
    private final String KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    LoginInformationFragment informationFragment;

    @Inject
    LoginPresenter loginPresenter;

    /************************
     * Signup View
     ************************/

    @InjectView(R.id.login_include_signup)
    View loginSignup;

    @InjectView(R.id.login_text_more)
    TextView moreText;

    @InjectView(R.id.login_edit_phone)
    EditText phoneEditText;

    @InjectView(R.id.login_edit_code)
    EditText passwordEditText;

    @InjectView(R.id.login_button_signup)
    Button loginButton;

    @InjectView(R.id.login_text_lost_code)
    TextView lostCodeText;

    /************************
     * Lost Code View
     ************************/

    @InjectView(R.id.login_include_lost_code)
    View loginLostCode;

    @InjectView(R.id.login_edit_phone_lost_code)
    EditText lostCodePhone;

    @InjectView(R.id.login_button_ask_code)
    Button receiveCodeButton;

    @InjectView(R.id.login_block_code_form)
    View enterCodeBlock;

    @InjectView(R.id.login_block_lost_code_confirmation)
    View confirmationBlock;

    @InjectView(R.id.login_text_confirmation)
    TextView codeConfirmation;

    @InjectView(R.id.login_button_home)
    Button homeButton;

    /************************
     * Welcome View
     ************************/

    @InjectView(R.id.login_include_welcome)
    View loginWelcome;

    @InjectView(R.id.login_edit_email_profile)
    EditText profileEmail;

    @InjectView(R.id.login_user_photo)
    ImageView profilePhoto;

    @InjectView(R.id.login_button_go)
    Button goButton;

    /************************
     * Tutorial View
     ************************/

    @InjectView(R.id.login_include_tutorial)
    View loginTutorial;

    @InjectView(R.id.login_button_finish_tutorial)
    Button finishTutorial;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        loginLostCode.setVisibility(View.GONE);
        loginWelcome.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.GONE);

        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String phoneNumber = manager.getLine1Number();
        if (phoneNumber != null) {
            phoneEditText.setText(phoneNumber);
        }

        Picasso.with(this).load(R.drawable.ic_user_photo)
                .transform(new CropCircleTransformation())
                .into(profilePhoto);
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerLoginComponent.builder()
                .entourageComponent(entourageComponent)
                .loginModule(new LoginModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onBackPressed() {
        /*
        if (loginMoreInformation.getVisibility() == View.VISIBLE) {
            emailEditText.setText("");
            loginMoreInformation.setVisibility(View.GONE);
            loginSignup.setVisibility(View.VISIBLE);
        }
        else
        */
        if (loginLostCode.getVisibility() == View.VISIBLE) {
            lostCodePhone.setText("");
            loginLostCode.setVisibility(View.GONE);
            loginSignup.setVisibility(View.VISIBLE);
        }
        else if (loginTutorial.getVisibility() == View.VISIBLE) {
            loginTutorial.setVisibility(View.GONE);
            loginWelcome.setVisibility(View.VISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startMapActivity() {
        resetLoginButton();
        startActivity(new Intent(this, DrawerActivity.class));
    }

    public void loginFail() {
        resetLoginButton();
        Toast.makeText(this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void startLoader() {
        loginButton.setText(R.string.button_loading);
        loginButton.setEnabled(false);
    }

    public void resetLoginButton() {
        loginButton.setText(R.string.login_button_signup);
        loginButton.setEnabled(true);
    }

    public void launchFillInProfileView(User user) {
        loginSignup.setVisibility(View.GONE);
        loginWelcome.setVisibility(View.VISIBLE);
        if (user.getEmail() != null) {
            profileEmail.setText(user.getEmail());
        }
    }

    // ----------------------------------
    // INTERFACES CALLBACKS
    // ----------------------------------

    @Override
    public void closeInformationFragment() {
        if (informationFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().remove(informationFragment).commit();
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    /************************
     * Signup View
     ************************/

    @OnClick(R.id.login_text_more)
    void onAskMore() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        informationFragment = new LoginInformationFragment();
        informationFragment.show(fragmentManager, "fragment_login_information");
    }

    @OnClick(R.id.login_button_signup)
    void onLoginClick() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        loginPresenter.login(
                phoneEditText.getText().toString(),
                passwordEditText.getText().toString(),
                ANDROID_DEVICE,
                sharedPreferences.getString(RegisterGCMService.KEY_REGISTRATION_ID, null),
                sharedPreferences.getBoolean(KEY_TUTORIAL_DONE, false)
        );
    }

    @OnClick(R.id.login_text_lost_code)
    void onLostCodeClick() {
        loginSignup.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.VISIBLE);
    }

    /************************
     * Lost Code View
     ************************/

    @OnClick(R.id.login_button_ask_code)
    void sendNewCode() {
        loginPresenter.sendNewCode(lostCodePhone.getText().toString());
        /**
         * here :
         * if code sent : R.string.login_text_code_ok
         * else         : R.string.login_text_code_ko
         */
        codeConfirmation.setText(R.string.login_text_lost_code_ko);
        enterCodeBlock.setVisibility(View.GONE);
        confirmationBlock.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.login_button_home)
    void returnHome() {
        lostCodePhone.setText("");
        confirmationBlock.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.GONE);
        loginSignup.setVisibility(View.VISIBLE);
    }

    /************************
     * Welcome View
     ************************/

    @OnClick(R.id.login_user_photo)
    void addPhoto() {

    }

    @OnClick(R.id.login_button_go)
    void startTutorial() {
        loginPresenter.updateUserEmail(profileEmail.getText().toString());
        loginWelcome.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.VISIBLE);
    }

    /************************
     * Tutorial View
     ************************/

    @OnClick(R.id.login_button_finish_tutorial)
    void finishTutorial() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(KEY_TUTORIAL_DONE, true).commit();
        startActivity(new Intent(this, DrawerActivity.class));
    }
}
