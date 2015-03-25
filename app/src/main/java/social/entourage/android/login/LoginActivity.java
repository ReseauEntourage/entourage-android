package social.entourage.android.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import social.entourage.android.EntourageActivity;
import social.entourage.android.R;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LoginActivity extends EntourageActivity {

    @Inject
    LoginPresenter loginPresenter;

    @InjectView(R.id.edittext_email)
    EditText emailEditText;

    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new LoginModule(this));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);
    }

    @OnClick(R.id.button_login)
    public void onLoginClick(Button loginButton) {
        loginPresenter.login(emailEditText.getText().toString());
    }

    public void loginSuccess(final String name) {
        Toast.makeText(this, "Hello " + name, Toast.LENGTH_SHORT).show();
    }

    public void loginFail() {
        Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
    }
}
