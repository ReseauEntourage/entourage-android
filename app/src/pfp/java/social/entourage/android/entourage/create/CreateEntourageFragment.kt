package social.entourage.android.entourage.create

import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compat.Place
import social.entourage.android.EntourageComponent
import social.entourage.android.R
import social.entourage.android.api.model.map.BaseEntourage
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.location.LocationFragment

/**
 *
 */
class CreateEntourageFragment  : BaseCreateEntourageFragment(), LocationFragment.OnFragmentInteractionListener, CreateEntourageListener {
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    // ----------------------------------
    // Constants
    // ----------------------------------
    // ----------------------------------
    // Attributes
    // ----------------------------------
    @JvmField
    @BindView(R.id.create_entourage_title_label)
    var titleLabel: TextView? = null

    @JvmField
    @BindView(R.id.create_entourage_description_label)
    var descriptionLabel: TextView? = null

    @JvmField
    @BindView(R.id.create_entourage_position_description)
    var positionLabel: TextView? = null

    @JvmField
    @BindView(R.id.create_entourage_date_start_label)
    var dateLabel: TextView? = null
    override fun setupComponent(entourageComponent: EntourageComponent) {
        DaggerCreateEntourageComponent.builder()
                .entourageComponent(entourageComponent)
                .createEntourageModule(CreateEntourageModule(this))
                .build()
                .inject(this)
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
    override fun initializeLocation() {
        val args = arguments
        if (args != null) {
            if (editedEntourage != null) {
                location = editedEntourage.location.location
                val metadata = editedEntourage.metadata
                if (metadata != null) {
                    positionTextView.text = metadata.displayAddress
                    positionLabel!!.visibility = View.GONE
                }
            } else {
                location = args.getParcelable(KEY_ENTOURAGE_LOCATION)
            }
        }
    }

    override fun initializeTitleEditText() {
        if (editedEntourage != null) {
            onTitleChanged(editedEntourage.title)
        }
    }

    override fun initializeDescriptionEditText() {
        if (editedEntourage != null) {
            onDescriptionChanged(editedEntourage.description)
        }
    }

    override fun initializeCategory() {
        if (groupType == null) groupType = BaseEntourage.TYPE_OUTING // only outings
        super.initializeCategory()
    }

    override fun updateDateStartTextView() {
        super.updateDateStartTextView()
        dateLabel!!.visibility = View.GONE
    }

    override fun updateDateEndTextView() {
        super.updateDateEndTextView()
        dateLabel!!.visibility = View.GONE
    }

    override fun isValid(): Boolean {
        joinRequestTypePublic = privacySwitch.isChecked
        return super.isValid()
    }

    // ----------------------------------
    // LocationFragment.OnFragmentInteractionListener
    // ----------------------------------
    override fun onEntourageLocationChosen(location: LatLng, address: String, place: Place) {
        // for PFP, we use only the place
        this.location = place.latLng
        positionTextView.text = place.address
        positionLabel!!.visibility = View.GONE
        if (groupType != null && groupType.equals(BaseEntourage.TYPE_OUTING, ignoreCase = true)) {
            if (entourageMetadata == null) entourageMetadata = BaseEntourage.Metadata()
            entourageMetadata.placeName = place.name.toString()
            if (place.address != null) {
                entourageMetadata.streetAddress = place.address.toString()
            }
            entourageMetadata.googlePlaceId = place.id
        }
    }

    // ----------------------------------
    // CreateEntourageListener
    // ----------------------------------
    override fun onTitleChanged(title: String) {
        if (title.trim { it <= ' ' }.isEmpty()) {
            titleLabel!!.visibility = View.VISIBLE
            titleEditText.text = ""
        } else {
            titleLabel!!.visibility = View.GONE
            titleEditText.text = title
        }
    }

    override fun onDescriptionChanged(description: String) {
        if (description.trim { it <= ' ' }.isEmpty()) {
            descriptionLabel!!.visibility = View.VISIBLE
            descriptionEditText.text = ""
        } else {
            descriptionLabel!!.visibility = View.GONE
            descriptionEditText.text = description
        }
    }
}