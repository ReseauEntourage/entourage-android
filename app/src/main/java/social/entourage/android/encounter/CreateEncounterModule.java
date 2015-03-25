package social.entourage.android.encounter;

import com.octo.entourage.EntourageModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                CreateEncounterActivity.class
        },
        addsTo = CreateEncounterModule.class,
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
    public CreateEncounterPresenter providesMainPresenter() {
        return new CreateEncounterPresenter(activity);
    }
}
