package social.entourage.android.tour.encounter

import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.tape.EncounterTaskResult.OperationType

/**
 * Created by mihaiionescu on 04/05/2017.
 */
interface EncounterUploadCallback {
    fun onSuccess(encounter: Encounter?, operationType: OperationType?)
    fun onFailure(encounter: Encounter?, operationType: OperationType?)
}