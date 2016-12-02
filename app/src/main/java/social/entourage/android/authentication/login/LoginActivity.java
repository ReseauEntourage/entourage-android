package social.entourage.android.authentication.login;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.flurry.android.FlurryAgent;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.util.HashSet;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.login.register.OnRegisterUserListener;
import social.entourage.android.authentication.login.register.RegisterNumberFragment;
import social.entourage.android.authentication.login.register.RegisterSMSCodeFragment;
import social.entourage.android.authentication.login.register.RegisterWelcomeFragment;
import social.entourage.android.base.AmazonS3Utils;
import social.entourage.android.message.push.RegisterGCMService;
import social.entourage.android.user.edit.photo.PhotoChooseInterface;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.edit.photo.PhotoEditFragment;
import social.entourage.android.view.HtmlTextView;

/**
 * Activity providing the login steps
 */
public class LoginActivity extends EntourageActivity implements LoginInformationFragment.OnEntourageInformationFragmentFinish, OnRegisterUserListener, PhotoChooseInterface {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int PERMISSIONS_REQUEST_PHONE_STATE = 1;
    public final static String KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE";

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

    private User onboardingUser;

    /************************
     * Signin View
     ************************/

    @Bind(R.id.login_include_signin)
    View loginSignin;

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

    @Bind(R.id.login_block_lost_code_start)
    View enterCodeBlock;

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

    //@Bind(R.id.login_edit_name_profile)
    //EditText profileName;

    @Bind(R.id.login_user_photo)
    ImageView profilePhoto;

    @Bind(R.id.login_button_go)
    FloatingActionButton goButton;

    /************************
     * Enter Name View
     ************************/

    @Bind(R.id.login_include_name)
    View loginNameView;

    @Bind(R.id.login_name_firstname)
    EditText firstnameEditText;

    @Bind(R.id.login_name_lastname)
    EditText lastnameEditText;

    @Bind(R.id.login_name_go_button)
    FloatingActionButton nameGoButton;

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

    /************************
     * Notifications Permissions View
     ************************/

    @Bind(R.id.login_include_notifications)
    View loginNotificationsView;

    /************************
     * Geolocation view
     ************************/

    @Bind(R.id.login_include_geolocation)
    View loginGeolocationView;

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

