package social.entourage.android.authentication.login;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to LoginActivity lifecycle
 * Provide a BaseLoginPresenter
 * @see LoginActivity
 * @see BaseLoginPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = LoginModule.class
)
@SuppressWarnings("unused")
public interface LoginComponent {
    void inject(LoginActivity activity);

    LoginPresenter getLoginPresenter();
}