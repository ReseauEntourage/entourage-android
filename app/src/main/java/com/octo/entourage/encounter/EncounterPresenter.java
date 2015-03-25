package com.octo.entourage.encounter;

import com.octo.entourage.api.model.map.Encounter;

/**
 * Presenter controlling the main activity
 */
public class EncounterPresenter {
    private final EncounterActivity activity;

    public EncounterPresenter(final EncounterActivity activity) {
        this.activity = activity;
    }

    public void displayEncounter(Encounter encounter) {
        activity.displayEncounter(encounter);
    }
}
