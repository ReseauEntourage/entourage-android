package social.entourage.android.tour.encounter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.compat.Place;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.tape.Events;
import social.entourage.android.location.LocationFragment;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

public class CreateEncounterActivity extends EntourageSecuredActivity implements LocationFragment.OnFragmentInteractionListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String BUNDLE_KEY_TOUR_ID = "BUNDLE_KEY_TOUR_ID";
    public static final String BUNDLE_KEY_ENCOUNTER = "BUNDLE_KEY_ENCOUNTER";
    public static final String BUNDLE_KEY_LATITUDE = "BUNDLE_KEY_LATITUDE";
    public static final String BUNDLE_KEY_LONGITUDE = "BUNDLE_KEY_LONGITUDE";

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    CreateEncounterPresenter presenter;

    @BindView(R.id.edittext_message)
    EditText messageEditText;

    @BindView(R.id.edittext_street_person_name)
    EditText streetPersonNameEditText;

    @BindView(R.id.encounter_author)
    TextView encounterAuthor;

    @BindView(R.id.encounter_date)
    TextView encounterDate;

    @BindView(R.id.create_encounter_position)
    TextView positionTextView;

    private LatLng location;

    private Encounter editedEncounter;
    private boolean readOnly = true;

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
            editedEncounter = (Encounter)arguments.getSerializable(BUNDLE_KEY_ENCOUNTER);
            if (editedEncounter == null) {
                // Create mode
                readOnly = false;
                presenter.setTourUUID(arguments.getString(BUNDLE_KEY_TOUR_ID));
                presenter.setLatitude(arguments.getDouble(BUNDLE_KEY_LATITUDE));
                presenter.setLongitude(arguments.getDouble(BUNDLE_KEY_LONGITUDE));

                location = new LatLng(arguments.getDouble(BUNDLE_KEY_LATITUDE), arguments.getDouble(BUNDLE_KEY_LONGITUDE));
            } else {
                readOnly = editedEncounter.isReadOnly();
                location = new LatLng(editedEncounter.getLatitude(), editedEncounter.getLongitude());
            }
        }
        initialiseFields();
        EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_START);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (textMatchList!=null && !textMatchList.isEmpty()) {
                    if (messageEditText.getText().toString().equals("")) {
                        messageEditText.setText(textMatchList.get(0));
                    } else {
                        messageEditText.setText(messageEditText.getText() + " " + textMatchList.get(0));
                    }
                    messageEditText.setSelection(messageEditText.getText().length());
                    EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initialiseFields() {
        encounterAuthor.setText(getResources().getString(R.string.encounter_label_person_name_and, presenter.getAuthor()));
        if (editedEncounter != null) {
            streetPersonNameEditText.setText(editedEncounter.getStreetPersonName());
        }

        Date today = new Date();
        String todayDateString = DateFormat.getDateFormat(getApplicationContext()).format(today);
        if (editedEncounter != null && editedEncounter.getCreationDate() != null) {
            todayDateString = DateFormat.getDateFormat(getApplicationContext()).format(editedEncounter.getCreationDate());
        }
        encounterDate.setText(getResources().getString(R.string.encounter_encountered, todayDateString));

        if (editedEncounter != null) {
            messageEditText.setText(editedEncounter.getMessage());
        }

        if (location != null) {
            CreateEncounterActivity.GeocoderTask geocoderTask = new CreateEncounterActivity.GeocoderTask();
            geocoderTask.execute(location);
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    public void onCloseButton() {
        BusProvider.INSTANCE.getInstance().post(new Events.OnEncounterCreated(null));
        finish();
    }

    @OnClick(R.id.title_action_button)
    public void createEncounter() {
        String personName = streetPersonNameEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();
        if (!message.equals("") && !personName.equals("")) {
            showProgressDialog(editedEncounter == null ? R.string.creating_encounter : R.string.updating_encounter);
            if (editedEncounter == null) {
                presenter.createEncounter(messageEditText.getText().toString(), streetPersonNameEditText.getText().toString());
            } else {
                editedEncounter.setStreetPersonName(personName);
                editedEncounter.setMessage(message);
                presenter.updateEncounter(editedEncounter);
            }
        } else {
            if (personName.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.encounter_empty_name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.encounter_empty_fields, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.button_record)
    public void onRecord() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message));
        try {
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED);
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show();
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED);
        }
    }

    @OnClick(R.id.create_encounter_position_layout)
    protected void onPositionClicked() {
        hideKeyboard();
        LocationFragment fragment = LocationFragment.newInstance(location, positionTextView.getText().toString(), this);
        fragment.show(getSupportFragmentManager(), LocationFragment.TAG);
    }

    public void onCreateEncounterFinished(String errorMessage, Encounter encounterResponse) {
        dismissProgressDialog();
        String message;
        if (errorMessage == null) {
            getAuthenticationController().incrementUserEncountersCount();
            message = getString(R.string.create_encounter_success);
            BusProvider.INSTANCE.getInstance().post(new Events.OnEncounterCreated(encounterResponse));

            finish();
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_OK);
        } else {
            message = getString(R.string.create_encounter_failure);
            Timber.e(message);
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_FAILED);

        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void onUpdatingEncounterFinished(String errorMessage, Encounter encounterResponse) {
        dismissProgressDialog();
        String message;
        if (errorMessage == null) {
            getAuthenticationController().incrementUserEncountersCount();
            message = getString(R.string.update_encounter_success);
            BusProvider.INSTANCE.getInstance().post(new Events.OnEncounterUpdated(editedEncounter));

            finish();
            //EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_OK);
        } else {
            message = getString(R.string.update_encounter_failure);
            Timber.e(message);
            //EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_FAILED);

        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    // ----------------------------------
    // LocationFragment.OnFragmentInteractionListener
    // ----------------------------------

    public void onEntourageLocationChosen(LatLng location, String address, Place place) {
        if (location != null) {
            this.location = location;
            if (presenter != null) {
                presenter.setLatitude(location.latitude);
                presenter.setLongitude(location.longitude);
            }
            if (editedEncounter != null) {
                editedEncounter.setLatitude(location.latitude);
                editedEncounter.setLongitude(location.longitude);
            }
            if (address != null) {
                positionTextView.setText(address);
            } else {
                CreateEncounterActivity.GeocoderTask geocoderTask = new CreateEncounterActivity.GeocoderTask();
                geocoderTask.execute(location);
            }
        }
    }

    // ----------------------------------
    // PRIVATE CLASSES
    // ----------------------------------
    //TODO check if this leaks!
    private class GeocoderTask extends AsyncTask<LatLng, Void, String> {

        @Override
        protected String doInBackground(final LatLng... params) {
            try {
                Geocoder geoCoder = new Geocoder(CreateEncounterActivity.this, Locale.getDefault());
                LatLng location = params[0];
                List<Address> addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    if (address.getMaxAddressLineIndex() >= 0) {
                        return addresses.get(0).getAddressLine(0);
                    }
                }
            }
            catch (IOException ignored) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final String address) {
            if (address != null) {
                CreateEncounterActivity.this.positionTextView.setText(address);
            }
        }
    }
}