package social.entourage.android.api.model;

import social.entourage.android.R;

public enum TourType {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    MEDICAL("medical", "tm", R.id.launcher_tour_type_medical),
    BARE_HANDS("barehands", "tb", R.id.launcher_tour_type_bare_hands),
    ALIMENTARY("alimentary", "ta", R.id.launcher_tour_type_alimentary);

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final String name;
    private final String key;
    private final int ressourceId;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    TourType(String name, String key, int ressourceId) {
        this.name = name;
        this.key = key;
        this.ressourceId = ressourceId;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    private int getRessourceId() {
        return ressourceId;
    }

    public String getKey() {
        return key;
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
