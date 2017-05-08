package social.entourage.android.api.tape;

import social.entourage.android.api.model.map.Encounter;

public class EncounterTaskResult {

    public enum OperationType {
        ENCOUNTER_ADD,
        ENCOUNTER_UPDATE
    };

    private boolean success;
    private Encounter encounter;
    private OperationType operationType;

    public EncounterTaskResult(boolean success, Encounter encounter, OperationType operationType) {
        this.success = success;
        this.encounter = encounter;
        this.operationType = operationType;
    }

    public boolean isSuccess() {
        return success;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
