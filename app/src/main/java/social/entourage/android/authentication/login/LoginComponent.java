package social.entourage.android.authentication.login;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to LoginEntourageFragment lifecycle
 * Provide a LoginPresenter
 * @see LoginEntourageFragment
 * @see LoginPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = LoginModule.class
)
@SuppressWarnings("unused")
public interface LoginComponent {
    void inject(LoginEntourageFragment fragment);

    LoginPresenter getLoginPresenter();
}
