package social.entourage.android.api.model;

import social.entourage.android.R;

public enum TourType {
    SOCIAL("social", R.id.launcher_tour_type_bare_hands),
    OTHER("other", R.id.launcher_tour_type_bare_hands),
    FOOD("food", R.id.launcher_tour_type_alimentary);

    private final String name;
    private final int ressourceId;

    TourType(String name, int ressourceId) {
        this.name = name;
        this.ressourceId = ressourceId;
    }

    public static TourType findByRessourceId(int ressourceId) {
        for (TourType tourType : TourType.values()) {
            if (tourType.getRessourceId() == ressourceId) {
                return tourType;
            }
        }
        return OTHER;
    }

    public String getName() {
        return name;
    }

    private int getRessourceId() {
        return ressourceId;
    }
}
