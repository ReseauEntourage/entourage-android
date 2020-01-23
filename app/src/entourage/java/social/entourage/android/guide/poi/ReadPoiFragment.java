package social.entourage.android.guide.poi;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.guide.Poi;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.guide.PoiRenderer;
import social.entourage.android.map.OnAddressClickListener;

/**
 * Activity showing the detail of a POI
 */
public class ReadPoiFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = ReadPoiFragment.class.getSimpleName();

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

    public static ReadPoiFragment newInstance(Poi poi) {
        ReadPoiFragment readPoiFragment = new ReadPoiFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_KEY_POI, poi);
        readPoiFragment.setArguments(args);

        return readPoiFragment;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_guide_poi_read, container, false);
        ButterKnife.bind(this, toReturn);
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_POI_FROM_MAP);

        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            poi = (Poi)getArguments().getSerializable(BUNDLE_KEY_POI);
        }
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        presenter.displayPoi(poi);
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerReadPoiComponent.builder()
                .entourageComponent(entourageComponent)
                .readPoiModule(new ReadPoiModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_user) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDisplayedPoi(Poi poi, @Nullable final OnAddressClickListener onAddressClickListener, @Nullable final ReadPoiPresenter.OnPhoneClickListener onPhoneClickListener) {
        txtPoiName.setText(poi.getName());
        txtPoiDesc.setText(poi.getDescription());
        setActionButton(btnPoiPhone, poi.getPhone());
        setActionButton(btnPoiMail, poi.getEmail());
        setActionButton(btnPoiWeb, poi.getWebsite());
        setActionButton(btnPoiAddress, poi.getAddress());
        if(onAddressClickListener!=null) {
            btnPoiAddress.setOnClickListener(onAddressClickListener);
        }
        if(onPhoneClickListener!=null) {
            btnPoiPhone.setOnClickListener(onPhoneClickListener);
        }
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
            btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    @OnClick(R.id.title_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
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
        String[] addresses = {getString(R.string.contact_email)};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        // Set the subject
        String title = poi.getName();
        if (title == null) title = "";
        String emailSubject = getString(R.string.poi_report_email_subject_format, title, poi.getId());
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        if (intent.resolveActivity(this.getActivity().getPackageManager()) != null) {
            // Start the intent
            startActivity(intent);
        } else {
            // No Email clients
            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
        }
    }

}