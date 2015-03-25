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
                CreateEncounterActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class CreateEncounterModule {
    private final CreateEncounterActivity activity;

    public CreateEncounterModule(final CreateEncounterActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public CreateEncounterActivity providesActivity() {
        return activity;
    }
}
