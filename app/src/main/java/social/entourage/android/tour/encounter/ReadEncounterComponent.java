package social.entourage.android.tour.encounter;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to ReadEncounterActivity lifecycle
 * Provide a ReadEncounterPresenter
 * @see ReadEncounterActivity
 * @see ReadEncounterPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = ReadEncounterModule.class
)
@SuppressWarnings("unused")
public interface ReadEncounterComponent {
    void inject(ReadEncounterActivity activity);

    ReadEncounterPresenter getReadEncounterPresenter();
}