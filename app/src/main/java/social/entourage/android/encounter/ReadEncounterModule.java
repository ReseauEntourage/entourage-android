package social.entourage.android.encounter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import social.entourage.android.EntourageModule;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                ReadEncounterActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class ReadEncounterModule {
    private final ReadEncounterActivity activity;

    public ReadEncounterModule(final ReadEncounterActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public ReadEncounterPresenter providesMainPresenter() {
        return new ReadEncounterPresenter(activity);
    }
}
