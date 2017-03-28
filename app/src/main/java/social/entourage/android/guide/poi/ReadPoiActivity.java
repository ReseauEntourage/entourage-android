package social.entourage.android.guide.poi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.guide.PoiRenderer;

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

    @BindView(R.id.textview_poi_name)
    TextView txtPoiName;
    @BindView(R.id.textview_poi_description)
    TextView txtPoiDesc;
    @BindView(R.id.button_poi_phone)
    Button btnPoiPhone;
    @BindView(R.id.button_poi_mail)
    Button btnPoiMail;
    @BindView(R.id.button_poi_web)
    Button btnPoiWeb;
    @BindView(R.id.button_poi_address)
    Button btnPoiAddress;
    @BindView(R.id.poi_type_layout)
    LinearLayout poiTypeLayout;
    @BindView(R.id.poi_type_image)
    ImageView poiTypeImage;
    @BindView(R.id.poi_type_label)
    TextView poiTypeLabel;

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
        //getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        PoiRenderer.CategoryType categoryType = PoiRenderer.CategoryType.findCategoryTypeById(poi.getCategoryId());
        poiTypeLayout.setBackgroundColor(categoryType.getColor());
        poiTypeLabel.setText(categoryType.getName());
        poiTypeImage.setImageResource(categoryType.getResourceTransparentId());
    }

    public void setActionButton(Button btn, String value) {
        if (btn == null || value == null) return;
        if(value.length() > 0) {
            btn.setVisibility(View.VISIBLE);
            btn.setText(value);
        }
    }

    @OnClick(R.id.poi_close_button)
    protected void onCloseButtonClicked() {
        finish();
    }

    @OnClick(R.id.poi_report_button)
    protected void onReportButtonClicked() {
        if (poi == null) {
            return;
        }
        // Build the email intent
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        // Set the email to
        String[] addresses = {Constants.EMAIL_CONTACT};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        // Set the subject
        String title = poi.getName();
        if (title == null) title = "";
        String emailSubject = getString(R.string.poi_report_email_subject_format, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the intent
            startActivity(intent);
        } else {
            // No Email clients
            Toast.makeText(this, R.string.error_no_email, Toast.LENGTH_SHORT).show();
        }
    }

}