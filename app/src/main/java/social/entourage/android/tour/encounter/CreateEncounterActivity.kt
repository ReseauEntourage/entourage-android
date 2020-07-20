package social.entourage.android.tour.encounter

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.format.DateFormat
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compat.Place
import kotlinx.android.synthetic.main.activity_encounter_create.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageComponent
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.base.EntourageSecuredActivity
import social.entourage.android.R
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.tape.Events.OnEncounterCreated
import social.entourage.android.api.tape.Events.OnEncounterUpdated
import social.entourage.android.location.LocationFragment
import social.entourage.android.tools.BusProvider.instance
import timber.log.Timber
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

class CreateEncounterActivity : EntourageSecuredActivity(), LocationFragment.OnFragmentInteractionListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Inject
    lateinit var presenter: CreateEncounterPresenter

    private var location: LatLng? = null
    private var editedEncounter: Encounter? = null
    private var readOnly = true

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encounter_create)
        val arguments = intent.extras
        // Create mode
        require(!(arguments == null || arguments.isEmpty)) { "You must provide latitude and longitude" }
        editedEncounter = arguments.getSerializable(BUNDLE_KEY_ENCOUNTER) as Encounter?
        editedEncounter?.let {
            readOnly = it.isReadOnly
            location = LatLng(it.latitude, it.longitude)
        } ?: run {
            // Create mode
            readOnly = false
            presenter.setTourUUID(arguments.getString(BUNDLE_KEY_TOUR_ID))
            presenter.setLatitude(arguments.getDouble(BUNDLE_KEY_LATITUDE))
            presenter.setLongitude(arguments.getDouble(BUNDLE_KEY_LONGITUDE))
            location = LatLng(arguments.getDouble(BUNDLE_KEY_LATITUDE), arguments.getDouble(BUNDLE_KEY_LONGITUDE))
        }
        title_close_button?.setOnClickListener {onCloseButton()}
        title_action_button?.setOnClickListener {createEncounter()}
        create_encounter_position_layout?.setOnClickListener {onPositionClicked()}
        button_record?.setOnClickListener {onRecord()}
        initialiseFields()
        EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_START)
    }

    override fun setupComponent(entourageComponent: EntourageComponent) {
        DaggerCreateEncounterComponent.builder()
                .entourageComponent(entourageComponent)
                .createEncounterModule(CreateEncounterModule(this))
                .build()
                .inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val textMatchList: List<String>? = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!textMatchList.isNullOrEmpty()) {
                    edittext_message?.let {
                        if (it.text.toString() == "") {
                            it.setText(textMatchList[0])
                        } else {
                            it.setText(it.text.toString() + " " + textMatchList[0])
                        }
                        it.setSelection(it.text.length)
                        EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initialiseFields() {
        encounter_author?.text = resources.getString(R.string.encounter_label_person_name_and, presenter.author)
        edittext_street_person_name?.setText(editedEncounter?.streetPersonName ?: "")
        val todayDateString = DateFormat.getDateFormat(applicationContext).format(editedEncounter?.creationDate ?: Date())
        encounter_date?.text = resources.getString(R.string.encounter_encountered, todayDateString)
        edittext_message?.setText(editedEncounter?.message ?:"")
        if (location != null) {
            GeocoderTask(this).execute(location)
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    private fun onCloseButton() {
        instance.post(OnEncounterCreated(null))
        finish()
    }

    private fun createEncounter() {
        val personName = edittext_street_person_name?.text.toString().trim { it <= ' ' }
        val message = edittext_message?.text.toString().trim { it <= ' ' }
        if (message != "" && personName != "") {
            showProgressDialog(if (editedEncounter == null) R.string.creating_encounter else R.string.updating_encounter)
            editedEncounter?.let {
                    it.streetPersonName = personName
                    it.message = message
                    presenter.updateEncounter(it)
                }
                    ?: presenter.createEncounter(message, personName)
        } else {
            if (personName == "") {
                Toast.makeText(applicationContext, R.string.encounter_empty_name, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, R.string.encounter_empty_fields, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onRecord() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message))
        try {
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED)
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(applicationContext, getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show()
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED)
        }
    }

    fun onPositionClicked() {
        hideKeyboard()
        create_encounter_position?.let {
            LocationFragment.newInstance(location, it.text.toString(), this).show(supportFragmentManager, LocationFragment.TAG)
        }
    }

    fun onCreateEncounterFinished(errorMessage: String?, encounterResponse: Encounter?) {
        dismissProgressDialog()
        val message: String
        if (errorMessage == null) {
            authenticationController.incrementUserEncountersCount()
            message = getString(R.string.create_encounter_success)
            instance.post(OnEncounterCreated(encounterResponse))
            finish()
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_OK)
        } else {
            message = getString(R.string.create_encounter_failure)
            Timber.e(message)
            EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_FAILED)
        }
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    fun onUpdatingEncounterFinished(errorMessage: String?, encounterResponse: Encounter?) {
        dismissProgressDialog()
        val message: String
        if (errorMessage == null) {
            authenticationController.incrementUserEncountersCount()
            message = getString(R.string.update_encounter_success)
            instance.post(OnEncounterUpdated(editedEncounter))
            finish()
            //EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_OK);
        } else {
            message = getString(R.string.update_encounter_failure)
            Timber.e(message)
            //EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_FAILED);
        }
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    // ----------------------------------
    // LocationFragment.OnFragmentInteractionListener
    // ----------------------------------
    override fun onEntourageLocationChosen(newLocation: LatLng?, address: String?, place: Place?) {
        val curLocation  = newLocation ?: place?.latLng ?: return
        this.location = curLocation
        presenter.setLatitude(curLocation.latitude)
        presenter.setLongitude(curLocation.longitude)
        editedEncounter?.latitude = curLocation.latitude
        editedEncounter?.longitude = curLocation.longitude
        create_encounter_position?.text = address
    }

    private fun setAddress(address: String) {
        create_encounter_position?.text = address
    }

    // ----------------------------------
    // PRIVATE CLASSES
    // ----------------------------------
    private class GeocoderTask internal constructor(context: CreateEncounterActivity) : AsyncTask<LatLng?, Void?, String?>() {
        private val activityReference: WeakReference<CreateEncounterActivity> = WeakReference(context)

        override fun doInBackground(vararg params: LatLng?): String? {
            try {
                val geoCoder = Geocoder(activityReference.get(), Locale.getDefault())
                val location = params[0] ?: return null
                val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses != null && addresses.size > 0) {
                    val address = addresses[0]
                    if (address.maxAddressLineIndex >= 0) {
                        return addresses[0].getAddressLine(0)
                    }
                }
            } catch (ignored: IOException) {
            }
            return null
        }

        override fun onPostExecute(address: String?) {
            if (address != null) {
                activityReference.get()?.setAddress(address)
            }
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val BUNDLE_KEY_TOUR_ID = "BUNDLE_KEY_TOUR_ID"
        const val BUNDLE_KEY_ENCOUNTER = "BUNDLE_KEY_ENCOUNTER"
        const val BUNDLE_KEY_LATITUDE = "BUNDLE_KEY_LATITUDE"
        const val BUNDLE_KEY_LONGITUDE = "BUNDLE_KEY_LONGITUDE"
        private const val VOICE_RECOGNITION_REQUEST_CODE = 1
    }
}