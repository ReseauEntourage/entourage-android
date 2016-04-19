package social.entourage.android.map.encounter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.Constants;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;

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
        /*
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        */
        displayEncounter();
        new GeocoderTask().execute(encounter);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.encounter_read_close)
    public void onCloseButton() {
        finish();
    }

    public void displayEncounter() {
        String location = "";
        Address address = encounter.getAddress();
        if (address != null) {
            if (address.getMaxAddressLineIndex() > 0) {
                location = address.getAddressLine(0);
            }
        }
        String encounterDate = "";
        if (encounter.getCreationDate() != null) {
            encounterDate = DateFormat.getDateFormat(getApplicationContext()).format(encounter.getCreationDate());
        }
        String encounterLocation = getResources().getString(R.string.encounter_read_location,
                encounter.getUserName(),
                encounter.getStreetPersonName(),
                location,
                encounterDate);
        streetPersonNameEditText.setText(encounterLocation);
        messageEditText.setText(encounter.getMessage());
    }

    private class GeocoderTask extends AsyncTask<Encounter, Void, Encounter> {

        @Override
        protected Encounter doInBackground(final Encounter... params) {
            try {
                Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                Encounter encounter = params[0];
                List<Address> addresses = geoCoder.getFromLocation(encounter.getLatitude(), encounter.getLongitude(), 1);
                if (addresses.size() > 0) {
                    encounter.setAddress(addresses.get(0));
                }
                return encounter;
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final Encounter encounter) {
            displayEncounter();
        }
    }
}