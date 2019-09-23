package social.entourage.android.tour.encounter;

import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.tape.EncounterTaskResult;

/**
 * Created by mihaiionescu on 04/05/2017.
 */

public interface EncounterUploadCallback {
    void onSuccess(Encounter encounter, EncounterTaskResult.OperationType operationType);

    void onFailure(Encounter encounter, EncounterTaskResult.OperationType operationType);
}
