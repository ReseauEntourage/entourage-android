package social.entourage.android.api;

import social.entourage.android.api.model.map.Encounter;

public class EncounterResponse {

    private final Encounter encounter;

    public EncounterResponse(Encounter encounter) {
        this.encounter = encounter;
    }

    public Encounter getEncounter() {
        return encounter;
    }
}
