package social.entourage.android.api.model;

import social.entourage.android.R;

public enum TourType {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    MEDICAL("medical", R.id.launcher_tour_type_medical),
    BARE_HANDS("barehands", R.id.launcher_tour_type_bare_hands),
    ALIMENTARY("alimentary", R.id.launcher_tour_type_alimentary);

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final String name;
    private final int ressourceId;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    TourType(String name, int ressourceId) {
        this.name = name;
        this.ressourceId = ressourceId;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    private int getRessourceId() {
        return ressourceId;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public static TourType findByRessourceId(int ressourceId) {
        for (TourType tourType : TourType.values()) {
            if (tourType.getRessourceId() == ressourceId) {
                return tourType;
            }
        }
        return BARE_HANDS;
    }

    public String getName() {
        return name;
    }
}
