package social.entourage.android.encounter;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module
public final class CreateEncounterModule {
    private final CreateEncounterActivity activity;

    public CreateEncounterModule(final CreateEncounterActivity activity) {
        this.activity = activity;
    }

    @Provides
    public CreateEncounterActivity providesActivity() {
        return activity;
    }
}
