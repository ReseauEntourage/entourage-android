package social.entourage.android.encounter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

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


public class CreateEncounterActivity extends EntourageSecuredActivity {

    private Encounter encounter;

    @Inject
    CreateEncounterPresenter presenter;

    @InjectView(R.id.edittext_message)
    EditText edtMessage;

    @InjectView(R.id.edittext_street_person_name)
    EditText edtStreetPersonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encounter_create);
        ButterKnife.inject(this);
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
        Encounter encounter = new Encounter();
        encounter.setUserName(getAuthenticationController().getUser().getFirstName());
        encounter.setLatitude(1.0);
        encounter.setLongitude(2.0);
        encounter.setMessage(edtMessage.getText().toString());
        encounter.setStreetPersonName(edtStreetPersonName.getText().toString());
        encounter.setCreationDate(new Date());

        presenter.createEncounter(encounter);
    }
}