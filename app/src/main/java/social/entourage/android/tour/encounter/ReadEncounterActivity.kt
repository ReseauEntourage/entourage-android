package social.entourage.android.tour.encounter

import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateFormat
import kotlinx.android.synthetic.main.activity_encounter_read.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.base.EntourageActivity
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.tour.Encounter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

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
        title_close_button?.setOnClickListener {onCloseButton()}
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
    private fun onCloseButton() {
        finish()
    }

    private fun displayEncounter() {
        if (isFinishing) return
        encounter?.let {encounter ->
            val location = encounter.address?.let { address ->
                if (address.maxAddressLineIndex >= 0) {
                    address.getAddressLine(0)
                } else ""
            } ?: ""
            val encounterDate: String? = encounter.creationDate?.let { date-> DateFormat.getDateFormat(applicationContext).format(date) } ?: ""
            val encounterLocation: String = if (location.isEmpty()) {
                resources.getString(R.string.encounter_read_location_no_address,
                        encounter.userName,
                        encounter.streetPersonName,
                        encounterDate)
            } else {
                resources.getString(R.string.encounter_read_location,
                        encounter.userName,
                        encounter.streetPersonName,
                        location,
                        encounterDate)
            }
            edittext_street_person_name?.setText(encounterLocation)
            edittext_message?.setText(encounter.message)
        }
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