package com.octo.entourage.encounter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.octo.entourage.EntourageActivity;
import com.octo.entourage.R;
import com.octo.entourage.model.Constants;
import com.octo.entourage.model.Encounter;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class EncounterActivity extends EntourageActivity {

    private Encounter encounter;

    @Inject
    EncounterPresenter presenter;

    @InjectView(R.id.edittext_creation_date)
    EditText edtCreationDate;

    @InjectView(R.id.edittext_person_name)
    EditText edtPersonName;

    @InjectView(R.id.edittext_street_person_name)
    EditText edtStreetPersonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encounter);
        ButterKnife.inject(this);

        Bundle args = getIntent().getExtras();
        encounter = (Encounter)args.get(Constants.KEY_ENCOUNTER_ID);
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
        return Arrays.<Object>asList(new EncounterModule(this));
    }

    public void displayEncounter(Encounter encounter) {
        edtCreationDate.setText(encounter.getCreationDate().toString());
        edtPersonName.setText(encounter.getUserName());
        edtStreetPersonName.setText(encounter.getStreetPersonName());
    }
}
