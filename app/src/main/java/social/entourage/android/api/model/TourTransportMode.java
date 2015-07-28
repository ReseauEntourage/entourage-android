package social.entourage.android.api.model;

import social.entourage.android.R;

public enum TourTransportMode {
    FEET("feet", R.id.launcher_tour_type_feet),
    CAR("car", R.id.launcher_tour_type_car);

    private final String name;
    private final int ressourceId;

    TourTransportMode(String name, int ressourceId) {
        this.name = name;
        this.ressourceId = ressourceId;
    }

    public static TourTransportMode findByRessourceId(int ressourceId) {
        for (TourTransportMode tourType : TourTransportMode.values()) {
            if (tourType.getRessourceId() == ressourceId) {
                return tourType;
            }
        }
        return FEET;
    }

    public String getName() {
        return name;
    }

    private int getRessourceId() {
        return ressourceId;
    }
}
