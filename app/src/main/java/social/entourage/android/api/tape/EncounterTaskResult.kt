package social.entourage.android.api.tape

import social.entourage.android.api.model.tour.Encounter

class EncounterTaskResult(val isSuccess: Boolean, val encounter: Encounter, val operationType: OperationType) {
    enum class OperationType {
        ENCOUNTER_ADD, ENCOUNTER_UPDATE
    }

}