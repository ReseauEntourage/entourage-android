package social.entourage.android.encounter;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.EncounterService;
import social.entourage.android.api.model.map.Encounter;

/**
 * Presenter controlling the main activity
 */
public class CreateEncounterPresenter implements Callback<EncounterResponse> {
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

    public void createEncounter(Encounter encounter) {
        encounterService.create(encounter, this);
    }

    @Override
    public void success(EncounterResponse encounterResponse, Response response) {
        activity.createEncounterSuccess();
    }

    @Override
    public void failure(RetrofitError error) {
        activity.createEncounterFail(error.toString());
    }
}
