package social.entourage.android.map.encounter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Date;
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

    @Bind(R.id.button_record)
    ImageButton recordButton;

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
        Encounter encounter = new Encounter();
        Bundle arguments = getIntent().getExtras();
        encounter.setTourId(arguments.getLong(Constants.KEY_TOUR_ID));
        encounter.setUserName(getAuthenticationController().getUser().getFirstName());
        encounter.setLatitude(arguments.getDouble(Constants.KEY_LATITUDE));
        encounter.setLongitude(arguments.getDouble(Constants.KEY_LONGITUDE));
        encounter.setMessage(messageEditText.getText().toString());
        encounter.setStreetPersonName(streetPersonNameEditText.getText().toString());
        encounter.setCreationDate(new Date());

        presenter.createEncounter(encounter);
    }

    @OnClick(R.id.button_record)
    public void onRecord() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message));
        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show();
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
            arguments.putSerializable(Constants.KEY_ENCOUNTER, encounterResponse);
            resultIntent.putExtras(arguments);
            setResult(Constants.RESULT_CREATE_ENCOUNTER_OK, resultIntent);
            finish();
        } else {
            message = getString(R.string.create_encounter_failure, errorMessage);
            Log.e(logTag, getString(R.string.create_encounter_failure) + errorMessage);
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}