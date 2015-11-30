package social.entourage.android.api.tape;

import social.entourage.android.api.model.map.Encounter;

public class EncounterTaskResult {

    private boolean success;
    private Encounter encounter;

    public EncounterTaskResult(boolean success, Encounter encounter) {
        this.success = success;
        this.encounter = encounter;
    }

    public boolean isSuccess() {
        return success;
    }

    public Encounter getEncounter() {
        return encounter;
    }
}
