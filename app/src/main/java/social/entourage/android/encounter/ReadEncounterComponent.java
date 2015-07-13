package social.entourage.android.encounter;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = ReadEncounterModule.class
)
public interface ReadEncounterComponent {
    void inject(ReadEncounterActivity activity);

    ReadEncounterPresenter getReadEncounterPresenter();
}