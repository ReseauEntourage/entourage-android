package social.entourage.android.user.edit;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to UserEditFragment lifecycle
 * Provides an UserEditPresenter
 *
 * @see UserEditFragment
 * @see UserEditPresenter
 *
 * Created by mihaiionescu on 01/11/16.
 */

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = UserEditModule.class
)

public interface UserEditComponent {

    void inject(UserEditFragment fragment);

    UserEditPresenter getUserPresenter();

}
