package social.entourage.android.encounter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.R;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.SoundCloudCreateTrackRequest;
import social.entourage.android.api.model.EncounterWrapper;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;

/**
 * Module related to CreateEncounterPresenter
 * @see CreateEncounterPresenter
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
    public CreateEncounterPresenter(
            final CreateEncounterActivity activity,
            final EncounterRequest encounterRequest
    ) {
        this.activity = activity;
        this.encounterRequest = encounterRequest;
    }

    public void createTrackOnSoundCloud(final Encounter encounter, final String audioFileName) {
        activity.showProgressDialog(R.string.creating_encounter_audio);
        SoundCloudCreateTrackRequest request = new SoundCloudCreateTrackRequest(
                encounter,
                audioFileName,
                activity.getString(R.string.soundcloud_track_title)
        );
        activity.getSpiceManager().execute(request, new SoundCloudRequestCallback(encounter));
    }

    public void createEncounter(Encounter encounter) {
        activity.showProgressDialog(R.string.creating_encounter);
        EncounterWrapper encounterWrapper = new EncounterWrapper();
        encounterWrapper.setEncounter(encounter);
        encounterRequest.create(encounter.getTourId(), encounterWrapper, new EncounterRequestCallback());
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private final class SoundCloudRequestCallback implements RequestListener<Encounter> {

        // this field is added so that it's still possible to create the encounter even if the call to SoundCloud fails
        // it's the same object as the one received by onRequestSuccess(Encounter)
        private final Encounter encounter;

        private SoundCloudRequestCallback(final Encounter encounter) {
            this.encounter = encounter;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(activity,
                    "Error creating audio message on SoundCloud: " + spiceException.getMessage(), Toast.LENGTH_LONG)
                    .show();
            createEncounter(this.encounter);
        }

        @Override
        public void onRequestSuccess(Encounter encounter) {
            createEncounter(encounter);
        }
    }

    private final class EncounterRequestCallback implements Callback<EncounterResponse> {

        @Override
        public void success(EncounterResponse encounterResponse, Response response) {
            activity.onCreateEncounterFinished(null, encounterResponse.getEncounter());
        }

        @Override
        public void failure(RetrofitError error) {
            activity.onCreateEncounterFinished(error.toString(), null);
        }
    }
}
