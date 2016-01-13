package social.entourage.android.map.encounter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.Constants;

@SuppressWarnings("WeakerAccess")
public class ReadEncounterActivity extends EntourageActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String BUNDLE_KEY_ENCOUNTER = "BUNDLE_KEY_ENCOUNTER";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Encounter encounter;

    @Inject
    ReadEncounterPresenter presenter;

    @Bind(R.id.edittext_street_person_name)
    EditText streetPersonNameEditText;

    @Bind(R.id.edittext_message)
    EditText messageEditText;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_encounter_read);
        ButterKnife.bind(this);

        FlurryAgent.logEvent(Constants.EVENT_OPEN_ENCOUNTER_FROM_MAP);
        Bundle args = getIntent().getExtras();
        encounter = (Encounter)args.get(BUNDLE_KEY_ENCOUNTER);
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
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        presenter.displayEncounter();
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void displayEncounter() {
        streetPersonNameEditText.setText(encounter.getStreetPersonName());
        messageEditText.setText(encounter.getMessage());
     }
}