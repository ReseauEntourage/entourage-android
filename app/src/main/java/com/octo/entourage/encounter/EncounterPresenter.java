package com.octo.entourage.encounter;

import com.octo.entourage.model.Encounter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

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
