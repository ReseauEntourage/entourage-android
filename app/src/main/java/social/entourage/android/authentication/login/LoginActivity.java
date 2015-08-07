package social.entourage.android.authentication.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.DrawerActivity;
import social.entourage.android.message.push.RegisterGCMService;

/**
 * Activity providing a login form
 */
@SuppressWarnings("WeakerAccess")
public class LoginActivity extends EntourageActivity {

    @Inject
    LoginPresenter loginPresenter;

    @InjectView(R.id.login_more_text)
    TextView moreText;
    @InjectView(R.id.edittext_email)
    EditText emailEditText;
    @InjectView(R.id.edittext_phone_number)
    EditText phoneEditText;
    @InjectView(R.id.edittext_password)
    EditText passwordEditText;
    @InjectView(R.id.button_login)
    Button loginButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String phoneNumber = manager.getLine1Number();
        if (phoneNumber != null) {
            phoneEditText.setText(phoneNumber);
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

    @OnClick(R.id.login_more_text)
    public void onAskMore() {
        Toast.makeText(this, "Ask More", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_login)
    public void onLoginClick() {

        boolean isFormatValid = false;
        String phoneNumber = phoneEditText.getText().toString();

        if (phoneNumber.length() == 10) {
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+33" + phoneNumber.substring(1, 10);
                isFormatValid = true;
            }
        }
        else if (phoneNumber.length() == 12) {
            if (phoneNumber.startsWith("+33")) {
                isFormatValid = true;
            }
        }

        if (isFormatValid) {
            Pattern pattern = Pattern.compile("^(\\+33)\\d{9}");
            Matcher matcher = pattern.matcher(phoneNumber);
            if (matcher.matches()) {
                startLoader();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
                loginPresenter.login(
                        phoneNumber, passwordEditText.getText().toString(),
                        getString(R.string.login_device_type),
                        sharedPreferences.getString(RegisterGCMService.KEY_REGISTRATION_ID, null)
                );
            } else {
                Toast.makeText(this, getString(R.string.login_number_invalid_format), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startMapActivity() {
        startActivity(new Intent(this, DrawerActivity.class));
        resetLoginButton();
        finish();
    }

    public void loginFail() {
        resetLoginButton();
        Toast.makeText(this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
    }

    private void startLoader() {
        loginButton.setText(R.string.loading_button_text);
        loginButton.setEnabled(false);
    }

    private void resetLoginButton() {
        loginButton.setText(R.string.login_button_connection_text);
        loginButton.setEnabled(true);
    }
}
