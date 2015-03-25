package social.entourage.android.encounter;

import social.entourage.android.EntourageModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                EncounterActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class EncounterModule {
    private final EncounterActivity activity;

    public EncounterModule(final EncounterActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public EncounterPresenter providesMainPresenter() {
        return new EncounterPresenter(activity);
    }
}
