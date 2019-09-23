package social.entourage.android.tour.encounter;

import com.squareup.otto.Subscribe;
import com.squareup.tape.Task;

import java.io.Serializable;
import java.util.Date;

import javax.inject.Inject;

import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.tape.EncounterTapeTaskQueue;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.api.tape.EncounterTaskResult;
import social.entourage.android.tools.BusProvider;

/**
 * Presenter controlling the CreateEncounterActivity
 * @see CreateEncounterActivity
 */
public class CreateEncounterPresenter implements EncounterUploadCallback {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final CreateEncounterActivity activity;
    private final AuthenticationController authenticationController;
    private final EncounterTapeTaskQueue queue;

    private String tourUUID;
    private double latitude;
    private double longitude;

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @Inject
    public CreateEncounterPresenter(final CreateEncounterActivity activity, final AuthenticationController authenticationController, final EncounterTapeTaskQueue queue) {
        this.activity = activity;
        this.authenticationController = authenticationController;
        this.queue = queue;
    }

    public void createEncounter(String message, String streetPersonName) {

        Encounter encounter = new Encounter();
        encounter.setUserName(authenticationController.getUser().getDisplayName());
        encounter.setMessage(message);
        encounter.setStreetPersonName(streetPersonName);
        encounter.setCreationDate(new Date());
        encounter.setTourId(tourUUID);
        encounter.setLatitude(latitude);
        encounter.setLongitude(longitude);


//        queue.add(new EncounterUploadTask(encounter));
//        activity.onCreateEncounterFinished(null, encounter);
        EncounterUploadTask encounterUploadTask = new EncounterUploadTask(encounter);
        encounterUploadTask.execute(this);
    }

    public void updateEncounter(Encounter encounter) {
        // to avoid endless requests to the server, we execute this task only once
        // with our presenter as the callback
        EncounterUploadTask encounterUploadTask = new EncounterUploadTask(encounter);
        encounterUploadTask.execute(this);
    }

    public void setTourUUID(String tourUUID) {
        this.tourUUID = tourUUID;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAuthor() {
        if ( !authenticationController.isAuthenticated() ) {
            return "";
        }
        return authenticationController.getUser().getDisplayName();
    }

    // ----------------------------------
    // EncounterUploadCallback
    // ----------------------------------

    public void onSuccess(Encounter encounter, EncounterTaskResult.OperationType operationType) {
        if (activity != null) {
            switch (operationType) {
                case ENCOUNTER_ADD:
                    activity.onCreateEncounterFinished(null, encounter);
                    break;
                case ENCOUNTER_UPDATE:
                    activity.onUpdatingEncounterFinished(null, encounter);
                    break;
                default:
                    break;
            }
        }
    }

    public void onFailure(Encounter encounter, EncounterTaskResult.OperationType operationType) {
        if (activity != null) {
            switch (operationType) {
                case ENCOUNTER_ADD:
                    activity.onCreateEncounterFinished("error", null);
                    break;
                case ENCOUNTER_UPDATE:
                    activity.onUpdatingEncounterFinished("error", null);
                    break;
                default:
                    break;
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public class EncounterUploadTask implements Task<EncounterUploadCallback>, Serializable {

        private static final long serialVersionUID = -4119167198701340648L;
        
        private Encounter encounter;
        private EncounterUploadCallback callback;

        public EncounterUploadTask(Encounter encounter) {
            this.encounter = encounter;
        }

        @Override
        public void execute(final EncounterUploadCallback callback) {
            this.callback = callback;
            BusProvider.getInstance().register(this);
            BusProvider.getInstance().post(this);
        }

        @Subscribe
        public void taskResult(EncounterTaskResult result) {
            BusProvider.getInstance().unregister(this);
            Encounter resultEncounter = result.getEncounter();
            EncounterTaskResult.OperationType operationType = result.getOperationType();
            if (resultEncounter != null && encounter.getCreationDate().equals(resultEncounter.getCreationDate())) {
                if (result.isSuccess()) {
                    callback.onSuccess(resultEncounter, operationType);
                } else {
                    callback.onFailure(resultEncounter, operationType);
                }
            } else {
                callback.onFailure(resultEncounter, operationType);
            }
        }

        public Encounter getEncounter() {
            return encounter;
        }
    }
}
