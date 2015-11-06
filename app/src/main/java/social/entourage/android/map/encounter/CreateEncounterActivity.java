package social.entourage.android.map.encounter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.Constants;

public class CreateEncounterActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String BUNDLE_KEY_TOUR_ID = "BUNDLE_KEY_ENCOUNTER";
    public static final String BUNDLE_KEY_ENCOUNTER = "BUNDLE_KEY_ENCOUNTER";
    public static final String BUNDLE_KEY_LATITUDE = "BUNDLE_KEY_LATITUDE";
    public static final String BUNDLE_KEY_LONGITUDE = "BUNDLE_KEY_LONGITUDE";

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    CreateEncounterPresenter presenter;

    @Bind(R.id.edittext_message)
    EditText messageEditText;

    @Bind(R.id.edittext_street_person_name)
    EditText streetPersonNameEditText;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_encounter_create);
        ButterKnife.bind(this);

        Bundle arguments = getIntent().getExtras();

        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("You must provide latitude and longitude");
        } else {
            presenter.setTourId(arguments.getLong(BUNDLE_KEY_TOUR_ID));
            presenter.setLatitude(arguments.getDouble(BUNDLE_KEY_LATITUDE));
            presenter.setLongitude(arguments.getDouble(BUNDLE_KEY_LONGITUDE));
        }
        FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_START);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!textMatchList.isEmpty()) {
                    messageEditText.setText(textMatchList.get(0));
                    FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.button_create_encounter)
    public void createEncounter() {
        showProgressDialog(R.string.creating_encounter);
        presenter.createEncounter(messageEditText.getText().toString(), streetPersonNameEditText.getText().toString());
        FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_OK);
    }

    @OnClick(R.id.button_record)
    public void onRecord() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message));
        try {
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED);
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show();
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED);
        }
    }

    public void onCreateEncounterFinished(String errorMessage, Encounter encounterResponse) {
        dismissProgressDialog();
        String message;
        if (errorMessage == null) {
            getAuthenticationController().incrementUserEncountersCount();
            message = getString(R.string.create_encounter_success);
            Intent resultIntent = new Intent();
            Bundle arguments = getIntent().getExtras();
            arguments.putSerializable(BUNDLE_KEY_ENCOUNTER, encounterResponse);
            resultIntent.putExtras(arguments);
            setResult(Constants.RESULT_CREATE_ENCOUNTER_OK, resultIntent);
            finish();
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_OK);
        } else {
            message = getString(R.string.create_encounter_failure, errorMessage);
            Log.e(logTag, message);
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_FAILED);

        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}