package social.entourage.android.user;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to UserActivity lifecycle
 * Provide a UserPresenter
 * @see UserActivity
 * @see UserPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = UserModule.class
)
@SuppressWarnings("unused")
public interface UserComponent {
    void inject(UserActivity activity);

    UserPresenter getUserPresenter();
}
