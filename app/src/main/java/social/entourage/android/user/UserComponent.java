package social.entourage.android.user;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to UserEntourageFragment lifecycle
 * Provide a UserPresenter
 * @see UserEntourageFragment
 * @see social.entourage.android.message.MessagePresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = UserModule.class
)
@SuppressWarnings("unused")
public interface UserComponent {
    void inject(UserEntourageFragment fragment);

    UserPresenter getUserPresenter();
}
