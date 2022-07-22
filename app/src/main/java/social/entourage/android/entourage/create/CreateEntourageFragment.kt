package social.entourage.android.entourage.create

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compat.Place
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import kotlinx.android.synthetic.main.fragment_entourage_create.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.*
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.base.location.LocationFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryFragment
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.entourage.create.wizard.CreateActionWizardListener
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage1Fragment
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage2Fragment
import social.entourage.android.entourage.create.wizard.CreateActionWizardPage3Fragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.EntLinkMovementMethod
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 */
class CreateEntourageFragment : BaseDialogFragment(),
    LocationFragment.OnFragmentInteractionListener,
    CreateEntourageListener,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    CreateActionWizardListener {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    var presenter: CreateEntouragePresenter = CreateEntouragePresenter(this)

    private var entourageCategory: EntourageCategory? = null
    private var location: LatLng? = null
    private var groupType: String? = null
    private var entourageDateStart: Calendar? = null
    private var entourageDateEnd: Calendar? = null
    private var entourageMetadata: BaseEntourage.Metadata? = null
    private var recipientConsentObtained = true
    private var isPublic = true
    private var isSaving = false
    private var editedEntourage: BaseEntourage? = null
    private var isStartDateEdited = true

    private var landscape_photo_url:String? = null
    private var portrait_photo_url:String? = null
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entourage_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        title_close_button.setOnClickListener { onCloseClicked() }
        title_action_button.setOnClickListener { onValidateClicked() }
        bottom_action_button?.setOnClickListener { onValidateClicked() }
        create_entourage_category_layout.setOnClickListener { onEditTypeClicked() }
        create_entourage_position_layout.setOnClickListener { onPositionClicked() }
        create_entourage_title_layout.setOnClickListener { onEditTitleClicked() }
        create_entourage_description_layout.setOnClickListener { onEditDescriptionClicked() }
        create_entourage_date_start_layout.setOnClickListener { onEditDateStartClicked()  }
        create_entourage_date_end_layout.setOnClickListener { onEditDateEndClicked() }
        create_entourage_privacy_switch.setOnClickListener { onPrivacySwitchClicked() }
        //To show choice type at launch (if not an event)
        if (!BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true) && entourageCategory?.isNewlyCreated==true) {
            onEditTypeClicked()
        }

        if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType)) {
            create_entourage_photo_layout?.visibility = View.VISIBLE
            create_entourage_photo_layout?.setOnClickListener { onEditPhotoClicked() }
        }
        else {
            create_entourage_photo_layout?.visibility = View.GONE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == VOICE_RECOGNITION_TITLE_CODE) {
                val textMatchList: List<String> = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
                if (textMatchList.isNotEmpty()) {
                    create_entourage_title?.let { titleEditText ->
                        if (titleEditText.text.isEmpty()) {
                            titleEditText.text = textMatchList[0]
                        } else {
                            titleEditText.text = titleEditText.text.toString() + " " + textMatchList[0]
                        }
                        //titleEditText.setSelection(titleEditText.getText().length());
                    }
                }
            } else if (requestCode == VOICE_RECOGNITION_DESCRIPTION_CODE) {
                val textMatchList: List<String> = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
                if (textMatchList.isNotEmpty()) {
                    create_entourage_description?.let { descriptionEditText ->
                        if (descriptionEditText.text.toString() == "") {
                            descriptionEditText.text = textMatchList[0]
                        } else {
                            descriptionEditText.text = descriptionEditText.text.toString() + " " + textMatchList[0]
                        }
                        //descriptionEditText.setSelection(descriptionEditText.getText().length());
                    }
                }
            }
        }
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------
    fun onCloseClicked() {
        dismiss()
    }

    private fun onValidateClicked() {

        if (!isSaving && isValid) {
            if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                isPublic = create_entourage_privacy_switch?.isChecked ?: false
            }
            if (editedEntourage != null) {
                saveEditedEntourage()
            } else {
                createEntourage()
            }
        }
    }

    private fun onEditTypeClicked() {
        entourageCategory?.let {
            val fragment = EntourageCategoryFragment.newInstance(it)
            fragment.setListener(this)
            fragment.show(parentFragmentManager, EntourageCategoryFragment.TAG)
        }
    }

    private fun onPositionClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION)
        val fragment = LocationFragment.newInstance(
            location,
            create_entourage_position?.text.toString(),
            BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true),
            this)
        fragment.show(parentFragmentManager, LocationFragment.TAG)
    }

    private fun onEditTitleClicked() {
        val entourageTitleFragment = CreateEntourageTitleFragment.newInstance(create_entourage_title?.text.toString(), entourageCategory, groupType)
        entourageTitleFragment.setListener(this)
        entourageTitleFragment.show(parentFragmentManager, CreateEntourageTitleFragment.TAG)
    }

    private fun onEditDescriptionClicked() {
        val descriptionFragment = CreateEntourageDescriptionFragment.newInstance(create_entourage_description?.text.toString(), entourageCategory, groupType)
        descriptionFragment.setListener(this)
        descriptionFragment.show(parentFragmentManager, CreateEntourageDescriptionFragment.TAG)
    }

    private fun onEditDateStartClicked() {
        isStartDateEdited = true
        showDatePicker()
    }

    private fun onEditDateEndClicked() {
        if (entourageDateStart == null) return
        isStartDateEdited = false
        showDatePicker()
    }

    private fun onPrivacySwitchClicked() {
        create_entourage_privacy_switch?.let { privacySwitch ->
            // adjust the labels accordingly
            create_entourage_privacy_label?.let { privacyLabel ->
                if (privacySwitch.isChecked) {
                    privacyLabel.setText(R.string.entourage_create_privacy_public)
                    privacyLabel.setTypeface(null, Typeface.BOLD)
                    privacyLabel.setTextColor(ResourcesCompat.getColor(resources, R.color.create_entourage_privacy_public, null))
                } else {
                    privacyLabel.setText(R.string.entourage_create_privacy_private)
                    privacyLabel.setTypeface(null, Typeface.NORMAL)
                    privacyLabel.setTextColor(ResourcesCompat.getColor(resources, R.color.create_entourage_privacy_private, null))
                }
            }

            create_entourage_privacy_description?.let { privacyDescription ->
                if (privacySwitch.isChecked) {
                    privacyDescription.setText(R.string.entourage_create_privacy_description_public)
                } else {
                    privacyDescription.setText(R.string.entourage_create_privacy_description_private)
                }
                privacyDescription.requestLayout()
            }
        }
    }

    private fun onEditPhotoClicked() {
        val photoGallery = CreateEntouragePhotoGalleryFragment.newInstance(portrait_photo_url,landscape_photo_url)
        photoGallery.setListener(this)
        photoGallery.show(parentFragmentManager, CreateEntouragePhotoGalleryFragment.TAG)
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------
    fun onEntourageCreationFailed() {
        isSaving = false
        if (activity != null) {
            Toast.makeText(
                activity,
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) R.string.outing_create_error else R.string.entourage_create_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onEntourageCreated(entourage: BaseEntourage) {
        isSaving = false
        if (activity != null) {
            postEntourageCreated(entourage)
        }
    }

    fun onEntourageEdited() {
        isSaving = false
        if (activity != null) {
            postEntourageSaved()
        }
    }

    fun onEntourageEditionFailed() {
        isSaving = false
        if (activity != null) {
            Toast.makeText(
                activity,
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) R.string.outing_save_error else R.string.entourage_save_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveEditedEntourage() {
        isSaving = true
        val entourageLocation = LocationPoint(0.0, 0.0)
        location?.let {
            entourageLocation.latitude = it.latitude
            entourageLocation.longitude = it.longitude
        }
        editedEntourage?.let { entourage ->
            entourage.setTitle(create_entourage_title?.text.toString())
            entourage.setDescription(create_entourage_description?.text.toString())
            entourage.location = entourageLocation
            entourageCategory?.let { cat ->
                cat.groupType?.let { entourage.actionGroupType = it }
                entourage.category = cat.category
            }
            groupType?.let { entourage.setGroupType(it) }
            entourage.metadata?.portrait_url = portrait_photo_url
            entourage.metadata?.landscape_url = landscape_photo_url
            entourage.metadata = entourageMetadata
            entourage.isPublic = isPublic
            presenter.editEntourage(entourage)
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private fun initializeView() {
        arguments?.let { args ->
            editedEntourage = args.getSerializable(FeedItem.KEY_FEEDITEM) as BaseEntourage?
            entourageMetadata = editedEntourage?.metadata ?: entourageMetadata
            groupType = args.getString(KEY_ENTOURAGE_GROUP_TYPE, null)
            entourageCategory = args.getSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY) as EntourageCategory?
        }
        initializeCategory()
        initializeLocation()
        initializeTitleEditText()
        initializeDescriptionEditText()
        initializeDate()
        initializeJoinRequestType()
        initializeHelpHtmlView()
        initializePrivacyAction()

        initializeEventPhoto()
    }

    private fun initializeCategory() {
        editedEntourage?.let { entourage ->
            entourageCategory = EntourageCategoryManager.findCategory(entourage.actionGroupType, entourage.category)
            groupType = entourage.getGroupType()
            entourageCategory?.isSelected = true
        }
        if (entourageCategory == null) {
            entourageCategory = groupType?.let { EntourageCategoryManager.getDefaultCategory(it) } ?: EntourageCategoryManager.defaultCategory
            entourageCategory?.isSelected = false
            entourageCategory?.isNewlyCreated = true
        }
        updateFragmentTitle()
        updateCategoryTextView()
    }

    private fun updateCategoryTextView() {
        create_entourage_category?.text = entourageCategory?.let {
            getString(if(BaseEntourage.GROUPTYPE_ACTION_DEMAND.equals(it.groupType, ignoreCase = true)) R.string.entourage_create_type_demand else R.string.entourage_create_type_contribution, it.title)
        } ?: "*"
        create_entourage_category_layout?.visibility = if (groupType != null && groupType.equals(BaseEntourage.GROUPTYPE_OUTING, ignoreCase = true)) View.GONE else View.VISIBLE
    }

    private fun updateFragmentTitle() {
        if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
            create_entourage_fragment_title?.setTitle(getString(R.string.entourage_create_outing_title))
        }
    }

    private fun initializeLocation() {
        arguments?.let { args ->
            editedEntourage?.let { entourage ->
                location = entourage.location?.location
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                    entourage.metadata?.let {
                        create_entourage_position?.text = it.displayAddress
                    }
                }
            } ?: run  {
                location = args.getParcelable(KEY_ENTOURAGE_LOCATION)
            }
            if (!BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                location?.let {location -> GeocoderTask().execute(location) }
            }
        }
    }

    private fun initializeTitleEditText() {
        editedEntourage?.let { create_entourage_title?.text = it.getTitle() }
    }

    private fun initializeDescriptionEditText() {
        editedEntourage?.let {
            create_entourage_description?.text = it.getDescription()
        }
    }

    private fun initializeDate() {
        editedEntourage?.metadata?.startDate?.let { startDate ->
            entourageDateStart = Calendar.getInstance().apply {
                this.time = startDate
            }
        }
        editedEntourage?.metadata?.endDate?.let { endDate ->
            entourageDateEnd = Calendar.getInstance().apply {
                this.time = endDate
            }
        }

        updateDateStartTextView()
        updateDateEndTextView()
        create_entourage_date_start_layout?.visibility = if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) View.VISIBLE else View.GONE
        create_entourage_date_end_layout?.visibility = if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) View.VISIBLE else View.GONE
    }

    private fun initializeJoinRequestType() {
        create_entourage_privacy_layout?.visibility = if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) View.VISIBLE else View.GONE
        create_entourage_privacy_layout?.visibility = View.GONE
        editedEntourage?.let {
            create_entourage_privacy_switch?.isChecked = it.isPublic
            onPrivacySwitchClicked()
        }
    }

    private fun initializeHelpHtmlView() {
        create_entourage_help_link?.let {
            (activity as? MainActivity)?.let { mainActivity ->
                val htmlString: String = if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                    getString(R.string.entourage_create_help_text_event, mainActivity.getLink(
                        Constants.EVENTS_GUIDE_ID))
                } else {
                    getString(R.string.entourage_create_help_text, mainActivity.getLink(Constants.GOAL_LINK_ID))
                }
                it.setHtmlString(htmlString, EntLinkMovementMethod)
            }
        }
    }

    private fun initializePrivacyAction() {
        if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
            ui_create_entourage_privacyAction?.visibility = View.GONE
        } else {
            changeActionPrivacyView(isPublic)
            ui_create_entourage_privacyAction?.visibility = View.GONE

            ui_layout_privacyAction_public?.setOnClickListener {
                val entourageTitleFragment = CreateEntouragePrivacyActionFragment.newInstance(isPublic)
                entourageTitleFragment.setListener(this)
                entourageTitleFragment.show(parentFragmentManager, CreateEntourageTitleFragment.TAG)
            }
        }
    }

    private fun initializeEventPhoto() {
        if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
            create_entourage_photo_layout?.visibility = View.VISIBLE
            create_entourage_photo_layout?.setOnClickListener { onEditPhotoClicked() }

            if (editedEntourage?.metadata != null) {
                landscape_photo_url = editedEntourage?.metadata?.landscape_url
                portrait_photo_url = editedEntourage?.metadata?.portrait_url
            }
            else if (entourageMetadata != null) {
                landscape_photo_url = entourageMetadata?.landscape_url
                portrait_photo_url = entourageMetadata?.portrait_url
            }

            landscape_photo_url?.let { landscape_url ->
                if (landscape_url.isNotEmpty()) {
                    Glide.with(this)
                        .load(landscape_url)
                        .into(ui_iv_photo)
                }
            }
        }
        else {
            create_entourage_photo_layout?.visibility = View.GONE
        }
    }

    private fun changeActionPrivacyView(isPublicChecked: Boolean) {
        isPublic = isPublicChecked
        if (isPublic) {
            ui_tv_entourage_privacyAction_public_title?.text = getString(R.string.entourage_create_privacy_public)
            ui_tv_entourage_privacyAction_public?.text = getString(R.string.entourage_create_privacy_description_public_action)
        }
        else {
            ui_tv_entourage_privacyAction_public_title?.text = getString(R.string.entourage_create_privacy_private)
            ui_tv_entourage_privacyAction_public?.text = getString(R.string.entourage_create_privacy_description_private_action)
        }
    }

    private val isValid: Boolean
        get() {
            if (create_entourage_title?.text.isNullOrBlank()) {
                Toast.makeText(activity, R.string.entourage_create_error_title_empty, Toast.LENGTH_SHORT).show()
                return false
            }
            else if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                if (create_entourage_date_start?.text.isNullOrBlank()) {
                    Toast.makeText(activity, R.string.entourage_create_error_date_empty, Toast.LENGTH_SHORT).show()
                    return false
                }
            } else {
                if (entourageCategory?.isNewlyCreated == true) {
                    Toast.makeText(activity, R.string.entourage_create_error_category_empty, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
            if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                if (entourageMetadata == null || entourageMetadata?.googlePlaceId.isNullOrBlank()) {
                    Toast.makeText(activity, R.string.entourage_create_error_location_empty, Toast.LENGTH_SHORT).show()
                    return false
                }
            } else {
                if (location == null) {
                    Toast.makeText(activity, R.string.entourage_create_error_location_empty, Toast.LENGTH_SHORT).show()
                    return false
                } else {
                    if (create_entourage_position?.text.isNullOrBlank()) {
                        Toast.makeText(activity, R.string.entourage_create_error_location_empty, Toast.LENGTH_SHORT).show()
                        return false
                    }
                }
            }
            return true
        }

    private inner class GeocoderTask : AsyncTask<LatLng?, Void?, String>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: LatLng?): String? {
            try {
                params[0]?.let { location ->
                    activity?.applicationContext?.let { context ->
                        Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)?.let { addresses ->
                            if (addresses.size > 0) {
                                if (addresses[0].maxAddressLineIndex >= 0) {
                                    return addresses[0].getAddressLine(0)
                                }
                            }
                        }
                    }
                }
            } catch (ignored: IOException) {
            } catch (ignored: NullPointerException) {
            } catch (ignored: IllegalStateException) {
            }
            return ""
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(address: String) {
            create_entourage_position?.text = address
        }
    }

    // ----------------------------------
    // LocationFragment.OnFragmentInteractionListener
    // ----------------------------------
    override fun onEntourageLocationChosen(location: LatLng?, address: String?, place: Place?) {
        if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
            if (place != null) {
                if (entourageMetadata == null) entourageMetadata = BaseEntourage.Metadata()
                entourageMetadata?.let { metadata ->
                    metadata.placeName = place.name.toString()
                    if (place.address != null) {
                        metadata.streetAddress = place.address.toString()
                        create_entourage_position?.text = place.address.toString()
                    }
                    metadata.setGooglePlaceId(place.id)
                }
                this.location = place.latLng
            } else {
                create_entourage_position?.text = ""
            }
        } else {
            if (location != null) {
                this.location = location
                if (address != null) {
                    create_entourage_position?.text = address
                }
            }
        }
    }

    // ----------------------------------
    // CreateEntourageListener
    // ----------------------------------
    override fun onTitleChanged(title: String) {
        create_entourage_title?.text = title
    }

    override fun onPrivacyChanged(privacy: Boolean) {
        changeActionPrivacyView(privacy)
    }

    override fun onDescriptionChanged(description: String) {
        create_entourage_description?.text = description
    }

    override fun onCategoryChosen(category: EntourageCategory) {
        category.isNewlyCreated = false
        entourageCategory = category
        updateCategoryTextView()
    }

    override fun onPhotoEventAdded(portrait_url: String?, landscape_url: String?) {
        landscape_photo_url = landscape_url
        portrait_photo_url = portrait_url

        editedEntourage?.metadata?.landscape_url = landscape_url
        editedEntourage?.metadata?.portrait_url = portrait_url

        entourageMetadata?.portrait_url = portrait_url
        entourageMetadata?.landscape_url = landscape_url

        landscape_photo_url?.let {
            if (it.isNotEmpty()) {
                Glide.with(this)
                    .load(landscape_url)
                    .error(R.drawable.ic_placeholder_detail_event)
                    .placeholder(R.drawable.ic_placeholder_event)
                    .into(ui_iv_photo)
            }
        }
    }

    // ----------------------------------
    // Date/Time Methods
    // ----------------------------------
    private fun updateDateStartTextView() {
        entourageDateStart?.let { startDate ->
            val df: DateFormat = SimpleDateFormat(getString(R.string.entourage_create_date_format), Locale.getDefault())
            create_entourage_date_start?.text = df.format(startDate.time)
        }
    }

    private fun updateDateEndTextView() {
        entourageDateEnd?.let { endDate ->
            val df: DateFormat = SimpleDateFormat(getString(R.string.entourage_create_date_format), Locale.getDefault())
            create_entourage_date_end?.text = df.format(endDate.time)
        }
    }

    private fun showDatePicker() {
        activity?.supportFragmentManager?.let { fragmentManager ->
            var dpd: DatePickerDialog? = null
            if (isStartDateEdited) {
                entourageDateStart = Calendar.getInstance().also { start ->
                    dpd = DatePickerDialog.newInstance(
                        this,
                        start.get(Calendar.YEAR),
                        start.get(Calendar.MONTH),
                        start.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        this.minDate = Calendar.getInstance() // only today and future dates
                    }
                }
            } else {
                entourageDateEnd?.let { endDate ->
                    dpd = DatePickerDialog.newInstance(
                        this,
                        endDate[Calendar.YEAR],
                        endDate[Calendar.MONTH],
                        endDate[Calendar.DAY_OF_MONTH]
                    ).apply {
                        this.minDate = entourageDateStart // only after start date
                    }
                }
            }
            dpd?.setCancelText(R.string.cancel)
            dpd?.show(fragmentManager, "DatePickerDialog")
        }
    }

    private fun showTimePicker() {
        activity?.supportFragmentManager?.let { fragmentManager ->
            val tpd = TimePickerDialog.newInstance(
                this@CreateEntourageFragment,
                true)
            if (isStartDateEdited) {
                entourageDateStart?.let { tpd.setInitialSelection(it[Calendar.HOUR_OF_DAY], it[Calendar.MINUTE]) }
            } else {
                entourageDateEnd?.let { endDate ->
                    tpd.setInitialSelection(endDate[Calendar.HOUR_OF_DAY], endDate[Calendar.MINUTE])
                }
                entourageDateStart?.let { startDate ->
                    tpd.setMinTime(startDate[Calendar.HOUR_OF_DAY],
                        startDate[Calendar.MINUTE],
                        startDate[Calendar.SECOND]) //Only after time from start date
                }
            }
            tpd.setSelectableTimes(generateTimepoints(15))
            tpd.setCancelText(R.string.cancel)
            tpd.show(fragmentManager, "TimePickerDialog")
        }
    }

    //Use to create selectable minutes custom
    private fun generateTimepoints(minutesInterval: Int): Array<Timepoint?>? {
        val timepoints: ArrayList<Timepoint> = ArrayList()
        var minute = 0
        while (minute <= 1440) {
            val currentHour = minute / 60
            val currentMinute = minute - if (currentHour > 0) currentHour * 60 else 0
            if (currentHour == 24) {
                minute += minutesInterval
                continue
            }
            timepoints.add(Timepoint(currentHour, currentMinute))
            minute += minutesInterval
        }
        return timepoints.toArray(arrayOfNulls<Timepoint>(timepoints.size))
    }

    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (isStartDateEdited) {
            entourageDateStart?.apply { this[year, monthOfYear] = dayOfMonth }?.also { startDate->
                if (entourageDateEnd == null || startDate.after(entourageDateEnd)) {
                    entourageDateEnd = Calendar.getInstance().apply {
                        this.time = startDate.time
                        this.set(Calendar.HOUR, this.get(Calendar.HOUR) + ADD_HOURS_TO_END_DATE)
                    }
                }
            }
        } else {
            entourageDateEnd?.apply { this[year, monthOfYear] = dayOfMonth }
        }
        Handler().post { showTimePicker() }
    }

    override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        val startDate = entourageDateStart ?: return
        var endDate = entourageDateEnd
        if (isStartDateEdited) {
            startDate[Calendar.HOUR_OF_DAY] = hourOfDay
            startDate[Calendar.MINUTE] = minute
            startDate[Calendar.SECOND] = second

            val tmpDateAfter = Calendar.getInstance()
            tmpDateAfter.time = startDate.time
            tmpDateAfter[Calendar.HOUR] = tmpDateAfter[Calendar.HOUR] + ADD_HOURS_TO_END_DATE

            if (endDate == null || tmpDateAfter.after(endDate)) {
                endDate = Calendar.getInstance()
                endDate.time = startDate.time
                endDate[Calendar.HOUR] = endDate[Calendar.HOUR] + ADD_HOURS_TO_END_DATE
                endDate[Calendar.MINUTE] = minute
            }
        } else {
            if (endDate == null) {
                endDate = Calendar.getInstance()
            }
            endDate?.set(Calendar.HOUR_OF_DAY, hourOfDay)
            endDate?.set(Calendar.MINUTE, minute)
            endDate?.set(Calendar.SECOND, second)
        }
        if (entourageMetadata == null) entourageMetadata = BaseEntourage.Metadata()

        endDate?.let {
            entourageMetadata?.setEndDate(it.time)
            entourageDateEnd = it
        }
        entourageMetadata?.setStartDate(startDate.time)
        updateDateStartTextView()
        updateDateEndTextView()
    }

    // ----------------------------------
    // Entourage create/edit methods
    // ----------------------------------
    private fun createEntourage() {
        if (BaseEntourage.GROUPTYPE_ACTION_DEMAND.equals(
                entourageCategory?.groupType,
                ignoreCase = true
            )
        ) { // for DEMAND events, we need to show a wizard
            showCreateActionWizard()
            return
        }
        createEntourageNoCheck()
    }

    private fun createEntourageNoCheck() {
        if (isSaving) return
        isSaving = true
        val entourageLocation = LocationPoint(0.0, 0.0)
        location?.let {
            entourageLocation.latitude = it.latitude
            entourageLocation.longitude = it.longitude
        }
        presenter.createEntourage(
            entourageCategory?.groupType,
            entourageCategory?.category,
            create_entourage_title?.text.toString(),
            create_entourage_description?.text.toString(),
            entourageLocation,
            recipientConsentObtained,
            groupType,
            entourageMetadata,
            isPublic,
            portrait_photo_url,
            landscape_photo_url)
    }

    private fun postEntourageCreated(entourage: BaseEntourage) {
        hideExtraScreens()
        try {
            dismiss()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
        EntBus.post(Events.OnFeedItemInfoViewRequestedEvent(entourage, true))
    }

    private fun postEntourageSaved() {
        Toast.makeText(
            activity,
            if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) R.string.outing_save_ok else R.string.entourage_save_ok,
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    private fun hideExtraScreens() {
        try { //Hide the wizard pages
            (parentFragmentManager.findFragmentByTag(CreateActionWizardPage1Fragment.TAG) as DialogFragment?)?.dismiss()
            (parentFragmentManager.findFragmentByTag(CreateActionWizardPage2Fragment.TAG) as DialogFragment?)?.dismiss()
            (parentFragmentManager.findFragmentByTag(CreateActionWizardPage3Fragment.TAG) as DialogFragment?)?.dismiss()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    // ----------------------------------
    // CreateActionWizard
    // ----------------------------------
    private fun showCreateActionWizard() {
        try {
            val createActionWizardPage1Fragment = CreateActionWizardPage1Fragment()
            createActionWizardPage1Fragment.setListener(this)
            createActionWizardPage1Fragment.show(parentFragmentManager, CreateActionWizardPage1Fragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun createActionWizardPreviousStep(currentStep: Int) {
        if (currentStep == 1) isSaving = false
    }

    override fun createActionWizardNextStep(currentStep: Int, option: Int) {
        when (currentStep) {
            1 -> handleStep1(option)
            2 -> handleStep2(option)
            3 -> handleStep3(option)
            else -> Timber.e("Invalid step %s", currentStep)
        }
    }

    private fun handleStep1(option: Int) {
        when (option) {
            1 -> try {
                val createActionWizardPage2Fragment = CreateActionWizardPage2Fragment()
                createActionWizardPage2Fragment.setListener(this)
                createActionWizardPage2Fragment.show(parentFragmentManager, CreateActionWizardPage2Fragment.TAG)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
            2, 3 -> if (editedEntourage != null) {
                saveEditedEntourage()
            } else {
                createEntourageNoCheck()
            }
        }
    }

    private fun handleStep2(option: Int) {
        when (option) {
            1 -> if (editedEntourage != null) {
                saveEditedEntourage()
            } else {
                createEntourageNoCheck()
            }
            2 -> try {
                val createActionWizardPage3Fragment = CreateActionWizardPage3Fragment()
                createActionWizardPage3Fragment.setListener(this)
                createActionWizardPage3Fragment.show(parentFragmentManager, CreateActionWizardPage3Fragment.TAG)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    private fun handleStep3(option: Int) {
        if (option == 1) {
            if (editedEntourage != null) {
                saveEditedEntourage()
            } else {
                recipientConsentObtained = false
                createEntourageNoCheck()
                recipientConsentObtained = true
            }
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage.android.createentourage"
        private const val KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION"
        const val KEY_ENTOURAGE_GROUP_TYPE = "social.entourage.android.KEY_ENTOURAGE_GROUP_TYPE"
        private const val VOICE_RECOGNITION_TITLE_CODE = 1
        private const val VOICE_RECOGNITION_DESCRIPTION_CODE = 2
        private const val ADD_HOURS_TO_END_DATE = 3

        fun newExpertInstance(location: LatLng?, groupType: String, category: EntourageCategory?): CreateEntourageFragment {
            val fragment = CreateEntourageFragment()
            val args = Bundle()
            args.putParcelable(KEY_ENTOURAGE_LOCATION, location)
            args.putString(KEY_ENTOURAGE_GROUP_TYPE, groupType)
            if (category != null) {
                args.putSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY, category)
            }
            fragment.arguments = args
            return fragment
        }

        fun newInstance(entourage: BaseEntourage?): CreateEntourageFragment {
            val fragment = CreateEntourageFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, entourage)
            fragment.arguments = args
            return fragment
        }
    }
}