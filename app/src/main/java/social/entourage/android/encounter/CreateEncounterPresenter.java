package social.entourage.android.encounter;

import android.os.AsyncTask;
import android.util.Log;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Http;
import com.soundcloud.api.Params;
import com.soundcloud.api.Request;
import com.soundcloud.api.Token;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.BuildConfig;
import social.entourage.android.R;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.EncounterService;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;

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

    public void postTrackOnSoundCloud(final Encounter encounter, final String audioFileName) {

        activity.showProgressDialog(R.string.creating_encounter);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                String trackUrl = null;
                final ApiWrapper wrapper = new ApiWrapper(
                        BuildConfig.SOUNDCLOUND_ID,
                        BuildConfig.SOUNDCLOUND_SECRET,
                        null,
                        null
                );

                Token token = null;
                try {
                    token = wrapper.login(BuildConfig.SOUNDCLOUND_USER, BuildConfig.SOUNDCLOUND_PASS);
                    String title = MessageFormat.format(activity.getString(R.string.soundcloud_track_title),
                            activity.getAuthenticationController().getUser().getFirstName(),
                            encounter.getStreetPersonName(),
                            Constants.FORMATER_DDMMYYYY.format(new Date()),
                            Constants.FORMATER_HHMM.format(new Date())
                    );

                    HttpResponse response = wrapper.post(Request.to(Endpoints.TRACKS)
                                    .add(Params.Track.TITLE, title)
                                    .add(Params.Track.TAG_LIST, "dev")
                                    .withFile(Params.Track.ASSET_DATA, new File(audioFileName))
                    );

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        JSONObject track = Http.getJSON(response);
                        Log.d(activity.getLogTag(), track.toString(4));
                        trackUrl = track.get("uri").toString();
                    } else {
                        Log.e(activity.getLogTag(), "Invalid status received: " + response.getStatusLine());
                        trackUrl = null;
                    }
                } catch (Exception e) {
                    Log.e(activity.getLogTag(), "Problem when uploading to SoundCloud", e);
                    throw new IllegalStateException("Problem when uploading to SoundCloud", e);
                }

                return trackUrl;
            }

            @Override
            protected void onPostExecute(String trackUrl) {
                onSoundTrackedPostFinished(encounter, trackUrl);
            }
        }.execute();
    }

    private void onSoundTrackedPostFinished(Encounter encounter, String trackUrl) {
        if (trackUrl != null) {
            encounter.setVoiceMessageUrl(trackUrl);
        }
        createEncounter(encounter);
    }

    public void createEncounter(Encounter encounter) {
        encounterService.create(encounter, this);
    }

    @Override
    public void success(EncounterResponse encounterResponse, Response response) {
        activity.onCreateEncounterFinished(null);
    }

    @Override
    public void failure(RetrofitError error) {
        activity.onCreateEncounterFinished(error.toString());
    }
}
