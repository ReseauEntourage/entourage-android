package social.entourage.android.encounter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;


public class CreateEncounterActivity extends EntourageSecuredActivity {

    @Inject
    CreateEncounterPresenter presenter;

    @InjectView(R.id.edittext_message)
    EditText edtMessage;

    @InjectView(R.id.edittext_street_person_name)
    EditText edtStreetPersonName;

    @InjectView(R.id.textview_person_name)
    TextView txtPersonName;

    @InjectView(R.id.textview_met)
    TextView txtMet;

    @InjectView(R.id.textview_duration)
    TextView txtDuration;

    @InjectView(R.id.button_record)
    ImageButton btnStartStopRecording;

    @InjectView(R.id.button_play)
    ImageButton btnPlay;

    private MediaRecorder mediaRecorder;

    private boolean isRecording;

    private boolean hasAMessageBeenRecorded;

    private MediaPlayer mediaPlayer;

    private boolean isPlaying;

    private static String audioFileName;

    private Bundle arguments;

    private Handler durationHandler = new Handler();

    private long startRecordTime;

    long totalRecordTime;

    private Encounter encounter;

    private Runnable updateDurationThread = new Runnable() {

        @Override
        public void run() {
            totalRecordTime = System.currentTimeMillis() - startRecordTime;
            String duration = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(totalRecordTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalRecordTime)),
                    TimeUnit.MILLISECONDS.toSeconds(totalRecordTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalRecordTime)));
            txtDuration.setText(duration);
            durationHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encounter_create);
        arguments = getIntent().getExtras();
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("You must provide latitude and longitude");
        }

        ButterKnife.inject(this);
        txtPersonName.setText(getAuthenticationController().getUser().getFirstName());
        txtMet.setText(getString(R.string.encounter_encountered, Constants.FORMATER_DDMMYYYY.format(new Date())));
        btnPlay.setEnabled(false);
        isPlaying = false;
        isRecording = false;
        hasAMessageBeenRecorded = false;
        audioFileName = getFilesDir() + "/encounter.aac";
        new File(audioFileName).delete();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new CreateEncounterModule(this));
    }

    @OnClick(R.id.button_create_encounter)
    public void createEncounter() {
        encounter = new Encounter();
        encounter.setUserName(getAuthenticationController().getUser().getFirstName());
        encounter.setLatitude(arguments.getDouble(Constants.KEY_LATITUDE));
        encounter.setLongitude(arguments.getDouble(Constants.KEY_LONGITUDE));
        encounter.setMessage(edtMessage.getText().toString());
        encounter.setStreetPersonName(edtStreetPersonName.getText().toString());
        encounter.setCreationDate(new Date());

        if (hasAMessageBeenRecorded) {
            task.execute();
        } else {
            presenter.createEncounter(encounter);
        }
    }

    @OnClick(R.id.button_record)
    public void onClickOnStartStopRecording() {
        if (isRecording) {
            stopRecording();
            btnStartStopRecording.setImageResource(R.drawable.player_record);
            btnPlay.setEnabled(true);
        } else {
            startRecording();
            btnStartStopRecording.setImageResource(R.drawable.player_stop);
            btnPlay.setEnabled(false);
        }
        isRecording = !isRecording;
    }

    @OnClick(R.id.button_play)
    public void onClickOnPlay() {
        if (isPlaying) {
            stopPlaying();
            btnPlay.setImageResource(R.drawable.player_play);
            btnStartStopRecording.setEnabled(true);
        } else {
            startPlaying();
            btnPlay.setImageResource(R.drawable.player_stop);
            btnStartStopRecording.setEnabled(false);
        }
        isPlaying = !isPlaying;
    }

    private void startPlaying() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFileName);
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    btnPlay.setImageResource(R.drawable.player_play);
                    btnStartStopRecording.setEnabled(true);
                    isPlaying = false;
                }
            });

        } catch (IOException e) {
            throw new IllegalStateException("MediaPlayer.prepare() failed: " + e.getMessage(), e);
        }
    }

    private void stopPlaying() {
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mediaRecorder.setOutputFile(audioFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            throw new IllegalStateException("MediaRecorder.prepare() failed: " + e.getMessage(), e);
        }

        mediaRecorder.start();
        startRecordTime = System.currentTimeMillis();
        durationHandler.postDelayed(updateDurationThread, 0);
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        hasAMessageBeenRecorded = true;
        durationHandler.removeCallbacks(updateDurationThread);
    }

    private AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

        @Override
        protected void onPreExecute() {
            showProgressDialog(R.string.creating_encounter);
        }

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
                String title = MessageFormat.format(getString(R.string.soundcloud_track_title),
                        getAuthenticationController().getUser().getFirstName(),
                        edtStreetPersonName.getText().toString(),
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
                    Log.d(logTag, track.toString(4));
                    trackUrl = track.get("uri").toString();
                } else {
                    Log.e(logTag, "Invalid status received: " + response.getStatusLine());
                    trackUrl = null;
                }
            } catch (Exception e) {
                Log.e(logTag, "Problem when uploading to SoundCloud", e);
                throw new IllegalStateException("Problem when uploading to SoundCloud", e);
            }

            return trackUrl;
        }

        @Override
        protected void onPostExecute(String trackUrl) {
            if (trackUrl != null) {
                encounter.setVoiceMessageUrl(trackUrl);
            }
            presenter.createEncounter(encounter);
        }
    };

    public void createEncounterSuccess() {
        dismissProgressDialog();
        Toast.makeText(this, R.string.create_encounter_success, Toast.LENGTH_LONG).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtras(arguments);
        setResult(Constants.RESULT_CREATE_ENCOUNTER_OK, resultIntent);
        finish();
    }

    public void createEncounterFail(String errorMessage) {
        dismissProgressDialog();
        Toast.makeText(this, getString(R.string.create_encounter_failure) + errorMessage, Toast.LENGTH_LONG).show();
        Log.e(logTag, getString(R.string.create_encounter_failure) + errorMessage);
    }
}