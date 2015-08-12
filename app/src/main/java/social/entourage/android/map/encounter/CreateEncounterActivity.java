package social.entourage.android.map.encounter;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.Constants;
import social.entourage.android.authentication.login.LoginActivity;

@SuppressWarnings("WeakerAccess")
public class CreateEncounterActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    CreateEncounterPresenter presenter;

    @InjectView(R.id.edittext_message)
    EditText edtMessage;

    @InjectView(R.id.edittext_street_person_name)
    EditText edtStreetPersonName;

    @InjectView(R.id.button_record)
    ImageButton btnStartStopRecording;

    @InjectView(R.id.button_play)
    ImageButton btnPlay;

    private final SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);

    private MediaRecorder mediaRecorder;

    private boolean isRecording;

    private boolean hasAMessageBeenRecorded;

    private MediaPlayer mediaPlayer;

    private boolean isPlaying;

    private String audioFileName;

    private Bundle arguments;

    private final Handler durationHandler = new Handler();

    private long startRecordTime;

    long totalRecordTime;

    private final Runnable updateDurationThread = new Runnable() {

        @Override
        public void run() {
            totalRecordTime = System.currentTimeMillis() - startRecordTime;
            durationHandler.postDelayed(this, 0);
        }
    };

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_encounter_create);
        ButterKnife.inject(this);

        arguments = getIntent().getExtras();
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("You must provide latitude and longitude");
        }

        if (!getAuthenticationController().isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        btnPlay.setEnabled(false);
        isPlaying = false;
        isRecording = false;
        hasAMessageBeenRecorded = false;
        audioFileName = getFilesDir() + "/encounter.aac";
        boolean isDelete = new File(audioFileName).delete();
        if (isDelete) {
            Log.v(this.getLogTag(), "no need to delete audio file");
        }
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerCreateEncounterComponent.builder()
                .entourageComponent(entourageComponent)
                .createEncounterModule(new CreateEncounterModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
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
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @OnClick(R.id.button_create_encounter)
    public void createEncounter() {
        Encounter encounter = new Encounter();
        encounter.setTourId(arguments.getLong(Constants.KEY_TOUR_ID));
        encounter.setUserName(getAuthenticationController().getUser().getFirstName());
        encounter.setLatitude(arguments.getDouble(Constants.KEY_LATITUDE));
        encounter.setLongitude(arguments.getDouble(Constants.KEY_LONGITUDE));
        encounter.setMessage(edtMessage.getText().toString());
        encounter.setStreetPersonName(edtStreetPersonName.getText().toString());
        encounter.setCreationDate(new Date());

        if (hasAMessageBeenRecorded) {
            presenter.createTrackOnSoundCloud(encounter, audioFileName);
        } else {
            presenter.createEncounter(encounter);
        }
    }

    @OnClick(R.id.button_record)
    public void onClickOnStartStopRecording() {
        if (isRecording) {
            stopRecording();
            btnStartStopRecording.setImageResource(R.drawable.ic_action_stop_sound);
            btnPlay.setEnabled(true);
        } else {
            startRecording();
            btnStartStopRecording.setImageResource(R.drawable.ic_action_stop_sound);
            btnPlay.setEnabled(false);
        }
        isRecording = !isRecording;
    }

    @OnClick(R.id.button_play)
    public void onClickOnPlay() {
        if (isPlaying) {
            stopPlaying();
            btnPlay.setImageResource(R.drawable.ic_action_play_sound);
            btnStartStopRecording.setEnabled(true);
        } else {
            startPlaying();
            btnPlay.setImageResource(R.drawable.ic_action_stop_sound);
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
                    btnPlay.setImageResource(R.drawable.ic_action_play_sound);
                    btnStartStopRecording.setEnabled(true);
                    isPlaying = false;
                }
            });

        } catch (IOException e) {
            Log.e(this.getLogTag(), "MediaPlayer.prepare() failed: " + e.getMessage(), e);
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
        mediaRecorder.setOutputFile(audioFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        }

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(this.getLogTag(), "MediaRecorder.prepare() failed: " + e.getMessage(), e);
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

    public void onCreateEncounterFinished(String errorMessage, Encounter encounterResponse) {
        dismissProgressDialog();
        String message;
        if (errorMessage == null) {
            getAuthenticationController().incrementUserEncountersCount();
            message = getString(R.string.create_encounter_success);
            Intent resultIntent = new Intent();
            arguments.putSerializable(Constants.KEY_ENCOUNTER, encounterResponse);
            resultIntent.putExtras(arguments);
            setResult(Constants.RESULT_CREATE_ENCOUNTER_OK, resultIntent);
            finish();
        } else {
            message = getString(R.string.create_encounter_failure, errorMessage);
            Log.e(logTag, getString(R.string.create_encounter_failure) + errorMessage);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}