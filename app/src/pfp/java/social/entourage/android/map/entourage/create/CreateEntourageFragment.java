package social.entourage.android.map.entourage.create;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.OnClick;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
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

    @BindView(R.id.create_entourage_privacy_label)
    TextView privacyLabel;

    @BindView(R.id.create_entourage_privacy_switch)
    Switch privacySwitch;

    @BindView(R.id.create_entourage_privacy_description)
    TextView privacyDescription;

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

    @OnClick(R.id.create_entourage_privacy_switch)
    protected void onPrivacySwitchClicked() {
        if (privacySwitch == null) return;
        // adjust the labels accordingly
        if (privacySwitch.isChecked()) {
            if (privacyLabel != null) {
                privacyLabel.setText(R.string.entourage_create_privacy_public);
                privacyLabel.setTypeface(privacyLabel.getTypeface(), Typeface.BOLD);
                privacyLabel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.create_entourage_privacy_public, null));
            }
            if (privacyDescription != null) {
                privacyDescription.setText(R.string.entourage_create_privacy_description_public);
                privacyDescription.requestLayout();
            }
        } else {
            if (privacyLabel != null) {
                privacyLabel.setText(R.string.entourage_create_privacy_private);
                privacyLabel.setTypeface(privacyLabel.getTypeface(), Typeface.NORMAL);
                privacyLabel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.create_entourage_privacy_private, null));
            }
            if (privacyDescription != null) {
                privacyDescription.setText(R.string.entourage_create_privacy_description_private);
                privacyDescription.requestLayout();
            }
        }
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    // ----------------------------------
    // Private methods
    // ----------------------------------

    @Override
    protected void initializeLocation() {
        Bundle args = getArguments();
        if (args != null) {
            if (editedEntourage != null) {
                location = editedEntourage.getLocation().getLocation();
            } else {
                location = args.getParcelable(KEY_ENTOURAGE_LOCATION);
            }
        }
    }

    protected void initializeTitleEditText() {

    }

    protected void initializeDescriptionEditText() {

    }

    @Override
    protected boolean isValid() {
        return super.isValid();
    }

    // ----------------------------------
    // LocationFragment.OnFragmentInteractionListener
    // ----------------------------------

    @Override
    public void onEntourageLocationChosen(LatLng location, String address) {
        if (location != null) {
            this.location = location;
            if (address != null) {
                positionTextView.setText(address);
            }
        }
    }

    // ----------------------------------
    // CreateEntourageListener
    // ----------------------------------

    @Override
    public void onTitleChanged(final String title) {
        titleEditText.setText(title);
    }

    @Override
    public void onDescriptionChanged(final String description) {
        descriptionEditText.setText(description);
    }

}
