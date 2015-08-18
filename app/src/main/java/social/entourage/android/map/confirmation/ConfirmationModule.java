package social.entourage.android.map.confirmation;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to ConfirmationActivity
 * @see ConfirmationActivity
 */
@Module
final class ConfirmationModule {

    private final ConfirmationActivity activity;

    public ConfirmationModule(final ConfirmationActivity activity) {
        this.activity = activity;
    }

    @Provides
    public ConfirmationActivity providesActivity() {
        return activity;
    }

}
