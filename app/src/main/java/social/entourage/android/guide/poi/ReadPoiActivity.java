package social.entourage.android.guide.poi;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.Constants;

/**
 * Activity showing the detail of a POI
 */
public class ReadPoiActivity extends EntourageActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String BUNDLE_KEY_POI = "BUNDLE_KEY_POI";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Poi poi;

    @Inject
    ReadPoiPresenter presenter;

    @Bind(R.id.textview_poi_name)
    TextView txtPoiName;
    @Bind(R.id.textview_poi_description)
    TextView txtPoiDesc;
    @Bind(R.id.button_poi_phone)
    Button btnPoiPhone;
    @Bind(R.id.button_poi_mail)
    Button btnPoiMail;
    @Bind(R.id.button_poi_web)
    Button btnPoiWeb;
    @Bind(R.id.button_poi_address)
    Button btnPoiAddress;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_poi_read);
        ButterKnife.bind(this);

        FlurryAgent.logEvent(Constants.EVENT_OPEN_POI_FROM_MAP);
        Bundle extras = getIntent().getExtras();
        poi = (Poi) extras.get(BUNDLE_KEY_POI);
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerReadPoiComponent.builder()
                .entourageComponent(entourageComponent)
                .readPoiModule(new ReadPoiModule(this))
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
        presenter.displayPoi(poi);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_user) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayPoi(Poi poi, final ReadPoiPresenter.OnAddressClickListener onAddressClickListener) {
        txtPoiName.setText(poi.getName());
        txtPoiDesc.setText(poi.getDescription());
        setActionButton(btnPoiPhone, poi.getPhone());
        setActionButton(btnPoiMail, poi.getEmail());
        setActionButton(btnPoiWeb, poi.getWebsite());
        setActionButton(btnPoiAddress, poi.getAdress());
        btnPoiAddress.setOnClickListener(onAddressClickListener);
    }

    public void setActionButton(Button btn, String value) {
        if(value.length() > 0) {
            btn.setVisibility(View.VISIBLE);
            btn.setText(value);
        }
    }
}