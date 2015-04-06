package social.entourage.android.api;

import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
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

import social.entourage.android.BuildConfig;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;

public class SoundCloudGetTrackRequest extends SpiceRequest<Void> {

    private final String trackUrl;

    private final String audioFileName;

    public SoundCloudGetTrackRequest(String trackUrl, String audioFileName) {
        super(Void.class);
        this.trackUrl = trackUrl;
        this.audioFileName = audioFileName;
    }

    @Override
    public Void loadDataFromNetwork() throws Exception {
        final ApiWrapper wrapper = new ApiWrapper(
                BuildConfig.SOUNDCLOUND_ID,
                BuildConfig.SOUNDCLOUND_SECRET,
                null,
                null
        );

        Token token = null;
        try {
            token = wrapper.login(BuildConfig.SOUNDCLOUND_USER, BuildConfig.SOUNDCLOUND_PASS);

            final Request resource = Request.to(trackUrl);

            System.out.println("GET " + resource);
            HttpResponse resp = wrapper.get(resource);

            // TODO Ici l'api me répond avec un 302
            // TODO Si j'appelle le nouvelle url je reçois de nouveau un 302...

            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                JSONObject track = Http.getJSON(resp);
                Log.d(this.getClass().getSimpleName(), track.toString(4));
            }
            else {
                System.err.println("Invalid status received: " + resp.getStatusLine());
            }
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Problem when uploading to SoundCloud", e);
            throw new IllegalStateException("Problem when uploading to SoundCloud", e);
        }

        return null;
    }
}
