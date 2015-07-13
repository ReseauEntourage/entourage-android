package social.entourage.android.encounter;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to ReadEncounterActivity
 * @see ReadEncounterActivity
 */
@Module
public final class ReadEncounterModule {
    private final ReadEncounterActivity activity;

    public ReadEncounterModule(final ReadEncounterActivity activity) {
        this.activity = activity;
    }

    @Provides
    public ReadEncounterPresenter providesMainPresenter() {
        return new ReadEncounterPresenter(activity);
    }
}
