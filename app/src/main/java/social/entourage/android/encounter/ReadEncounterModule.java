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
