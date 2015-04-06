package social.entourage.android.encounter;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;

public class ReadEncounterActivity extends EntourageActivity {

    private Encounter encounter;

    @Inject
    ReadEncounterPresenter presenter;

    @InjectView(R.id.textview_person_name)
    TextView txtPersonName;

    @InjectView(R.id.edittext_street_person_name)
    EditText edtStreetPersonName;

    @InjectView(R.id.textview_met)
    TextView txtMet;

    @InjectView(R.id.edittext_message)
    EditText edtMessage;

    @InjectView(R.id.textview_listen_to_voice_message)
    View txtListenToMessage;

    @InjectView(R.id.layout_player)
    View layoutPlayer;

    @InjectView(R.id.textview_duration)
    TextView txtDuration;

    @InjectView(R.id.button_play)
    ImageButton btnPlay;

    private SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);

    private MediaPlayer mediaPlayer;

    private boolean isPlaying;

    private String audioFileName;

    private Handler durationHandler = new Handler();

    private long startPlayTime;

    long totalPlayTime;

    private Runnable updateDurationThread = new Runnable() {

        @Override
        public void run() {
            totalPlayTime = System.currentTimeMillis() - startPlayTime;
            String duration = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(totalPlayTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalPlayTime)),
                    TimeUnit.MILLISECONDS.toSeconds(totalPlayTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalPlayTime)));
            txtDuration.setText(duration);
            durationHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encounter_read);
        ButterKnife.inject(this);

        FlurryAgent.logEvent(Constants.EVENT_OPEN_ENCOUNTER_FROM_MAP);
        Bundle args = getIntent().getExtras();
        encounter = (Encounter)args.get(Constants.KEY_ENCOUNTER);
        audioFileName = getFilesDir() + "/encounter_downloaded.aac";
        new File(audioFileName).delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
        presenter.displayEncounter(encounter, audioFileName);

    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }


    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new ReadEncounterModule(this));
    }

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @OnClick(R.id.button_play)
    public void onClickOnPlay() {
        if (isPlaying) {
            stopPlaying();
            btnPlay.setImageResource(R.drawable.player_play);
        } else {
            startPlaying();
            btnPlay.setImageResource(R.drawable.player_stop);
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
                    durationHandler.removeCallbacks(updateDurationThread);
                    isPlaying = false;
                }
            });

        } catch (IOException e) {
            Log.e(this.getLogTag(), "MediaPlayer.prepare() failed: " + e.getMessage(), e);
            throw new IllegalStateException("MediaPlayer.prepare() failed: " + e.getMessage(), e);
        }

        durationHandler.postDelayed(updateDurationThread, 0);
    }

    private void stopPlaying() {
        mediaPlayer.release();
        mediaPlayer = null;
        durationHandler.removeCallbacks(updateDurationThread);
    }

    public void displayEncounter(Encounter encounter) {
        txtPersonName.setText(getString(R.string.encounter_label_person_name_and, encounter.getUserName()));
        edtStreetPersonName.setText(encounter.getStreetPersonName());
        edtMessage.setText(encounter.getMessage());
        txtMet.setText(getString(R.string.encounter_encountered, Constants.FORMATER_DDMMYYYY.format(encounter.getCreationDate())));
        // tant qu'un message audio n'est pas downloadé le bouton n'est pas utilisable
        btnPlay.setEnabled(false);

        if (encounter.getVoiceMessageUrl() == null) {
            txtListenToMessage.setVisibility(View.GONE);
            layoutPlayer.setVisibility(View.GONE);
        }
     }

    public void disablePlayer() {
        txtListenToMessage.setVisibility(View.GONE);
        layoutPlayer.setVisibility(View.GONE);
    }

    public void enablePlayer() {
        btnPlay.setEnabled(true);
    }
}