package social.entourage.android.invite;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Created by mihaiionescu on 12/07/16.
 */

@ActivityScope
@Component (
        dependencies = EntourageComponent.class,
        modules = InviteModule.class
)

public interface InviteComponent {
    void inject(InviteBaseFragment fragment);

    InvitePresenter getPresenter();
}
