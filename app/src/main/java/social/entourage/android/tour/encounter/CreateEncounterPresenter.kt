package social.entourage.android.tour.encounter

import com.squareup.otto.Subscribe
import com.squareup.tape.Task
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.tape.EncounterTapeTaskQueue
import social.entourage.android.api.tape.EncounterTaskResult
import social.entourage.android.api.tape.EncounterTaskResult.OperationType
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.tools.BusProvider.instance
import java.io.Serializable
import java.util.*
import javax.inject.Inject

/**
 * Presenter controlling the CreateEncounterActivity
 * @see CreateEncounterActivity
 */
class CreateEncounterPresenter
@Inject constructor(private val activity: CreateEncounterActivity?, private val authenticationController: AuthenticationController, private val queue: EncounterTapeTaskQueue)
    : EncounterUploadCallback {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var tourUUID: String? = null
    private var latitude = 0.0
    private var longitude = 0.0
    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun createEncounter(message: String?, streetPersonName: String?) {
        val encounter = Encounter()
        encounter.userName = authenticationController.me?.displayName
        encounter.message = message
        encounter.streetPersonName = streetPersonName
        encounter.creationDate = Date()
        encounter.tourId = tourUUID
        encounter.latitude = latitude
        encounter.longitude = longitude


//        queue.add(new EncounterUploadTask(encounter));
//        activity.onCreateEncounterFinished(null, encounter);
        EncounterUploadTask(encounter).execute(this)
    }

    fun updateEncounter(encounter: Encounter) {
        // to avoid endless requests to the server, we execute this task only once
        // with our presenter as the callback
        EncounterUploadTask(encounter).execute(this)
    }

    fun setTourUUID(tourUUID: String?) {
        this.tourUUID = tourUUID
    }

    fun setLatitude(latitude: Double) {
        this.latitude = latitude
    }

    fun setLongitude(longitude: Double) {
        this.longitude = longitude
    }

    val author: String
        get() = authenticationController.me?.displayName ?: ""

    // ----------------------------------
    // EncounterUploadCallback
    // ----------------------------------
    override fun onSuccess(encounter: Encounter?, operationType: OperationType?) {
       when (operationType) {
            OperationType.ENCOUNTER_ADD -> activity?.onCreateEncounterFinished(null, encounter)
            OperationType.ENCOUNTER_UPDATE -> activity?.onUpdatingEncounterFinished(null, encounter)
        }
    }

    override fun onFailure(encounter: Encounter?, operationType: OperationType?) {
        when (operationType) {
            OperationType.ENCOUNTER_ADD -> activity?.onCreateEncounterFinished("error", null)
            OperationType.ENCOUNTER_UPDATE -> activity?.onUpdatingEncounterFinished("error", null)
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    inner class EncounterUploadTask(val encounter: Encounter) : Task<EncounterUploadCallback?>, Serializable {
        private var callback: EncounterUploadCallback? = null

        override fun execute(callback: EncounterUploadCallback?) {
            if(callback==null) return
            this.callback = callback
            instance.register(this)
            instance.post(this)
        }

        @Subscribe
        fun taskResult(result: EncounterTaskResult) {
            instance.unregister(this)
            val resultEncounter = result.encounter
            val operationType = result.operationType
            if (encounter.creationDate == resultEncounter.creationDate) {
                if (result.isSuccess) {
                    callback?.onSuccess(resultEncounter, operationType)
                } else {
                    callback?.onFailure(resultEncounter, operationType)
                }
            } else {
                callback?.onFailure(resultEncounter, operationType)
            }
        }

        /*companion object { private const val serialVersionUID = -4119167198701340648L  }*/

    }

}