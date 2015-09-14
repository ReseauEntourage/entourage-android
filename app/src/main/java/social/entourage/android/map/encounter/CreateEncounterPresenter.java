package social.entourage.android.map.encounter;


import java.util.Date;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.Constants;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.authentication.AuthenticationController;

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
    private final AuthenticationController authenticationController;

    private long tourId;
    private double latitude;
    private double longitude;

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @Inject
    public CreateEncounterPresenter(final CreateEncounterActivity activity, final EncounterRequest encounterRequest, final AuthenticationController authenticationController) {
        this.activity = activity;
        this.encounterRequest = encounterRequest;
        this.authenticationController = authenticationController;
    }

    public void createEncounter(String message, String streetPersonName) {
        Encounter encounter = new Encounter();
        encounter.setUserName(authenticationController.getUser().getFirstName());
        encounter.setMessage(message);
        encounter.setStreetPersonName(streetPersonName);
        encounter.setCreationDate(new Date());
        encounter.setTourId(tourId);
        encounter.setLatitude(latitude);
        encounter.setLongitude(longitude);

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

    public void setTourId(long tourId) {
        this.tourId = tourId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
