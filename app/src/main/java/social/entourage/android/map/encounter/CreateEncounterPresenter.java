package social.entourage.android.map.encounter;


import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.R;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.model.map.Encounter;

/**
 * Presenter controlling the CreateEncounterActivity
 * @see CreateEncounterActivity
 */
public class CreateEncounterPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final CreateEncounterActivity activity;

    private final EncounterRequest encounterRequest;

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @Inject
    public CreateEncounterPresenter(final CreateEncounterActivity activity, final EncounterRequest encounterRequest) {
        this.activity = activity;
        this.encounterRequest = encounterRequest;
    }

    public void createEncounter(Encounter encounter) {
        activity.showProgressDialog(R.string.creating_encounter);
        Encounter.EncounterWrapper encounterWrapper = new Encounter.EncounterWrapper();
        encounterWrapper.setEncounter(encounter);
        encounterRequest.create(encounter.getTourId(), encounterWrapper, new Callback<EncounterResponse>() {
            @Override
            public void success(EncounterResponse encounterResponse, Response response) {
                activity.onCreateEncounterFinished(null, encounterResponse.getEncounter());
            }

            @Override
            public void failure(RetrofitError error) {
                activity.onCreateEncounterFinished(error.toString(), null);
            }
        });
    }
}
