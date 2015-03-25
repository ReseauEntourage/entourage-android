package social.entourage.android.encounter;

import com.octo.entourage.api.model.map.Encounter;

/**
 * Presenter controlling the main activity
 */
public class ReadEncounterPresenter {
    private final ReadEncounterActivity activity;

    public ReadEncounterPresenter(final ReadEncounterActivity activity) {
        this.activity = activity;
    }

    public void displayEncounter(Encounter encounter) {
        activity.displayEncounter(encounter);
    }
}
