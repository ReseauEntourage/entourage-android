package social.entourage.android.authentication.login;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.util.HashSet;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.message.push.RegisterGCMService;
import social.entourage.android.view.HtmlTextView;

/**
 * Activity providing the login steps
 */
public class LoginActivity extends EntourageActivity implements LoginInformationFragment.OnEntourageInformationFragmentFinish {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int PERMISSIONS_REQUEST_PHONE_STATE = 1;
    public final static String KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String loggedPhoneNumber;

    LoginInformationFragment informationFragment;

    private View previousView = null;

    @Inject
    LoginPresenter loginPresenter;

    /************************
     * Signup View
     ************************/

    @Bind(R.id.login_include_signup)
    View loginSignup;

    @Bind(R.id.login_edit_phone)
    EditText phoneEditText;

    @Bind(R.id.login_edit_code)
    EditText passwordEditText;

    @Bind(R.id.login_button_signup)
    Button loginButton;

    @Bind(R.id.login_text_lost_code)
    TextView lostCodeText;

    /************************
     * Lost Code View
     ************************/

    @Bind(R.id.login_include_lost_code)
    View loginLostCode;

    @Bind(R.id.login_edit_phone_lost_code)
    EditText lostCodePhone;

    @Bind(R.id.login_button_ask_code)
    Button receiveCodeButton;

    @Bind(R.id.login_block_code_form)
    View enterCodeBlock;

    @Bind(R.id.login_block_lost_code_button)
    View lostCodeButtonBlock;

    @Bind(R.id.login_block_lost_code_confirmation)
    View confirmationBlock;

    @Bind(R.id.login_text_confirmation)
    HtmlTextView codeConfirmation;

    @Bind(R.id.login_button_home)
    Button homeButton;

    /************************
     * Welcome View
     ************************/

    @Bind(R.id.login_include_welcome)
    View loginWelcome;

    @Bind(R.id.login_edit_email_profile)
    EditText profileEmail;

    @Bind(R.id.login_edit_name_profile)
    EditText profileName;

    @Bind(R.id.login_user_photo)
    ImageView profilePhoto;

    @Bind(R.id.login_button_go)
    Button goButton;

    /************************
     * Tutorial View
     ************************/

    @Bind(R.id.login_include_tutorial)
    View loginTutorial;

    @Bind(R.id.login_button_finish_tutorial)
    Button finishTutorial;

    /************************
     * Startup View
     ************************/

    @Bind(R.id.login_include_startup)
    View loginStartup;

    /************************
     * Newsletter subscription View
     ************************/

    @Bind(R.id.login_include_newsletter)
    View loginNewsletter;

    @Bind(R.id.login_newsletter_button)
    Button newsletterButton;

    @Bind(R.id.login_newsletter_email)
    TextView newsletterEmail;

    /************************
     * Verify Code view
     ************************/

    @Bind(R.id.login_include_verify_code)
    View loginVerifyCode;

    @Bind(R.id.login_button_verify_code)
    Button verifyCodeButton;

    @Bind(R.id.login_verify_code_code)
    TextView receivedCode;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkPermissions();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        FlurryAgent.logEvent(Constants.EVENT_LOGIN_START);

