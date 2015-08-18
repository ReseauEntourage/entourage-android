package social.entourage.android.authentication.login;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to LoginInformationFragment lifecycle
 * Provide a LoginInformationPresenter
 * @see LoginInformationFragment
 * @see LoginInformationPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = LoginInformationModule.class
)
@SuppressWarnings("unused")
public interface LoginInformationComponent {
    void inject(LoginInformationFragment fragment);

    LoginInformationPresenter getLoginInformationPresenter();
}
