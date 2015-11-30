package social.entourage.android.map.encounter;

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
public class CreateEncounterPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    public interface EncounterUploadCallback {
        void onSuccess();
        void onFailure();
    }

    private final CreateEncounterActivity activity;
    private final AuthenticationController authenticationController;
    private final EncounterTapeTaskQueue queue;

    private long tourId;
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
        encounter.setUserName(authenticationController.getUser().getFirstName());
        encounter.setMessage(message);
        encounter.setStreetPersonName(streetPersonName);
        encounter.setCreationDate(new Date());
        encounter.setTourId(tourId);
        encounter.setLatitude(latitude);
        encounter.setLongitude(longitude);


        queue.add(new EncounterUploadTask(encounter));
        activity.onCreateEncounterFinished(null, encounter);
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

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public class EncounterUploadTask implements Task<EncounterUploadCallback>, Serializable {

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
            if (resultEncounter != null && encounter.getCreationDate().equals(resultEncounter.getCreationDate())) {
                if (result.isSuccess()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure();
                }
            } else {
                callback.onFailure();
            }
        }

        public Encounter getEncounter() {
            return encounter;
        }
    }
}
