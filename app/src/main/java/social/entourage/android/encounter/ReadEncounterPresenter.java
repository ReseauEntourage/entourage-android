package social.entourage.android.encounter;

import social.entourage.android.api.model.map.Encounter;

public class ReadEncounterPresenter {
    private final ReadEncounterActivity activity;

    public ReadEncounterPresenter(final ReadEncounterActivity activity) {
        this.activity = activity;
    }

    public void displayEncounter() {
        activity.displayEncounter();
    }
}
