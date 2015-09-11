package social.entourage.android.map.encounter;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.Constants;

@SuppressWarnings("WeakerAccess")
public class ReadEncounterActivity extends EntourageActivity {

    private Encounter encounter;

    @Inject
    ReadEncounterPresenter presenter;

    @InjectView(R.id.edittext_street_person_name)
    EditText edtStreetPersonName;

    @InjectView(R.id.edittext_message)
    EditText edtMessage;

    @InjectView(R.id.layout_player)
    View layoutPlayer;

    @InjectView(R.id.textview_duration)
    TextView txtDuration;

    @InjectView(R.id.button_play)
    ImageButton btnPlay;

    private MediaPlayer mediaPlayer;

    private boolean isPlaying;

    private final Handler durationHandler = new Handler();

    private long startPlayTime;

    long totalPlayTime;

    private final Runnable updateDurationThread = new Runnable() {

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

        btnPlay.setEnabled(false);
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerReadEncounterComponent.builder()
                .entourageComponent(entourageComponent)
                .readEncounterModule(new ReadEncounterModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.displayEncounter();

    }

    @OnClick(R.id.button_play)
    public void onClickOnPlay() {
        if (isPlaying) {
            stopPlaying();
            btnPlay.setImageResource(R.drawable.ic_action_play_sound);
        } else {
            startPlaying();
            btnPlay.setImageResource(R.drawable.ic_action_stop_sound);
        }
        isPlaying = !isPlaying;
    }

    private void startPlaying() {
        if(mediaPlayer==null) {
            return;
        }
        mediaPlayer.start();

        startPlayTime = System.currentTimeMillis();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnPlay.setImageResource(R.drawable.ic_action_play_sound);
                durationHandler.removeCallbacks(updateDurationThread);
                isPlaying = false;
            }
        });

        durationHandler.postDelayed(updateDurationThread, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer!=null) {
            mediaPlayer.release();
        }
    }

    private void stopPlaying() {
        if(mediaPlayer==null) {
            return;
        }
        mediaPlayer.stop();
        btnPlay.setEnabled(false);
        mediaPlayer.prepareAsync();
        durationHandler.removeCallbacks(updateDurationThread);
    }

    public void displayEncounter() {
        edtStreetPersonName.setText(encounter.getStreetPersonName());
        edtMessage.setText(encounter.getMessage());

        if (encounter.getVoiceMessageUrl() == null) {
            layoutPlayer.setVisibility(View.GONE);
            btnPlay.setEnabled(false);
        } else {
            layoutPlayer.setVisibility(View.VISIBLE);

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, Uri.parse(encounter.getVoiceMessageUrl() + "?client_id=" + BuildConfig.SOUNDCLOUND_ID));
                mediaPlayer.setOnPreparedListener(trackPreparedListner);
                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                Log.e(this.getLogTag(), "MediaPlayer.setDataSource() failed: " + e.getMessage(), e);
            }
        }
     }

    private final MediaPlayer.OnPreparedListener trackPreparedListner = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mediaPlayer) {
            btnPlay.setEnabled(true);
        }
    };

    public void disablePlayer() {
        layoutPlayer.setVisibility(View.GONE);
    }

    public void enablePlayer() {
        btnPlay.setEnabled(true);
    }
}