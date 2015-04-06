package social.entourage.android.encounter;

import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;

import social.entourage.android.api.SoundCloudGetTrackRequest;
import social.entourage.android.api.model.map.Encounter;

/**
 * Presenter controlling the main activity
 */
public class ReadEncounterPresenter {
    private final ReadEncounterActivity activity;

    public ReadEncounterPresenter(final ReadEncounterActivity activity) {
        this.activity = activity;
    }

    public void displayEncounter(Encounter encounter, String audioFileName) {
        activity.displayEncounter(encounter);
        if (encounter.getVoiceMessageUrl() != null) {
            downloadAudiMessageFromSoundCloud(encounter.getVoiceMessageUrl(), audioFileName);
        }
    }

    public void downloadAudiMessageFromSoundCloud(String trackUrl, String audioFileName) {
        SoundCloudGetTrackRequest request = new SoundCloudGetTrackRequest(
                trackUrl,
                audioFileName
        );
        activity.getSpiceManager().execute(request, new SoundCloudGetRequestCallback());
    }

    private final class SoundCloudGetRequestCallback implements RequestListener<Void> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            activity.disablePlayer();
            Toast.makeText(activity, "Unable to download track from SoundCloud ", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(Void v) {
            activity.enablePlayer();
        }
    }
}
