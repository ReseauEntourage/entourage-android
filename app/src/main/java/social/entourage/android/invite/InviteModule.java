package social.entourage.android.invite;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mihaiionescu on 12/07/16.
 */
@Module
public class InviteModule {
    private final InviteBaseFragment fragment;

    public InviteModule(InviteBaseFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public InviteBaseFragment providesInviteContactsFragment() {
        return fragment;
    }
}
