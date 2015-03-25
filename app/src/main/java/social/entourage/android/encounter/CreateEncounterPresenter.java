package social.entourage.android.encounter;

import javax.inject.Inject;

import social.entourage.android.api.EncounterService;
import social.entourage.android.api.LoginService;

/**
 * Presenter controlling the main activity
 */
public class CreateEncounterPresenter {
    private final CreateEncounterActivity activity;
    private final EncounterService encounterService;

    @Inject
    public CreateEncounterPresenter(
            final CreateEncounterActivity activity,
            final EncounterService encounterService
    ) {
        this.activity = activity;
        this.encounterService = encounterService;
    }

    public void createActivity() {

    }
}
