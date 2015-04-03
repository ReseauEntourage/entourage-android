package social.entourage.android.encounter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encounter_read);
        ButterKnife.inject(this);

        FlurryAgent.logEvent(Constants.EVENT_OPEN_ENCOUNTER_FROM_MAP);
        Bundle args = getIntent().getExtras();
        encounter = (Encounter)args.get(Constants.KEY_ENCOUNTER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.displayEncounter(encounter);
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
        return Arrays.<Object>asList(new ReadEncounterModule(this));
    }

    public void displayEncounter(Encounter encounter) {
        txtPersonName.setText(encounter.getUserName() + " ");
        edtStreetPersonName.setText(encounter.getStreetPersonName());
        txtMet.setText(getString(R.string.encounter_encountered, Constants.FORMATER_DDMMYYYY.format(encounter.getCreationDate())));
     }
}