package social.entourage.android.authentication.login;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.message.push.RegisterGCMService;

/**
 * Activity providing the login steps
 */
public class LoginActivity extends EntourageActivity implements LoginInformationFragment.OnEntourageInformationFragmentFinish {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final static String ANDROID_DEVICE = "android";
    private final static String KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE";
    private static final int PERMISSIONS_REQUEST_PHONE_STATE = 1;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    LoginInformationFragment informationFragment;

    @Inject
    LoginPresenter loginPresenter;

    /************************
     * Signup View
     ************************/

    @Bind(R.id.login_include_signup)
    View loginSignup;

    @Bind(R.id.login_text_more)
    TextView moreText;

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

    @Bind(R.id.login_block_lost_code_confirmation)
    View confirmationBlock;

    @Bind(R.id.login_text_confirmation)
    TextView codeConfirmation;

    @Bind(R.id.login_button_home)
    Button homeButton;

    /************************
     * Welcome View
     ************************/

    @Bind(R.id.login_include_welcome)
    View loginWelcome;

    @Bind(R.id.login_edit_email_profile)
    EditText profileEmail;

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

        loginLostCode.setVisibility(View.GONE);
        loginWelcome.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.GONE);

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
            return;
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
        FlurryAgent.logEvent(Constants.EVENT_LOGIN_OK);
        startActivity(new Intent(this, DrawerActivity.class));
    }

    public void loginFail() {
        stopLoader();
        FlurryAgent.logEvent(Constants.EVENT_LOGIN_FAILED);
        Toast.makeText(this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
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
    }

    public void stopLoader() {
        loginButton.setText(R.string.login_button_signup);
        loginButton.setEnabled(true);
        receiveCodeButton.setText(R.string.login_button_ask_code);
        receiveCodeButton.setEnabled(true);
        lostCodePhone.setEnabled(true);
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
    }

    void newCodeAsked(User user) {
        stopLoader();
        if (user != null) {
            codeConfirmation.setText(R.string.login_text_lost_code_ok);
        } else {
            codeConfirmation.setText(R.string.login_text_lost_code_ko);
        }
        enterCodeBlock.setVisibility(View.GONE);
        confirmationBlock.setVisibility(View.VISIBLE);
    }

    /************************
     * Welcome View
     ************************/

    @OnClick(R.id.login_user_photo)
    void addPhoto() {

    }

    //TODO: remove this when the tutorial content is ready
    @OnClick(R.id.login_button_go)
    void finishTutorial() {
        loginPresenter.updateUserEmail(profileEmail.getText().toString());
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(KEY_TUTORIAL_DONE, true).apply();
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
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(KEY_TUTORIAL_DONE, true).apply();
        startActivity(new Intent(this, DrawerActivity.class));
    }
    */
}
