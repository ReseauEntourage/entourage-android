package social.entourage.android.api;

import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Http;
import com.soundcloud.api.Params;
import com.soundcloud.api.Request;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import social.entourage.android.BuildConfig;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.Constants;

public class SoundCloudCreateTrackRequest extends SpiceRequest<Encounter> {

    private static final String TRACK_STREAM_URL = "stream_url";
    private static final String PERMALINK_URL = "permalink_url";

    private final Encounter encounter;

    private final String audioFileName;

    private final String soundCloudTrackTitle;

    public SoundCloudCreateTrackRequest(Encounter encounter, String audioFileName, String soundCloudTrackTitle) {
        super(Encounter.class);
        this.encounter = encounter;
        this.audioFileName = audioFileName;
        this.soundCloudTrackTitle = soundCloudTrackTitle;
    }

    @Override
    public Encounter loadDataFromNetwork() throws Exception {
        final ApiWrapper wrapper = new ApiWrapper(
                BuildConfig.SOUNDCLOUND_ID,
                BuildConfig.SOUNDCLOUND_SECRET,
                null,
                null
        );

        try {
            wrapper.login(BuildConfig.SOUNDCLOUND_USER, BuildConfig.SOUNDCLOUND_PASS);
            String title = MessageFormat.format(soundCloudTrackTitle,
                    encounter.getUserName(),
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
                Log.d(this.getClass().getSimpleName(), track.toString(4));
                encounter.setVoiceMessageUrl(track.get(TRACK_STREAM_URL).toString());
                encounter.setSoundCloudPermalinkUrl(track.get(PERMALINK_URL).toString());
            } else {
                Log.e(this.getClass().getSimpleName(), "Invalid status received: " + response.getStatusLine());
                encounter.setVoiceMessageUrl(null);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Problem when uploading to SoundCloud", e);
            throw new IllegalStateException("Problem when uploading to SoundCloud", e);
        }

        return encounter;
    }
}
