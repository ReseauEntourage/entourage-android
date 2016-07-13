package social.entourage.android.invite.contacts;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mihaiionescu on 12/07/16.
 */
@Module
public class InviteContactsModule {
    private final InviteContactsFragment fragment;

    public InviteContactsModule(InviteContactsFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public InviteContactsFragment providesInviteContactsFragment() {
        return fragment;
    }
}
