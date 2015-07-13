package social.entourage.android.encounter;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = CreateEncounterModule.class
)
public interface CreateEncounterComponent {
    void inject(CreateEncounterActivity activity);

    CreateEncounterPresenter getCreateEncounterPresenter();
}
