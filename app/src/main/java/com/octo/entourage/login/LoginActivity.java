package com.octo.entourage.login;

import android.os.Bundle;

import com.octo.entourage.EntourageActivity;
import com.octo.entourage.R;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends EntourageActivity {

    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new LoginModule(this));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
