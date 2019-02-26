package social.entourage.android.authentication.login;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.PermissionChecker;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.HashSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.login.register.OnRegisterUserListener;
import social.entourage.android.authentication.login.register.RegisterNumberFragment;
import social.entourage.android.authentication.login.register.RegisterSMSCodeFragment;
import social.entourage.android.authentication.login.register.RegisterWelcomeFragment;
import social.entourage.android.authentification.login.LoginPresenter;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.map.permissions.NoLocationPermissionFragment;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.Utils;
import social.entourage.android.user.AvatarUploadPresenter;
import social.entourage.android.user.AvatarUploadView;
import social.entourage.android.user.edit.UserEditActionZoneFragment;
import social.entourage.android.user.edit.photo.PhotoChooseInterface;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.edit.photo.PhotoEditFragment;
import social.entourage.android.view.CountryCodePicker.CountryCodePicker;
import social.entourage.android.view.HtmlTextView;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static social.entourage.android.EntourageApplication.KEY_TUTORIAL_DONE;

/**
 * Activity providing the login steps
 */
public class LoginActivity extends EntourageActivity
        implements LoginInformationFragment.OnEntourageInformationFragmentFinish, OnRegisterUserListener, PhotoChooseInterface, UserEditActionZoneFragment.FragmentListener, AvatarUploadView {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    private static final String VERSION = "Version : ";

    private static final int PERMISSIONS_REQUEST_LOCATION = 1;

    public final static int LOGIN_ERROR_UNAUTHORIZED = -1;
    public final static int LOGIN_ERROR_INVALID_PHONE_FORMAT = -2;
    public final static int LOGIN_ERROR_UNKNOWN = -9998;
    public final static int LOGIN_ERROR_NETWORK = -9999;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String loggedPhoneNumber;

    LoginInformationFragment informationFragment;

    private View previousView = null;

    @Inject
    LoginPresenter loginPresenter;

    @Inject
    AvatarUploadPresenter avatarUploadPresenter;

    private User onboardingUser;

    /************************
     * Signin View
     ************************/

    @BindView(R.id.login_include_signin)
    View loginSignin;

    @BindView(R.id.login_ccp)
    CountryCodePicker countryCodePicker;

    @BindView(R.id.login_edit_phone)
    EditText phoneEditText;

    @BindView(R.id.login_edit_code)
    EditText passwordEditText;

    @BindView(R.id.login_button_signup)
    Button loginButton;

    @BindView(R.id.login_text_lost_code)
    TextView lostCodeText;

    /************************
     * Lost Code View
     ************************/

    @BindView(R.id.login_include_lost_code)
    View loginLostCode;

    @BindView(R.id.login_lost_code_ccp)
    CountryCodePicker lostCodeCountryCodePicker;

    @BindView(R.id.login_edit_phone_lost_code)
    EditText lostCodePhone;

    @BindView(R.id.login_button_ask_code)
    Button receiveCodeButton;

    @BindView(R.id.login_block_lost_code_start)
    View enterCodeBlock;

    @BindView(R.id.login_block_lost_code_confirmation)
    View confirmationBlock;

    @BindView(R.id.login_text_confirmation)
    HtmlTextView codeConfirmation;

    @BindView(R.id.login_button_home)
    Button homeButton;

    /************************
     * Welcome View
     ************************/

    @BindView(R.id.login_include_email)
    View loginEmail;

    @BindView(R.id.login_edit_email_profile)
    EditText profileEmail;

    //@BindView(R.id.login_edit_name_profile)
    //EditText profileName;

    @BindView(R.id.login_user_photo)
    ImageView profilePhoto;

    @BindView(R.id.login_button_go)
    FloatingActionButton goButton;

    /************************
     * Enter Name View
     ************************/

    @BindView(R.id.login_include_name)
    View loginNameView;

    @BindView(R.id.login_name_firstname)
    EditText firstnameEditText;

    @BindView(R.id.login_name_lastname)
    EditText lastnameEditText;

    @BindView(R.id.login_name_go_button)
    FloatingActionButton nameGoButton;

    /************************
     * Tutorial View
     ************************/

    @BindView(R.id.login_include_tutorial)
    View loginTutorial;

    @BindView(R.id.login_button_finish_tutorial)
    Button finishTutorial;

    /************************
     * Startup View
     ************************/

    @BindView(R.id.login_include_startup)
    View loginStartup;

    /************************
     * Newsletter subscription View
     ************************/

    @BindView(R.id.login_include_newsletter)
    View loginNewsletter;

    @BindView(R.id.login_newsletter_button)
    Button newsletterButton;

    @BindView(R.id.login_newsletter_email)
    TextView newsletterEmail;

    /************************
     * Verify Code view
     ************************/

    @BindView(R.id.login_include_verify_code)
    View loginVerifyCode;

    @BindView(R.id.login_button_verify_code)
    View verifyCodeButton;

    @BindView(R.id.login_verify_code_code)
    TextView receivedCode;

    /************************
     * Notifications Permissions View
     ************************/

    @BindView(R.id.login_include_notifications)
    View loginNotificationsView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkPermissions();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_01);

        loginSignin.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.GONE);
        loginVerifyCode.setVisibility(View.GONE);
        loginEmail.setVisibility(View.GONE);
        loginNameView.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.GONE);
        loginNotificationsView.setVisibility(View.GONE);

        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onLoginClick();
                    return true;
                }
                return false;
            }
        });

        LoginTextWatcher ltw = new LoginTextWatcher();
        firstnameEditText.addTextChangedListener(ltw);
        lastnameEditText.addTextChangedListener(ltw);

        if (loginPresenter != null) {
            AuthenticationController authenticationController = loginPresenter.authenticationController;
            if (authenticationController != null) {
                User user = authenticationController.getUser();
                if (user != null) {
                    launchFillInProfileView(user.getPhone(), user);
                }
            }
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
        if (loginSignin.getVisibility() == View.VISIBLE) {
            phoneEditText.setText("");
            passwordEditText.setText("");
            hideKeyboard();
            loginSignin.setVisibility(View.GONE);
            loginStartup.setVisibility(View.VISIBLE);
        } else if (loginLostCode.getVisibility() == View.VISIBLE) {
            lostCodePhone.setText("");
            loginLostCode.setVisibility(View.GONE);
            loginSignin.setVisibility(View.VISIBLE);
            showKeyboard(phoneEditText);
        } else if (loginTutorial.getVisibility() == View.VISIBLE) {
            loginTutorial.setVisibility(View.GONE);
            showEmailView();
        } else if (loginEmail.getVisibility() == View.VISIBLE) {
            loginEmail.setVisibility(View.GONE);
            loginNameView.setVisibility(View.VISIBLE);
        } else if (loginNameView.getVisibility() == View.VISIBLE) {
            hideKeyboard();
            loginNameView.setVisibility(View.GONE);
            loginSignin.setVisibility(View.VISIBLE);
            showKeyboard(phoneEditText);
        } else if (loginNewsletter.getVisibility() == View.VISIBLE && previousView != null) {
            newsletterEmail.setText("");
            loginNewsletter.setVisibility(View.GONE);
            previousView.setVisibility(View.VISIBLE);
            if (previousView == loginSignin) {
                showKeyboard(phoneEditText);
            }
        } else if (loginVerifyCode.getVisibility() == View.VISIBLE) {
            showLostCodeScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        BusProvider.getInstance().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        BusProvider.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (int index = 0; index < permissions.length; index++) {
                if (permissions[index].equalsIgnoreCase(getUserLocationAccess()) && grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    BusProvider.getInstance().post(new Events.OnLocationPermissionGranted(false));
                } else {
                    BusProvider.getInstance().post(new Events.OnLocationPermissionGranted(true));
                }
            }
            // We don't care if the user allowed/denied the location, just show the notifications view
            //TODO to do this in onResume, HOTFIX: dismisAllowingStateLoss in function
            hideActionZoneView();
            showNotificationPermissionView();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startMapActivity() {
        stopLoader();
        hideKeyboard();
        EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_OK);
        startActivity(new Intent(this, DrawerActivity.class));
        finish();
    }

    public void loginFail(int errorCode) {
        stopLoader();
        @StringRes int errorMessage;
        switch (errorCode) {
            case LOGIN_ERROR_INVALID_PHONE_FORMAT:
                errorMessage = R.string.login_error_invalid_phone_format;
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_FAILED);
                break;
            case LOGIN_ERROR_UNAUTHORIZED:
                errorMessage = R.string.login_error_invalid_credentials;
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_FAILED);
                break;
            case LOGIN_ERROR_NETWORK:
                errorMessage = R.string.login_error_network;
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_ERROR);
                break;
            default:
                errorMessage = R.string.login_error;
                EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_ERROR);
                break;
        }
        new AlertDialog.Builder(this)
            .setTitle(R.string.login_error_title)
            .setMessage(errorMessage)
            .setPositiveButton(R.string.login_retry_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                }
            })
            .create()
            .show();
    }

    public void displayToast(@StringRes int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void startLoader() {
        loginButton.setText(R.string.button_loading);
        loginButton.setEnabled(false);
        receiveCodeButton.setText(R.string.button_loading);
        receiveCodeButton.setEnabled(false);
        lostCodePhone.setEnabled(false);
        newsletterButton.setText(R.string.button_loading);
        newsletterButton.setEnabled(false);
        verifyCodeButton.setEnabled(false);
        nameGoButton.setEnabled(false);
    }

    public void stopLoader() {
        loginButton.setText(R.string.login_button_signup);
        loginButton.setEnabled(true);
        receiveCodeButton.setText(R.string.login_button_ask_code);
        receiveCodeButton.setEnabled(true);
        lostCodePhone.setEnabled(true);
        newsletterButton.setText(R.string.login_button_newsletter);
        newsletterButton.setEnabled(true);
        verifyCodeButton.setEnabled(true);
        nameGoButton.setEnabled(true);
    }

    public void launchFillInProfileView(String phoneNumber, @NonNull User user) {

        loggedPhoneNumber = phoneNumber;

        if (this.onboardingUser != null) {
            user.setOnboardingUser(true);
        }
        try {
            DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(RegisterSMSCodeFragment.TAG);
            if (fragment != null) {
                fragment.dismiss();
            }
            fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(RegisterNumberFragment.TAG);
            if (fragment != null) {
                fragment.dismiss();
            }
            fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(RegisterWelcomeFragment.TAG);
            if (fragment != null) {
                fragment.dismiss();
            }

            hideKeyboard();

            if (loginPresenter != null) {
                loginStartup.setVisibility(View.GONE);
                loginSignin.setVisibility(View.GONE);
                loginVerifyCode.setVisibility(View.GONE);
                if (loginPresenter.shouldShowNameView(user)) {
                    showNameView();
                } else if (loginPresenter.shouldShowEmailView(user)) {
                    showEmailView();
                } else if (loginPresenter.shouldShowPhotoChooseView(user)) {
                    showPhotoChooseSource();
                } else if (loginPresenter.shouldShowActionZoneView(user)) {
                    showActionZoneView();
                } else {
                    showNotificationPermissionView();
                }
            }
        } catch(IllegalStateException e){
            EntourageEvents.logEvent(EntourageEvents.EVENT_ILLEGAL_STATE);
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

    @Override
    public void onPhotoBack() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_BACK);
        showActionZoneView();
    }

    @Override
    public void onPhotoIgnore() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_IGNORE);
        showActionZoneView();
    }

    @Override
    public void onPhotoChosen(final Uri photoUri, int photoSource) {

        if (photoSource == PhotoChooseSourceFragment.TAKE_PHOTO_REQUEST) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_SUBMIT);
        }

        if (loginPresenter == null || loginPresenter.authenticationController == null || loginPresenter.authenticationController.getUser() == null) {
            displayToast(R.string.login_error);
            PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
            if (photoEditFragment != null) {
                photoEditFragment.onPhotoSent(false);
            }
            return;
        }

        //Upload the photo to Amazon S3
        showProgressDialog(R.string.user_photo_uploading);

        File file = new File(photoUri.getPath());
        avatarUploadPresenter.uploadPhoto(file);
    }

    public void onUploadError() {
        displayToast(R.string.user_photo_error_not_saved);
        dismissProgressDialog();
        PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
        if (photoEditFragment != null) {
            photoEditFragment.onPhotoSent(false);
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    /************************
     * Signin View
     ************************/

    /*
    @OnClick(R.id.login_text_more)
    void onAskMore() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        informationFragment = new LoginInformationFragment();
        informationFragment.show(fragmentManager, "fragment_login_information");
    }
    */
    @OnClick(R.id.login_back_button)
    void onLoginBackClick() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_signup)
    void onLoginClick() {
        if (loginPresenter != null) {
            loginPresenter.login(
                countryCodePicker.getSelectedCountryCodeWithPlus(),
                phoneEditText.getText().toString(),
                passwordEditText.getText().toString());
        } else {
            displayToast(R.string.login_error);
        }
    }

    @OnClick(R.id.login_text_lost_code)
    void onLostCodeClick() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SMS_CODE_REQUEST);
        hideKeyboard();
        loginSignin.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.VISIBLE);
        confirmationBlock.setVisibility(View.GONE);

        String phoneNumber = phoneEditText.getText().toString();
        lostCodePhone.setText(phoneNumber);

        showKeyboard(lostCodePhone);
    }

    /*
    @OnClick(R.id.login_welcome_more)
    void onMoreClick() {
        loginSignin.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.VISIBLE);
        previousView = loginSignin;
        showKeyboard(newsletterEmail);
    }
    */

    /************************
     * Lost Code View
     ************************/

    @OnClick(R.id.login_lost_code_close)
    void lostCodeClose() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_ask_code)
    void sendNewCode() {
        String phoneNumber = Utils.checkPhoneNumberFormat(lostCodeCountryCodePicker.getSelectedCountryCodeWithPlus(), lostCodePhone.getText().toString());
        if (phoneNumber == null) {
            displayToast(R.string.login_text_invalid_format);
            return;
        }
        if (loginPresenter != null) {
            startLoader();
            loginPresenter.sendNewCode(phoneNumber);
            EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_SEND_NEW_CODE);
        } else {
            displayToast(R.string.login_error);
        }
    }

    @OnClick(R.id.login_button_home)
    void returnHome() {
        lostCodePhone.setText("");
        confirmationBlock.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.GONE);
        loginSignin.setVisibility(View.VISIBLE);
        showKeyboard(phoneEditText);
    }

    void newCodeAsked(User user, boolean isOnboarding) {
        stopLoader();
        if (user != null) {
            if (isOnboarding) {
                //registerPhoneNumberSent(onboardingUser.getPhone(), true);
                displayToast(R.string.registration_smscode_sent);
            } else {
                if (loginLostCode.getVisibility() == View.VISIBLE) {
                    loginLostCode.setVisibility(View.GONE);
                    loginVerifyCode.setVisibility(View.VISIBLE);
                } else {
                    displayToast(R.string.login_text_lost_code_ok);
                }
            }
        } else {
            if (isOnboarding || Configuration.getInstance().showLostCodeErrorToast()) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_03_2);
                displayToast(R.string.login_text_lost_code_ko);
            } else {
                if (loginLostCode.getVisibility() == View.VISIBLE) {
                    //codeConfirmation.setText(R.string.login_text_lost_code_ko);
                    codeConfirmation.setHtmlString(R.string.login_text_lost_code_ko_html);
                    enterCodeBlock.setVisibility(View.GONE);
                    confirmationBlock.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /************************
     * Email View
     ************************/

    private void showEmailView() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_4);
        loginEmail.setVisibility(View.VISIBLE);

        if (loginPresenter != null && loginPresenter.authenticationController != null) {
            User user = loginPresenter.authenticationController.getUser();
            if (user != null) {
                if (user.getEmail() != null) {
                    profileEmail.setText(user.getEmail());
                }
            }
        }

        profileEmail.requestFocus();
    }

    @OnClick(R.id.login_email_back_button)
    void onEmailBackClicked() {
        profileEmail.setText("");
        onBackPressed();
    }

    @OnClick(R.id.login_button_go)
    void saveEmail() {
        if (loginPresenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_EMAIL_SUBMIT);
            loginPresenter.updateUserEmail(profileEmail.getText().toString());

            loginPresenter.updateUserToServer();
        } else {
            displayToast(R.string.login_error);
        }
    }

    @OnClick(R.id.login_email_ignore_button)
    void ignoreEmail() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_EMAIL_IGNORE);
        if (loginPresenter != null) {
            loginPresenter.updateUserToServer();
        } else {
            displayToast(R.string.login_error);
        }
    }

    /************************
     * Enter Name View
     ************************/

    void showNameView() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_5);
        loginNameView.setVisibility(View.VISIBLE);

        if (loginPresenter != null && loginPresenter.authenticationController != null) {
            User user = loginPresenter.authenticationController.getUser();
            if (user != null) {
                if (user.getFirstName() != null) {
                    firstnameEditText.setText(user.getFirstName());
                }
                if (user.getLastName() != null) {
                    lastnameEditText.setText(user.getLastName());
                }
            }
        }

        firstnameEditText.requestFocus();
    }

    @OnClick(R.id.login_name_back_button)
    void onNameBackClicked() {
        onBackPressed();
    }

    @OnClick(R.id.login_name_go_button)
    void onNameGoClicked() {
        if (loginPresenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_NAME_SUBMIT);
            String firstname = firstnameEditText.getText().toString().trim();
            String lastname = lastnameEditText.getText().toString().trim();
            if (firstname.length() == 0) {
                displayToast(R.string.login_firstname_error);
                return;
            }
            if (lastname.length() == 0) {
                displayToast(R.string.login_lastname_error);
                return;
            }
            hideKeyboard();
            loginPresenter.updateUserName(firstname, lastname);
            User user = loginPresenter.authenticationController.getUser();
            if (user != null) {
                if (loginPresenter.shouldShowEmailView(user)) {
                    loginNameView.setVisibility(View.GONE);
                    showEmailView();
                    return;
                }
            }
            loginPresenter.updateUserToServer();
        } else {
            displayToast(R.string.login_error);
        }
    }

    protected void showPhotoChooseSource() {
        if (isFinishing()) return;
        if (loginPresenter != null && loginPresenter.authenticationController != null) {
            hideKeyboard();
            loginEmail.setVisibility(View.GONE);
            loginNameView.setVisibility(View.GONE);

            User user = loginPresenter.authenticationController.getUser();
            if (user == null || user.getAvatarURL() == null || user.getAvatarURL().length() == 0) {
                try {
                    PhotoChooseSourceFragment fragment = new PhotoChooseSourceFragment();
                    fragment.show(getSupportFragmentManager(), PhotoChooseSourceFragment.TAG);
                } catch(IllegalStateException e){
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ILLEGAL_STATE);
                }
            } else {
                if (loginPresenter.shouldShowActionZoneView()) {
                    showActionZoneView();
                } else {
                    showNotificationPermissionView();
                }
            }
        } else {
            displayToast(R.string.login_error);
        }
    }

    protected void onUserPhotoUpdated(boolean updated) {
        if (isFinishing()) return;
        try {
            dismissProgressDialog();
            PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
            if (photoEditFragment != null) {
                photoEditFragment.onPhotoSent(updated);
            }
            if (updated) {
                PhotoChooseSourceFragment fragment = (PhotoChooseSourceFragment) getSupportFragmentManager().findFragmentByTag(PhotoChooseSourceFragment.TAG);
                if (fragment != null && !fragment.isStopped()) {
                    fragment.dismiss();
                }
                showActionZoneView();
            } else {
                displayToast(R.string.user_photo_error_not_saved);
            }
        } catch(IllegalStateException e) {
            Timber.e(e);
        }
    }

    /************************
     * Private Methods
     ************************/

    private void saveNotificationsPreference(boolean enabled) {
        //remember the choice
        final SharedPreferences notificationsPreferences = EntourageApplication.get().getSharedPreferences();
        SharedPreferences.Editor editor = notificationsPreferences.edit();
        editor.putBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.commit();
    }

    private void finishTutorial() {
        //set the tutorial as done
        SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        HashSet<String> loggedNumbers = (HashSet<String>) sharedPreferences.getStringSet(KEY_TUTORIAL_DONE, new HashSet<String>());
        loggedNumbers.add(loggedPhoneNumber);
        sharedPreferences.edit().putStringSet(KEY_TUTORIAL_DONE, loggedNumbers).apply();

        startMapActivity();
    }

    /************************
     * Tutorial View
     ************************/

    /*
    TODO: put this back when the tutorial content is ready
    @OnClick(R.id.login_button_go)
    void startTutorial() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_TUTORIAL_START);
        loginPresenter.updateUserEmail(profileEmail.getText().toString());
        loginEmail.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.VISIBLE);
    }
    */

    /************************
     * Startup View
     ************************/

    @OnClick(R.id.login_button_login)
    void onStartupLoginClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SPLASH_LOGIN);
        if (loginPresenter != null && loginPresenter.shouldShowTC()) {
            this.onboardingUser = null;
            RegisterWelcomeFragment registerWelcomeFragment = new RegisterWelcomeFragment();
            registerWelcomeFragment.show(getSupportFragmentManager(), RegisterWelcomeFragment.TAG);
        } else {
            showLoginScreen();
        }
    }

    void showLoginScreen() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_START);
        loginStartup.setVisibility(View.GONE);
        loginSignin.setVisibility(View.VISIBLE);
        showKeyboard(phoneEditText);
        this.onboardingUser = null;
    }

    @OnClick(R.id.login_button_register)
    void showRegisterScreen() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SPLASH_SIGNUP);
        this.onboardingUser = new User();
        RegisterWelcomeFragment registerWelcomeFragment = new RegisterWelcomeFragment();
        registerWelcomeFragment.show(getSupportFragmentManager(), RegisterWelcomeFragment.TAG);
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
        if (loginPresenter != null) {
            hideKeyboard();
            startLoader();
            loginPresenter.subscribeToNewsletter(newsletterEmail.getText().toString());
        } else {
            displayToast(R.string.login_error);
        }
    }

    void newsletterResult(boolean success) {
        stopLoader();
        if (success) {
            displayToast(R.string.login_text_newsletter_success);
            onBackPressed();
        } else {
            displayToast(R.string.login_text_newsletter_fail);
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
        if (loginPresenter != null) {
            loginPresenter.login(
                lostCodeCountryCodePicker.getSelectedCountryCodeWithPlus(),
                lostCodePhone.getText().toString(),
                receivedCode.getText().toString()
            );
        } else {
            displayToast(R.string.login_error);
        }
    }

    @OnClick(R.id.login_verify_code_back)
    void showLostCodeScreen() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_03_1);
        hideKeyboard();
        receivedCode.setText("");
        loginVerifyCode.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.VISIBLE);
        showKeyboard(lostCodePhone);
    }

    @Optional
    @OnClick(R.id.login_verify_code_description)
    void showResendByEmailView() {
        View verifyCodeByEmailView = findViewById(R.id.login_verify_code_email);
        if (verifyCodeByEmailView != null) verifyCodeByEmailView.setVisibility(View.VISIBLE);
    }

    /************************
     * Register
     ************************/

    // OnRegisterUserListener
    @Override
    public void registerShowSignIn() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_02_1);
        showLoginScreen();
    }

    @Override
    public boolean registerStart() {
        if ( onboardingUser != null || (loginPresenter != null && loginPresenter.shouldContinueWithRegistration()) ) return true;
        showLoginScreen();
        return false;
    }

    @Override
    public void registerSavePhoneNumber(String phoneNumber) {
        if (loginPresenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PHONE_SUBMIT);
            loginPresenter.registerUserPhone(phoneNumber);
        } else {
            displayToast(R.string.login_error);
        }
    }

    @Override
    public void registerCheckCode(final String smsCode) {
        if (loginPresenter != null && onboardingUser != null) {
            loginPresenter. login(null, onboardingUser.getPhone(), smsCode);
        } else {
            displayToast(R.string.login_error);
        }
    }

    @Override
    public void registerResendCode() {
        if (loginPresenter != null && onboardingUser != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_03_1);
            loginPresenter.sendNewCode(onboardingUser.getPhone(), true);
        } else {
            displayToast(R.string.login_error);
        }
    }

    protected void registerPhoneNumberSent(String phoneNumber, boolean smsSent) {
        if (isFinishing()) return;
        if (getSupportFragmentManager() != null) {
            RegisterNumberFragment numberFragment = (RegisterNumberFragment)getSupportFragmentManager().findFragmentByTag(RegisterNumberFragment.TAG);
            if (numberFragment != null) numberFragment.savedPhoneNumber(smsSent);
        }
        if (smsSent) {
            displayToast(R.string.registration_smscode_sent);
            if (onboardingUser != null) {
                onboardingUser.setPhone(phoneNumber);
            }

            try {
                RegisterSMSCodeFragment fragment = new RegisterSMSCodeFragment();
                fragment.show(getSupportFragmentManager(), RegisterSMSCodeFragment.TAG);
            } catch (IllegalStateException e) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ILLEGAL_STATE);
            }
        }
    }

    /************************
     * Notifications View
     ************************/

    public void showNotificationPermissionView() {
        if (NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()) {
            finishTutorial();
            return;
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_04_3);
        loginNotificationsView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.login_notifications_ignore_button)
    protected void onNotificationsIgnore() {
        //saveNotificationsPreference(false);
        //loginNotificationsView.setVisibility(View.GONE);
        finishTutorial();
    }

    @OnClick(R.id.login_notifications_accept)
    protected void onNotificationsAccept() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATIONS_ACCEPT);
        //saveNotificationsPreference(true);
        //loginNotificationsView.setVisibility(View.GONE);
        finishTutorial();
        Intent settingsIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setData(uri);
        }
        try {
            startActivity(settingsIntent);
        } catch (ActivityNotFoundException ex) {
            Timber.tag("NOTIFICATIONS").e("Failed to start the activity that shows the app settings");
        }
    }

    /************************
     * Geolocation View
     ************************/

    public void saveGeolocationPreference(boolean enabled) {
        //remember the choice
        final SharedPreferences notificationsPreferences = EntourageApplication.get().getSharedPreferences();
        SharedPreferences.Editor editor = notificationsPreferences.edit();
        editor.putBoolean(EntourageApplication.KEY_GEOLOCATION_ENABLED, enabled);
        editor.commit();
    }

    private boolean isGeolocationGranted() {
        return (PermissionChecker.checkSelfPermission(this, getUserLocationAccess()) == PackageManager.PERMISSION_GRANTED);
    }

    protected String getUserLocationAccess() {
        if (loginPresenter == null) return ACCESS_COARSE_LOCATION;
        User user = loginPresenter.authenticationController.getUser();
        return user != null ? user.getLocationAccessString() : ACCESS_COARSE_LOCATION;
    }

    /************************
     * Action Zone View
     ************************/

    private void showActionZoneView() {
        if (isFinishing()) return;
        User me = loginPresenter.authenticationController.getUser();
        UserEditActionZoneFragment actionZoneFragment = UserEditActionZoneFragment.newInstance(me != null ? me.getAddress() : null);
        actionZoneFragment.setFragmentListener(this);
        actionZoneFragment.setFromLogin(true);
        try {
            actionZoneFragment.show(getSupportFragmentManager(), UserEditActionZoneFragment.TAG);
        } catch (IllegalStateException e) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ILLEGAL_STATE);
        }
    }

    private void hideActionZoneView() {
        if (isFinishing()) return;
        UserEditActionZoneFragment actionZoneFragment = (UserEditActionZoneFragment)getSupportFragmentManager().findFragmentByTag(UserEditActionZoneFragment.TAG);
        if (actionZoneFragment != null) {
            actionZoneFragment.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onUserEditActionZoneFragmentDismiss() {
        showNameView();
    }

    @Override
    public void onUserEditActionZoneFragmentIgnore() {
        onUserEditActionZoneFragmentAddressSaved();
    }

    @Override
    public void onUserEditActionZoneFragmentAddressSaved() {
        if (isGeolocationGranted()) {
            hideActionZoneView();
            showNotificationPermissionView();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String accessLocation = getUserLocationAccess();
                requestPermissions(new String[]{accessLocation}, PERMISSIONS_REQUEST_LOCATION);
            } else {
                showNotificationPermissionView();
            }
        }
    }

    /************************
     * Bus Events
     ************************/

    @Subscribe
    public void onShowURLRequested(Events.OnShowURLEvent event) {
        if (event == null) return;
        showWebView(event.getUrl());
    }

    /************************
     * LoginTextWatcher Class
     ************************/

    class LoginTextWatcher implements TextWatcher {
        private boolean firstEvent = true;
        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            if(firstEvent) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_NAME_TYPE);
                firstEvent = false;
            }
        }

        @Override
        public void afterTextChanged(final Editable s) {

        }
    }
}
