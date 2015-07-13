package social.entourage.android.login;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = LoginModule.class
)
public interface LoginComponent {
    void inject(LoginActivity activity);

    LoginPresenter getLoginPresenter();
}
