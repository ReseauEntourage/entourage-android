package social.entourage.android.invite.contacts;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Created by mihaiionescu on 12/07/16.
 */

@ActivityScope
@Component (
        dependencies = EntourageComponent.class,
        modules = InviteContactsModule.class
)

public interface InviteContactsComponent {
    void inject(InviteContactsFragment fragment);

    InviteContactsPresenter getPresenter();
}
