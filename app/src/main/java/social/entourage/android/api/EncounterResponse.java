package social.entourage.android.api;

import social.entourage.android.api.model.map.Encounter;

/**
 * Created by NIL on 25/03/2015.
 */
public class EncounterResponse {

    private Encounter encounter;

    public EncounterResponse(Encounter encounter) {
        this.encounter = encounter;
    }

    public Encounter getEncounter() {
        return encounter;
    }
}
