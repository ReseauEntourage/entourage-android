package social.entourage.android.tour.encounter

import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import kotlinx.android.synthetic.main.activity_encounter_read.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageActivity
import social.entourage.android.EntourageComponent
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.tour.Encounter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

class ReadEncounterActivity : EntourageActivity() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var encounter: Encounter? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encounter_read)
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_ENCOUNTER_FROM_MAP)
        encounter = intent?.extras?.get(BUNDLE_KEY_ENCOUNTER) as Encounter?
        title_close_button.setOnClickListener {onCloseButton()}
    }

    override fun onStart() {
        super.onStart()
        displayEncounter()
        if (encounter != null) {
            GeocoderTask(this).execute(encounter)
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun onCloseButton() {
        finish()
    }

    private fun displayEncounter() {
        if (encounter == null || isFinishing) return
        val location = if (encounter!!.address != null && encounter!!.address.maxAddressLineIndex >= 0) {
            encounter!!.address.getAddressLine(0)
        } else ""
        val encounterDate: String? = if (encounter!!.creationDate != null) DateFormat.getDateFormat(applicationContext).format(encounter!!.creationDate) else ""
        val encounterLocation: String = if (location.isEmpty()) {
            resources.getString(R.string.encounter_read_location_no_address,
                    encounter!!.userName,
                    encounter!!.streetPersonName,
                    encounterDate)
        } else {
            resources.getString(R.string.encounter_read_location,
                    encounter!!.userName,
                    encounter!!.streetPersonName,
                    location,
                    encounterDate)
        }
        edittext_street_person_name?.setText(encounterLocation)
        edittext_message?.setText(encounter!!.message)
    }

    private class GeocoderTask internal constructor(context: ReadEncounterActivity) : AsyncTask<Encounter?, Void?, Encounter?>() {
        private val activityReference: WeakReference<ReadEncounterActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Encounter?): Encounter? {
            try {
                val geoCoder = Geocoder(activityReference.get(), Locale.getDefault())
                val encounter = params[0] ?: return null
                val addresses = geoCoder.getFromLocation(encounter.latitude, encounter.longitude, 1)
                if (addresses != null && addresses.size > 0) {
                    encounter.address = addresses[0]
                }
                return encounter
            } catch (ignored: IOException) {
            }
            return null
        }

        override fun onPostExecute(encounter: Encounter?) {
            activityReference.get()?.displayEncounter()
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val BUNDLE_KEY_ENCOUNTER = "BUNDLE_KEY_ENCOUNTER"
    }
}