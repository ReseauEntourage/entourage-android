package social.entourage.android.encounter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;


public class CreateEncounterActivity extends EntourageSecuredActivity {

    private Encounter encounter;

    @Inject
    CreateEncounterPresenter presenter;

    @InjectView(R.id.edittext_message)
    EditText edtMessage;

    @InjectView(R.id.edittext_street_person_name)
    EditText edtStreetPersonName;

    @InjectView(R.id.textview_met)
    TextView txtMet;

    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encounter_create);
        ButterKnife.inject(this);
        txtMet.setText(getString(R.string.encounter_encountered, Constants.FORMATER_DDMMYYYY.format(new Date())));
        args = getIntent().getExtras();

        if (args == null || args.isEmpty()) {
            throw new IllegalArgumentException("You must provide latitude and longitude");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        double latitude = args.getDouble(Constants.KEY_LATITUDE);
        double longitude = args.getDouble(Constants.KEY_LONGITUDE);

        Encounter encounter = new Encounter();
        encounter.setUserName(getAuthenticationController().getUser().getFirstName());
        encounter.setLatitude(latitude);
        encounter.setLongitude(longitude);
        encounter.setMessage(edtMessage.getText().toString());
        encounter.setStreetPersonName(edtStreetPersonName.getText().toString());
        encounter.setCreationDate(new Date());

        presenter.createEncounter(encounter);
    }
}