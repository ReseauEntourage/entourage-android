package social.entourage.android.api.model.tour

import social.entourage.android.R

enum class TourType (val typeName: String, val key: String, private val ressourceId: Int) {
    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    MEDICAL("medical", "tm", R.id.launcher_tour_type_medical),
    BARE_HANDS("barehands", "tb", R.id.launcher_tour_type_bare_hands),
    ALIMENTARY("alimentary", "ta", R.id.launcher_tour_type_alimentary);

    companion object {
        // ----------------------------------
        // PUBLIC METHODS
        // ----------------------------------
        fun findByRessourceId(ressourceId: Int): TourType {
            for (tourType in values()) {
                if (tourType.ressourceId == ressourceId) {
                    return tourType
                }
            }
            return BARE_HANDS
        }
    }

}