        loginSignup.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.GONE);
        loginVerifyCode.setVisibility(View.GONE);
        loginWelcome.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.GONE);

        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        /*
        Picasso.with(this).load(R.drawable.ic_user_photo)
                .transform(new CropCircleTransformation())
                .into(profilePhoto);
                */
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_PHONE_STATE) {
            for (int index = 0; index < permissions.length; index++) {
                if (permissions[index].equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE) && grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (loginSignup.getVisibility() == View.VISIBLE) {
            phoneEditText.setText("");
            passwordEditText.setText("");
            loginSignup.setVisibility(View.GONE);
            loginStartup.setVisibility(View.VISIBLE);
        }
        else if (loginLostCode.getVisibility() == View.VISIBLE) {
            lostCodePhone.setText("");
            loginLostCode.setVisibility(View.GONE);
            loginSignup.setVisibility(View.VISIBLE);
            showKeyboard(phoneEditText);
        }
        else if (loginTutorial.getVisibility() == View.VISIBLE) {
            loginTutorial.setVisibility(View.GONE);
            loginWelcome.setVisibility(View.VISIBLE);
        }
        else if (loginNewsletter.getVisibility() == View.VISIBLE && previousView != null) {
            newsletterEmail.setText("");
            loginNewsletter.setVisibility(View.GONE);
            previousView.setVisibility(View.VISIBLE);
            if (previousView == loginSignup) {
                showKeyboard(phoneEditText);
            }
        }
        else if (loginVerifyCode.getVisibility() == View.VISIBLE) {
            showLostCodeScreen();
        }
        else {
            super.onBackPressed();
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    @TargetApi(23)
    private void checkPermissions() {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.login_permission_title)
                        .setMessage(R.string.login_permission_description)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{ Manifest.permission.READ_PHONE_STATE }, PERMISSIONS_REQUEST_PHONE_STATE);
                            }
                        }).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_PHONE_STATE);
            }
        } else {
            TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String phoneNumber = manager.getLine1Number();
            if (phoneNumber != null) {
                phoneEditText.setText(phoneNumber);
            }
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startMapActivity() {
        stopLoader();
        hideKeyboard();
        FlurryAgent.logEvent(Constants.EVENT_LOGIN_OK);
        startActivity(new Intent(this, DrawerActivity.class));
    }

    public void loginFail(boolean networkError) {
        stopLoader();
        FlurryAgent.logEvent(Constants.EVENT_LOGIN_FAILED);
        //displayToast(getString(R.string.login_fail));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.login_login_error_title)
            .setMessage(networkError ? R.string.login_login_error_network : R.string.login_login_error_invalid_credentials)
            .setPositiveButton(R.string.login_login_error_retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                }
            });
        builder.create().show();
    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void startLoader() {
        loginButton.setText(R.string.button_loading);
        loginButton.setEnabled(false);
        receiveCodeButton.setText(R.string.button_loading);
        receiveCodeButton.setEnabled(false);
        lostCodePhone.setEnabled(false);
        newsletterButton.setText(R.string.button_loading);
        newsletterButton.setEnabled(false);
        verifyCodeButton.setText(R.string.button_loading);
        verifyCodeButton.setEnabled(false);
    }

    public void stopLoader() {
        loginButton.setText(R.string.login_button_signup);
        loginButton.setEnabled(true);
        receiveCodeButton.setText(R.string.login_button_ask_code);
        receiveCodeButton.setEnabled(true);
        lostCodePhone.setEnabled(true);
        newsletterButton.setText(R.string.login_button_newsletter);
        newsletterButton.setEnabled(true);
        verifyCodeButton.setText(R.string.login_button_verify_code);
        verifyCodeButton.setEnabled(true);
    }

    public void launchFillInProfileView(String phoneNumber, User user) {
        loggedPhoneNumber = phoneNumber;
        loginSignup.setVisibility(View.GONE);
        loginVerifyCode.setVisibility(View.GONE);
        loginWelcome.setVisibility(View.VISIBLE);
        if (user.getEmail() != null) {
            profileEmail.setText(user.getEmail());
        }
        profileEmail.requestFocus();
    }

    // ----------------------------------
    // INTERFACES CALLBACKS
    // ----------------------------------

    @Override
    public void closeEntourageInformationFragment() {
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

    /*
    @OnClick(R.id.login_text_more)
    void onAskMore() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        informationFragment = new LoginInformationFragment();
        informationFragment.show(fragmentManager, "fragment_login_information");
    }
    */

    @OnClick(R.id.login_button_signup)
    void onLoginClick() {
        loginPresenter.login(
                phoneEditText.getText().toString(),
                passwordEditText.getText().toString());
    }

    @OnClick(R.id.login_text_lost_code)
    void onLostCodeClick() {
        hideKeyboard();
        loginSignup.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.VISIBLE);
        lostCodeButtonBlock.setVisibility(View.VISIBLE);
        confirmationBlock.setVisibility(View.GONE);

        showKeyboard(lostCodePhone);
    }

    @OnClick(R.id.login_welcome_more)
    void onMoreClick() {
        loginSignup.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.VISIBLE);
        previousView = loginSignup;
        showKeyboard(newsletterEmail);
    }

    /************************
     * Lost Code View
     ************************/

    @OnClick(R.id.login_lost_code_close)
    void lostCodeClose() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_ask_code)
    void sendNewCode() {
        startLoader();
        loginPresenter.sendNewCode(lostCodePhone.getText().toString());
        FlurryAgent.logEvent(Constants.EVENT_LOGIN_SEND_NEW_CODE);
    }

    @OnClick(R.id.login_button_home)
    void returnHome() {
        lostCodePhone.setText("");
        confirmationBlock.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.GONE);
        loginSignup.setVisibility(View.VISIBLE);
        showKeyboard(phoneEditText);
    }

    void newCodeAsked(User user) {
        stopLoader();
        if (user != null) {
            if (loginLostCode.getVisibility() == View.VISIBLE) {
                loginLostCode.setVisibility(View.GONE);
                loginVerifyCode.setVisibility(View.VISIBLE);
            }
            else {
                displayToast(getString(R.string.login_text_lost_code_ok));
            }
        } else {
            if (loginLostCode.getVisibility() == View.VISIBLE) {
                //codeConfirmation.setText(R.string.login_text_lost_code_ko);
                codeConfirmation.setHtmlString(R.string.login_text_lost_code_ko_html);
                enterCodeBlock.setVisibility(View.GONE);
                lostCodeButtonBlock.setVisibility(View.GONE);
                confirmationBlock.setVisibility(View.VISIBLE);
            }
        }
    }

    /************************
     * Welcome View
     ************************/

    @OnClick(R.id.login_user_photo)
    void addPhoto() {

    }

    @OnClick(R.id.login_button_go)
    void finishTutorial() {
        loginPresenter.updateUserEmail(profileEmail.getText().toString());
        //show the notifications dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.login_permission_notification_description)
            .setPositiveButton(R.string.login_permission_notification_accept,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            showNewsfeedScreenWithNotifications(true);
                        }
                    })
            .setNegativeButton(R.string.login_permission_notification_refuse,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            showNewsfeedScreenWithNotifications(false);
                        }
                    })
            .create().show();
    }

    private void showNewsfeedScreenWithNotifications(boolean enabled) {
        //remember the choice
        final SharedPreferences notificationsPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = notificationsPreferences.edit();
        editor.putBoolean(RegisterGCMService.KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.commit();
        //set the tutorial as done
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        HashSet<String>loggedNumbers = (HashSet) sharedPreferences.getStringSet(KEY_TUTORIAL_DONE, new HashSet<String>());
        loggedNumbers.add(loggedPhoneNumber);
        sharedPreferences.edit().putStringSet(KEY_TUTORIAL_DONE, loggedNumbers).commit();
        //start the activity
        startActivity(new Intent(this, DrawerActivity.class));
    }

    /*
    TODO: put this back when the tutorial content is ready
    @OnClick(R.id.login_button_go)
    void startTutorial() {
        FlurryAgent.logEvent(Constants.EVENT_TUTORIAL_START);
        loginPresenter.updateUserEmail(profileEmail.getText().toString());
        loginWelcome.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.VISIBLE);
    }
    */

    /************************
     * Tutorial View
     ************************/

    /*
    TODO: put this back when the tutorial content is ready
    @OnClick(R.id.login_button_finish_tutorial)
    void finishTutorial() {
        FlurryAgent.logEvent(Constants.EVENT_TUTORIAL_END);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(KEY_TUTORIAL_DONE, true).apply();
        startActivity(new Intent(this, DrawerActivity.class));
    }
    */

    /************************
     * Startup View
     ************************/

    @OnClick(R.id.login_button_login)
    void showLoginScreen() {
        loginStartup.setVisibility(View.GONE);
        loginSignup.setVisibility(View.VISIBLE);
        showKeyboard(phoneEditText);
    }

    @OnClick(R.id.login_button_register)
    void showNewsletterScreen() {
        loginStartup.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.VISIBLE);
        previousView = loginStartup;
        showKeyboard(newsletterEmail);
    }

    /************************
     * Newsletter View
     ************************/

    @OnClick(R.id.login_newsletter_close)
    void newsletterClose() {
        hideKeyboard();
        onBackPressed();
    }

    @OnClick(R.id.login_newsletter_button)
    void newsletterSubscribe() {
        hideKeyboard();
        startLoader();
        loginPresenter.subscribeToNewsletter(newsletterEmail.getText().toString());
    }

    void newsletterResult(boolean success) {
        stopLoader();
        if (success) {
            displayToast(getString(R.string.login_text_newsletter_success));
            onBackPressed();
        }
        else {
            displayToast(getString(R.string.login_text_newsletter_fail));
        }
    }

    /************************
     * Verify Code View
     ************************/

    @OnClick(R.id.login_code_sent_close)
    void verifyCodeClose() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_verify_code)
    void verifyCode() {
        loginPresenter.login(
                lostCodePhone.getText().toString(),
                receivedCode.getText().toString()
        );
    }

    @OnClick(R.id.login_verify_code_resend)
    void resendCode() {
        sendNewCode();
    }

    @OnClick(R.id.login_verify_code_back)
    void showLostCodeScreen() {
        hideKeyboard();
        receivedCode.setText("");
        loginVerifyCode.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.VISIBLE);
        showKeyboard(lostCodePhone);
    }

}
