package social.entourage.android.authentication.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.message.push.RegisterGCMService;

public class LoginEntourageFragment extends Fragment {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    LoginPresenter loginPresenter;

    @InjectView(R.id.login_more_text)
    TextView moreText;

    @InjectView(R.id.edittext_phone_number)
    EditText phoneEditText;

    @InjectView(R.id.edittext_password)
    EditText passwordEditText;

    @InjectView(R.id.button_login)
    Button loginButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_login_signup, container, false);
        ButterKnife.inject(this, toReturn);

        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = manager.getLine1Number();
        if (phoneNumber != null) {
            phoneEditText.setText(phoneNumber);
        }

        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerLoginComponent.builder()
                .entourageComponent(entourageComponent)
                .loginModule(new LoginModule(this))
                .build()
                .inject(this);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startMapActivity() {
        resetLoginButton();
        startActivity(new Intent(getActivity(), DrawerActivity.class));
        getActivity().finish();
    }

    public void loginFail() {
        resetLoginButton();
        Toast.makeText(getActivity(), getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
    }

    public void displayWrongFormat() {
        Toast.makeText(getActivity(), getString(R.string.login_number_invalid_format), Toast.LENGTH_SHORT).show();
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

    @OnClick(R.id.button_login)
    void onLoginClick() {
        SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        loginPresenter.login(
                phoneEditText.getText().toString(),
                passwordEditText.getText().toString(),
                getString(R.string.login_device_type),
                sharedPreferences.getString(RegisterGCMService.KEY_REGISTRATION_ID, null)
        );
    }

    @OnClick(R.id.login_more_text)
    void onAskMore() {
        Toast.makeText(getActivity(), "Ask More", Toast.LENGTH_SHORT).show();
    }
}
