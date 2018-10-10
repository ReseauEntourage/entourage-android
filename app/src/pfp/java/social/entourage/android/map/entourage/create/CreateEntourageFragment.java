package social.entourage.android.map.entourage.create;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.location.LocationFragment;

/**
 *
 */
public class CreateEntourageFragment extends BaseCreateEntourageFragment implements LocationFragment.OnFragmentInteractionListener, CreateEntourageListener {

    // ----------------------------------
    // Constants
    // ----------------------------------

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @BindView(R.id.create_entourage_title_label)
    TextView titleLabel;

    @BindView(R.id.create_entourage_description_label)
    TextView descriptionLabel;

    @BindView(R.id.create_entourage_position_description)
    TextView positionLabel;

    @BindView(R.id.create_entourage_date_label)
    TextView dateLabel;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageFragment() {
        // Required empty public constructor
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerCreateEntourageComponent.builder()
                .entourageComponent(entourageComponent)
                .createEntourageModule(new CreateEntourageModule(this))
                .build()
                .inject(this);
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    // ----------------------------------
    // Base class overrides
    // ----------------------------------

    @Override
    protected void initializeLocation() {
        Bundle args = getArguments();
        if (args != null) {
            if (editedEntourage != null) {
                location = editedEntourage.getLocation().getLocation();
                BaseEntourage.Metadata metadata = editedEntourage.getMetadata();
                if (metadata != null) {
                    positionTextView.setText(metadata.getDisplayAddress());
                    positionLabel.setVisibility(View.GONE);
                }
            } else {
                location = args.getParcelable(KEY_ENTOURAGE_LOCATION);
            }
        }
    }

    @Override
    protected void initializeTitleEditText() {
        if (editedEntourage != null) {
            onTitleChanged(editedEntourage.getTitle());
        }
    }

    @Override
    protected void initializeDescriptionEditText() {
        if (editedEntourage != null) {
            onDescriptionChanged(editedEntourage.getDescription());
        }
    }

    @Override
    protected void initializeCategory() {
        if (groupType == null) groupType = Entourage.TYPE_OUTING; // only outings
        super.initializeCategory();
    }

    @Override
    protected void updateDateTextView() {
        super.updateDateTextView();
        dateLabel.setVisibility(View.GONE);
    }

    @Override
    protected boolean isValid() {
        joinRequestTypePublic = privacySwitch.isChecked();
        return super.isValid();
    }

    // ----------------------------------
    // LocationFragment.OnFragmentInteractionListener
    // ----------------------------------

    @Override
    public void onEntourageLocationChosen(LatLng location, String address, Place place) {
        // for PFP, we use only the place
        if (place != null) {
            this.location = place.getLatLng();
            positionTextView.setText(place.getAddress());
            positionLabel.setVisibility(View.GONE);
            if (groupType != null && groupType.equalsIgnoreCase(Entourage.TYPE_OUTING)) {
                if (entourageMetadata == null) entourageMetadata = new BaseEntourage.Metadata();
                entourageMetadata.setPlaceName(place.getName().toString());
                if (place.getAddress() != null) {
                    entourageMetadata.setStreetAddress(place.getAddress().toString());
                }
                entourageMetadata.setGooglePlaceId(place.getId());
            }
        } else {
            this.location = null;
            positionTextView.setText("");
            positionLabel.setVisibility(View.VISIBLE);
        }
    }

    // ----------------------------------
    // CreateEntourageListener
    // ----------------------------------

    @Override
    public void onTitleChanged(final String title) {
        if (title == null || title.trim().length() == 0) {
            titleLabel.setVisibility(View.VISIBLE);
            titleEditText.setText("");
        } else {
            titleLabel.setVisibility(View.GONE);
            titleEditText.setText(title);
        }
    }

    @Override
    public void onDescriptionChanged(final String description) {
        if (description == null || description.trim().length() == 0) {
            descriptionLabel.setVisibility(View.VISIBLE);
            descriptionEditText.setText("");
        } else {
            descriptionLabel.setVisibility(View.GONE);
            descriptionEditText.setText(description);
        }
    }

}
