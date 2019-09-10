package social.entourage.android.entourage.information;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to EntourageInformationFragment
 * @see EntourageInformationFragment
 */
@Module
public class EntourageInformationModule {
    private final EntourageInformationFragment fragment;

    public EntourageInformationModule(final EntourageInformationFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public EntourageInformationFragment providesEntourageInformationFragment() {
        return fragment;
    }
}
