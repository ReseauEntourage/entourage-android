package social.entourage.android.authentication.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.message.push.RegisterGCMService;

/**
 * Activity providing the login steps
 */
@SuppressWarnings("WeakerAccess")
public class LoginActivity extends EntourageActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String FACEBOOK = "https://www.facebook.com/EntourageReseauCivique";
    private final String TWITTER = "https://twitter.com/R_Entour";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    LoginPresenter loginPresenter;

    /************************
     * Signup View
     ************************/

    @InjectView(R.id.login_signup)
    View loginSignup;

    @InjectView(R.id.login_more_text)
    TextView moreText;

    @InjectView(R.id.edittext_phone_number)
    EditText phoneEditText;

    @InjectView(R.id.edittext_password)
    EditText passwordEditText;

    @InjectView(R.id.button_login)
    Button loginButton;

    @InjectView(R.id.login_button_lost_password)
    Button lostCodeButton;

    /************************
     * More Information View
     ************************/

    @InjectView(R.id.login_more_information)
    View loginMoreInformation;

    @InjectView(R.id.login_button_facebook)
    ImageButton facebookButton;

    @InjectView(R.id.login_button_twitter)
    ImageButton twitterButton;

    @InjectView(R.id.login_form_email)
    EditText emailEdiText;

    @InjectView(R.id.login_button_newsletter)
    Button newsletterButton;

    /************************
     * Lost Code View
     ************************/

    @InjectView(R.id.login_lost_code)
    View loginLostCode;

    @InjectView(R.id.login_welcome_mail)
    EditText lostCodePhone;

    @InjectView(R.id.login_button_add_picture)
    Button receiveCodeButton;

    @InjectView(R.id.login_welcome_block)
    View enterCodeBlock;

    @InjectView(R.id.login_lost_code_confirmation_block)
    View confirmationBlock;

    @InjectView(R.id.login_text_confirmation)
    TextView codeConfirmation;

    @InjectView(R.id.login_button_home)
    Button homeButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        loginMoreInformation.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.GONE);

        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String phoneNumber = manager.getLine1Number();
        if (phoneNumber != null) {
            //phoneEditText.setText(phoneNumber);
        }
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
        if (loginMoreInformation.getVisibility() == View.VISIBLE) {
            emailEdiText.setText("");
            loginMoreInformation.setVisibility(View.GONE);
            loginSignup.setVisibility(View.VISIBLE);
        }
        else if (loginLostCode.getVisibility() == View.VISIBLE) {
            lostCodePhone.setText("");
            loginLostCode.setVisibility(View.GONE);
            loginSignup.setVisibility(View.VISIBLE);
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

    public void displayWrongFormat() {
        Toast.makeText(this, getString(R.string.login_number_invalid_format), Toast.LENGTH_SHORT).show();
    }

    public void startLoader() {
        loginButton.setText(R.string.loading_button_text);
        loginButton.setEnabled(false);
    }

    public void resetLoginButton() {
        loginButton.setText(R.string.login_button_connection_text);
        loginButton.setEnabled(true);
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    /************************
     * Signup View
     ************************/

    @OnClick(R.id.login_more_text)
    void onAskMore() {
        loginSignup.setVisibility(View.GONE);
        loginMoreInformation.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.button_login)
    void onLoginClick() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        loginPresenter.login(
                phoneEditText.getText().toString(),
                passwordEditText.getText().toString(),
                getString(R.string.login_device_type),
                sharedPreferences.getString(RegisterGCMService.KEY_REGISTRATION_ID, null)
        );
    }

    @OnClick(R.id.login_button_lost_password)
    void onLostCodeClick() {
        loginSignup.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.VISIBLE);
    }

    /************************
     * More Information View
     ************************/

    @OnClick(R.id.login_button_facebook)
    void facebookButton() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK));
        startActivity(browserIntent);
    }

    @OnClick(R.id.login_button_twitter)
    void twitterButton() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER));
        startActivity(browserIntent);
    }

    @OnClick(R.id.login_button_newsletter)
    void newsletterButton() {
        emailEdiText.setText("");
        loginPresenter.signupForNewsletter(emailEdiText.getText().toString());
        loginMoreInformation.setVisibility(View.GONE);
        loginSignup.setVisibility(View.VISIBLE);
    }

    /************************
     * Lost Code View
     ************************/

    @OnClick(R.id.login_button_add_picture)
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

}
