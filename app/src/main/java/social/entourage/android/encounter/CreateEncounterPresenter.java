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

public class CreateEncounterPresenter {

    private final CreateEncounterActivity activity;

    private final EncounterRequest encounterRequest;

    public boolean twitterChecked;

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
        encounterRequest.create(encounterWrapper, new EncounterRequestCallback());
    }

    public void tweetWithAudioFile(Encounter encounter) {
        String tweet = String.format(activity.getString(R.string.tweet_with_audio),
                                     encounter.getStreetPersonName(),
                                     encounter.getSoundCloudPermalinkUrl(),
                                     Constants.HASHTAG,
                                     Constants.TWITTER_ENTOURAGE_ACCOUNT_NAME);
        sendTweet(tweet);
    }

    public void tweetWithoutAudioFile(Encounter encounter) {
        String tweet = String.format(activity.getString(R.string.tweet_without_audio),
                                     encounter.getStreetPersonName(),
                                     Constants.HASHTAG,
                                     Constants.TWITTER_ENTOURAGE_ACCOUNT_NAME);
        sendTweet(tweet);
    }

    private void sendTweet(String tweet) {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, tweet);
        tweetIntent.setType("text/plain");

        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;

        for (ResolveInfo resolveInfo: resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }

        if (resolved) {
            activity.startActivity(tweetIntent);
        } else {
            Toast.makeText(activity, "Twitter app not found", Toast.LENGTH_LONG).show();
        }
    }

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
            if(twitterChecked) {
                tweetWithoutAudioFile(this.encounter);
            }
        }

        @Override
        public void onRequestSuccess(Encounter encounter) {
            createEncounter(encounter);
            if(twitterChecked) {
                tweetWithAudioFile(encounter);
            }
        }
    }

    private final class EncounterRequestCallback implements Callback<EncounterResponse> {

        @Override
        public void success(EncounterResponse encounterResponse, Response response) {
            activity.onCreateEncounterFinished(null);
        }

        @Override
        public void failure(RetrofitError error) {
            activity.onCreateEncounterFinished(error.toString());
        }
    }
}
