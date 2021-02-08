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
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compat.Place
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import kotlinx.android.synthetic.main.fragment_entourage_create.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.EntourageComponent
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.EntLinkMovementMethod
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryFragment
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.location.LocationFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Base fragment for creating and editing an action/entourage
 */
open class BaseCreateEntourageFragment
    : BaseDialogFragment(), LocationFragment.OnFragmentInteractionListener, CreateEntourageListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    @JvmField
    @Inject
    var presenter: CreateEntouragePresenter? = null

    protected var entourageCategory: EntourageCategory? = null
    protected var location: LatLng? = null
    protected var groupType: String? = null
    private var entourageDateStart //= Calendar.getInstance();
            : Calendar? = null
    private var entourageDateEnd //= Calendar.getInstance();
            : Calendar? = null
    private var entourageMetadata: BaseEntourage.Metadata? = null
    protected var recipientConsentObtained = true
    protected var joinRequestTypePublic = true
    protected var isSaving = false
    protected var editedEntourage: BaseEntourage? = null
    private var isStartDateEdited = true
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
        setupComponent(get().components)
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
    }

    protected open fun setupComponent(entourageComponent: EntourageComponent) {}
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
        if (isSaving) return
        if (isValid) {
            if (presenter != null) {
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                    joinRequestTypePublic = create_entourage_privacy_switch?.isChecked ?: false
                }
                if (editedEntourage != null) {
                    saveEditedEntourage()
                } else {
                    createEntourage()
                }
            } else {
                Toast.makeText(activity, R.string.entourage_create_error, Toast.LENGTH_SHORT).show()
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

    fun onEntourageEdited(entourage: BaseEntourage) {
        isSaving = false
        if (activity != null) {
            postEntourageSaved(entourage)
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

    // ----------------------------------
    // Entourage create/edit methods
    // ----------------------------------
    protected open fun createEntourage() {
        if (isSaving) return
        isSaving = true
        val entourageLocation = LocationPoint(0.0, 0.0)
        location?.let {
            entourageLocation.latitude = it.latitude
            entourageLocation.longitude = it.longitude
        }
        presenter?.createEntourage(
                entourageCategory?.groupType,
                entourageCategory?.category,
                create_entourage_title?.text.toString(),
                create_entourage_description?.text.toString(),
                entourageLocation,
                recipientConsentObtained,
                groupType,
                entourageMetadata,
                joinRequestTypePublic)
    }

    protected open fun postEntourageCreated(entourage: BaseEntourage) {
        Toast.makeText(
                activity,
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) R.string.outing_create_ok else R.string.entourage_create_ok,
                Toast.LENGTH_SHORT
        ).show()
        try {
            dismiss()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
        EntBus.post(OnFeedItemInfoViewRequestedEvent(entourage,true))
    }

    protected fun saveEditedEntourage() {
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
            entourage.metadata = entourageMetadata
            entourage.isJoinRequestPublic = joinRequestTypePublic
            presenter?.editEntourage(entourage)
        }
    }

    protected open fun postEntourageSaved(entourage: BaseEntourage) {
        Toast.makeText(
                activity,
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) R.string.outing_save_ok else R.string.entourage_save_ok,
                Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    protected fun initializeView() {
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
        editedEntourage?.let {
            create_entourage_privacy_switch?.isChecked = it.isJoinRequestPublic
            onPrivacySwitchClicked()
        }
    }

    private fun initializeHelpHtmlView() {
        create_entourage_help_link?.let {
            (activity as? MainActivity)?.let { mainActivity ->
                val htmlString: String = if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
                    getString(R.string.entourage_create_help_text_event, mainActivity.getLink(Constants.EVENTS_GUIDE_ID))
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
            changeActionView(editedEntourage?.isJoinRequestPublic ?: true)
            ui_create_entourage_privacyAction?.visibility = View.VISIBLE
            ui_layout_privacyAction_public?.setOnClickListener { changeActionView(true) }
            ui_layout_privacyAction_private?.setOnClickListener { changeActionView(false) }
        }
    }

    private fun changeActionView(isPublicActive: Boolean) {
        ui_tv_entourage_privacyAction_public_title?.let {it.typeface = Typeface.create(it.typeface, if (isPublicActive) Typeface.BOLD else Typeface.NORMAL)}
        ui_tv_entourage_privacyAction_public?.let {it.typeface = Typeface.create(it.typeface, if (isPublicActive) Typeface.BOLD else Typeface.NORMAL)}
        ui_tv_entourage_privacyAction_private_title?.let {it.typeface = Typeface.create(it.typeface, if (!isPublicActive) Typeface.BOLD else Typeface.NORMAL)}
        ui_tv_entourage_privacyAction_private?.let {it.typeface = Typeface.create(it.typeface, if (!isPublicActive) Typeface.BOLD else Typeface.NORMAL)}
        ui_iv_button_public?.visibility = if (isPublicActive) View.VISIBLE else View.INVISIBLE
        ui_iv_button_private?.visibility = if (!isPublicActive) View.VISIBLE else View.INVISIBLE
        joinRequestTypePublic = isPublicActive
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
                if (entourageMetadata == null || entourageMetadata!!.googlePlaceId.isNullOrBlank()) {
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
        override fun doInBackground(vararg params: LatLng?): String? {
            try {
                params[0]?.let { location ->
                    val geoCoder = Geocoder(activity, Locale.getDefault())
                    geoCoder.getFromLocation(location.latitude, location.longitude, 1)?.let { addresses ->
                        if (addresses.size > 0) {
                            if (addresses[0].maxAddressLineIndex >= 0) {
                                return addresses[0].getAddressLine(0)
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

    override fun onDescriptionChanged(description: String) {
        create_entourage_description?.text = description
    }

    override fun onCategoryChosen(category: EntourageCategory) {
        category.isNewlyCreated = false
        entourageCategory = category
        updateCategoryTextView()
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
                    this@BaseCreateEntourageFragment,
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

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage.android.createentourage"
        protected const val KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION"
        const val KEY_ENTOURAGE_GROUP_TYPE = "social.entourage.android.KEY_ENTOURAGE_GROUP_TYPE"
        private const val VOICE_RECOGNITION_TITLE_CODE = 1
        private const val VOICE_RECOGNITION_DESCRIPTION_CODE = 2
        private const val ADD_HOURS_TO_END_DATE = 3

        fun newInstance(location: LatLng?, groupType: String, category: EntourageCategory?): CreateEntourageFragment {
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