        loginSignin.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.GONE);
        loginVerifyCode.setVisibility(View.GONE);
        loginWelcome.setVisibility(View.GONE);
        loginNameView.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.GONE);
        loginNotificationsView.setVisibility(View.GONE);
        loginGeolocationView.setVisibility(View.GONE);

        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        firstnameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                FlurryAgent.logEvent(Constants.EVENT_NAME_TYPE);
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });
        lastnameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                FlurryAgent.logEvent(Constants.EVENT_NAME_TYPE);
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

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
        if (loginSignin.getVisibility() == View.VISIBLE) {
            phoneEditText.setText("");
            passwordEditText.setText("");
            hideKeyboard();
            loginSignin.setVisibility(View.GONE);
            loginStartup.setVisibility(View.VISIBLE);
        }
        else if (loginLostCode.getVisibility() == View.VISIBLE) {
            lostCodePhone.setText("");
            loginLostCode.setVisibility(View.GONE);
            loginSignin.setVisibility(View.VISIBLE);
            showKeyboard(phoneEditText);
        }
        else if (loginTutorial.getVisibility() == View.VISIBLE) {
            loginTutorial.setVisibility(View.GONE);
            loginWelcome.setVisibility(View.VISIBLE);
        }
        else if (loginWelcome.getVisibility() == View.VISIBLE) {
            loginWelcome.setVisibility(View.GONE);
            loginSignin.setVisibility(View.VISIBLE);
        }
        else if (loginNameView.getVisibility() == View.VISIBLE) {
            loginNameView.setVisibility(View.GONE);
            loginWelcome.setVisibility(View.VISIBLE);
        }
        else if (loginNewsletter.getVisibility() == View.VISIBLE && previousView != null) {
            newsletterEmail.setText("");
            loginNewsletter.setVisibility(View.GONE);
            previousView.setVisibility(View.VISIBLE);
            if (previousView == loginSignin) {
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
        finish();
    }

    public void loginFail(int errorCode) {
        stopLoader();
        FlurryAgent.logEvent(Constants.EVENT_LOGIN_FAILED);
        @StringRes int errorMessage;
        switch (errorCode) {
            case LOGIN_ERROR_INVALID_PHONE_FORMAT:
                errorMessage = R.string.login_error_invalid_phone_format;
                break;
            case LOGIN_ERROR_UNAUTHORIZED:
                errorMessage = R.string.login_error_invalid_credentials;
                break;
            case LOGIN_ERROR_NETWORK:
            default:
                errorMessage = R.string.login_error;
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

    public void displayToast(String message) {
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
        verifyCodeButton.setText(R.string.button_loading);
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
        verifyCodeButton.setText(R.string.login_button_verify_code);
        verifyCodeButton.setEnabled(true);
        nameGoButton.setEnabled(true);
    }

    public void launchFillInProfileView(String phoneNumber, User user) {

        //TODO Need a better approach
        DialogFragment fragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(RegisterSMSCodeFragment.TAG);
        if (fragment != null) fragment.dismiss();
        fragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(RegisterNumberFragment.TAG);
        if (fragment != null) fragment.dismiss();
        fragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(RegisterWelcomeFragment.TAG);
        if (fragment != null) fragment.dismiss();

        loggedPhoneNumber = phoneNumber;

        if (this.onboardingUser != null) {
            user.setOnboardingUser(true);
        }
        hideKeyboard();

        loginStartup.setVisibility(View.GONE);
        loginSignin.setVisibility(View.GONE);
        loginVerifyCode.setVisibility(View.GONE);
        if (user.getEmail() == null || user.getEmail().length() == 0) {
            loginWelcome.setVisibility(View.VISIBLE);
            profileEmail.requestFocus();
        }
        else if (user.getFirstName() == null || user.getFirstName().length() == 0 || user.getLastName() == null || user.getLastName().length() == 0) {
            showNameView();
        }
        else if (user.getAvatarURL() == null || user.getAvatarURL().length() == 0) {
            showPhotoChooseSource();
        }
        else {
            showNotificationPermissionView();
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
        FlurryAgent.logEvent(Constants.EVENT_PHOTO_BACK);
        showNotificationPermissionView();
    }

    @Override
    public void onPhotoIgnore() {
        FlurryAgent.logEvent(Constants.EVENT_PHOTO_IGNORE);
        showNotificationPermissionView();
    }

    @Override
    public void onPhotoChosen(final Uri photoUri) {

        FlurryAgent.logEvent(Constants.EVENT_PHOTO_SUBMIT);

        if (loginPresenter == null || loginPresenter.authenticationController == null || loginPresenter.authenticationController.getUser() == null) {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
            PhotoEditFragment photoEditFragment = (PhotoEditFragment)getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
            if (photoEditFragment != null) {
                photoEditFragment.onPhotoSent(false);
            }
        }

        //Upload the photo to Amazon S3
        showProgressDialog(R.string.user_photo_uploading);

        final String objectKey = "user_"+loginPresenter.authenticationController.getUser().getId()+".jpg";
        TransferUtility transferUtility = AmazonS3Utils.getTransferUtility(this);
        TransferObserver transferObserver = transferUtility.upload(
                BuildConfig.AWS_BUCKET,
                "300x300/"+objectKey,
                new File(photoUri.getPath()),
                CannedAccessControlList.PublicRead
        );
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(final int id, final TransferState state) {
                if (state == TransferState.COMPLETED) {
                    if (loginPresenter != null) {
                        loginPresenter.updateUserPhoto(objectKey);
                        // Delete the temporary file
                        File tmpImageFile = new File(photoUri.getPath());
                        if (!tmpImageFile.delete()) {
                            // Failed to delete the file
                            Log.d("EntouragePhoto", "Failed to delete the temporary photo file");
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                        dismissProgressDialog();
                        PhotoEditFragment photoEditFragment = (PhotoEditFragment)getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                        if (photoEditFragment != null) {
                            photoEditFragment.onPhotoSent(false);
                        }
                    }
                }
            }

            @Override
            public void onProgressChanged(final int id, final long bytesCurrent, final long bytesTotal) {

            }

            @Override
            public void onError(final int id, final Exception ex) {
                Toast.makeText(LoginActivity.this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                dismissProgressDialog();
                PhotoEditFragment photoEditFragment = (PhotoEditFragment)getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                if (photoEditFragment != null) {
                    photoEditFragment.onPhotoSent(false);
                }
            }
        });
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
                    phoneEditText.getText().toString(),
                    passwordEditText.getText().toString());
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.login_text_lost_code)
    void onLostCodeClick() {
        FlurryAgent.logEvent(Constants.EVENT_SMS_CODE_REQUEST);
        hideKeyboard();
        loginSignin.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.VISIBLE);
        confirmationBlock.setVisibility(View.GONE);

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
        if (loginPresenter != null) {
            startLoader();
            loginPresenter.sendNewCode(lostCodePhone.getText().toString());
            FlurryAgent.logEvent(Constants.EVENT_LOGIN_SEND_NEW_CODE);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
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
                    displayToast(getString(R.string.login_text_lost_code_ok));
                }
            }
        } else {
            if (isOnboarding) {
                displayToast(getString(R.string.login_text_lost_code_ko));
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
     * Welcome View
     ************************/

    @OnClick(R.id.login_email_back_button)
    void onEmailBackClicked() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_go)
    void saveEmail() {
        if (loginPresenter != null) {
            FlurryAgent.logEvent(Constants.EVENT_EMAIL_SUBMIT);
            loginPresenter.updateUserEmail(profileEmail.getText().toString());

            User user = loginPresenter.authenticationController.getUser();
            if (user.getFirstName() == null || user.getFirstName().length() == 0 || user.getLastName() == null || user.getLastName().length() == 0) {
                loginWelcome.setVisibility(View.GONE);
                showNameView();
            } else {
                loginPresenter.updateUserToServer();
            }
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    /************************
     * Enter Name View
     ************************/

    void showNameView() {
        loginNameView.setVisibility(View.VISIBLE);

        if (loginPresenter != null && loginPresenter.authenticationController  != null) {
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
            FlurryAgent.logEvent(Constants.EVENT_NAME_SUBMIT);
            hideKeyboard();
            loginPresenter.updateUserName(firstnameEditText.getText().toString(), lastnameEditText.getText().toString());
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    protected void showPhotoChooseSource() {
        if (loginPresenter != null && loginPresenter.authenticationController != null) {
            hideKeyboard();
            loginWelcome.setVisibility(View.GONE);
            loginNameView.setVisibility(View.GONE);

            User user = loginPresenter.authenticationController.getUser();
            if (user == null || user.getAvatarURL() == null || user.getAvatarURL().length() == 0) {
                PhotoChooseSourceFragment fragment = new PhotoChooseSourceFragment();
                fragment.show(getSupportFragmentManager(), PhotoChooseSourceFragment.TAG);
            } else {
                showNotificationPermissionView();
            }
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    protected void onUserPhotoUpdated(boolean updated) {
        dismissProgressDialog();
        PhotoEditFragment photoEditFragment = (PhotoEditFragment)getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
        if (photoEditFragment != null) {
            photoEditFragment.onPhotoSent(updated);
        }
        if (updated) {
            PhotoChooseSourceFragment fragment = (PhotoChooseSourceFragment)getSupportFragmentManager().findFragmentByTag(PhotoChooseSourceFragment.TAG);
            if (fragment != null) {
                fragment.dismiss();
            }
            showNotificationPermissionView();
        } else {
            Toast.makeText(this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
        }
    }

    /************************
     * Private Methods
     ************************/

    private void saveNotifications(boolean enabled) {
        //remember the choice
        final SharedPreferences notificationsPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = notificationsPreferences.edit();
        editor.putBoolean(RegisterGCMService.KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.commit();
    }

    private void finishTutorial() {
        //set the tutorial as done
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        HashSet<String>loggedNumbers = (HashSet<String>) sharedPreferences.getStringSet(KEY_TUTORIAL_DONE, new HashSet<String>());
        loggedNumbers.add(loggedPhoneNumber);
        sharedPreferences.edit().putStringSet(KEY_TUTORIAL_DONE, loggedNumbers).commit();

        startMapActivity();
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
        FlurryAgent.logEvent(Constants.EVENT_SPLASH_LOGIN);
        loginStartup.setVisibility(View.GONE);
        loginSignin.setVisibility(View.VISIBLE);
        showKeyboard(phoneEditText);
        this.onboardingUser = null;
    }

    @OnClick(R.id.login_button_register)
    void showRegisterScreen() {
        FlurryAgent.logEvent(Constants.EVENT_SPLASH_SIGNUP);
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
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
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
        if (loginPresenter != null) {
            loginPresenter.login(
                    lostCodePhone.getText().toString(),
                    receivedCode.getText().toString()
            );
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
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

    /************************
     * Register
     ************************/

    // OnRegisterUserListener

    @Override
    public void registerShowSignIn() {
        showLoginScreen();
    }

    @Override
    public void registerSavePhoneNumber(String phoneNumber) {
        if (loginPresenter != null) {
            FlurryAgent.logEvent(Constants.EVENT_PHONE_SUBMIT);
            loginPresenter.registerUserPhone(phoneNumber);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void registerCheckCode(final String smsCode) {
        if (loginPresenter != null) {
            loginPresenter.login(onboardingUser.getPhone(), smsCode);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void registerResendCode() {
        if (loginPresenter != null) {
            FlurryAgent.logEvent(Constants.EVENT_SMS_CODE_REQUEST);
            loginPresenter.sendNewCode(onboardingUser.getPhone(), true);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    protected void registerPhoneNumberSent(String phoneNumber, boolean smsSent) {
        if (smsSent) {
            displayToast(R.string.registration_smscode_sent);
        }
        onboardingUser.setPhone(phoneNumber);
        RegisterSMSCodeFragment fragment = new RegisterSMSCodeFragment();
        fragment.show(getSupportFragmentManager(), RegisterSMSCodeFragment.TAG);
    }

    /************************
     * Notifications View
     ************************/

    private void showNotificationPermissionView() {
        loginNotificationsView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.login_notifications_ignore_button)
    protected void onNotificationsIgnore() {
        FlurryAgent.logEvent(Constants.EVENT_NOTIFICATIONS_REFUSE);
        saveNotifications(false);
        loginNotificationsView.setVisibility(View.GONE);
        showGeolocationView();
    }

    @OnClick(R.id.login_notifications_accept)
    protected void onNotificationsAccept() {
        FlurryAgent.logEvent(Constants.EVENT_NOTIFICATIONS_ACCEPT);
        saveNotifications(true);
        loginNotificationsView.setVisibility(View.GONE);
        showGeolocationView();
    }

    /************************
     * Geolocation View
     ************************/

    private void showGeolocationView() {
        loginGeolocationView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.login_geolocation_ignore_button)
    protected void onGeolocationIgnore() {
        FlurryAgent.logEvent(Constants.EVENT_GEOLOCATION_REFUSE);
        loginGeolocationView.setVisibility(View.GONE);
        finishTutorial();
    }

    @OnClick(R.id.login_geolocation_accept_button)
    protected void onGeolocationAccepted() {
        FlurryAgent.logEvent(Constants.EVENT_GEOLOCATION_ACCEPT);
        loginGeolocationView.setVisibility(View.GONE);
        finishTutorial();
    }

}
