package social.entourage.android.map.filter;

import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Entourage;

/**
 * Created by mihaiionescu on 17/05/16.
 */
public class MapFilter {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    public static final int DAYS_1 = 24; //hours
    public static final int DAYS_2 = 48; //hours
    public static final int DAYS_3 = 72; //hours

    public boolean tourTypeMedical = true;
    public boolean tourTypeSocial = true;
    public boolean tourTypeDistributive = true;

    public boolean entourageTypeDemand = true;
    public boolean entourageTypeContribution = true;

    public boolean showTours = true;

    public boolean onlyMyEntourages = false;

    public int timeframe = 24; //hours

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private static MapFilter ourInstance = new MapFilter();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    private MapFilter() {
    }

    // ----------------------------------
    // Methods
    // ----------------------------------

    public String getTourTypes() {
        StringBuilder tourTypes = new StringBuilder("");
        if (tourTypeMedical) {
            tourTypes.append(TourType.MEDICAL.getName());
        }
        if (tourTypeSocial) {
            if (tourTypes.length() > 0) tourTypes.append(",");
            tourTypes.append(TourType.BARE_HANDS.getName());
        }
        if (tourTypeDistributive) {
            if (tourTypes.length() > 0) tourTypes.append(",");
            tourTypes.append(TourType.ALIMENTARY.getName());
        }

        return tourTypes.toString();
    }

    public String getEntourageTypes() {
        StringBuilder entourageTypes = new StringBuilder("");

        if (entourageTypeDemand) {
            entourageTypes.append(Entourage.TYPE_DEMAND);
        }
        if (entourageTypeContribution) {
            if (entourageTypes.length() > 0) entourageTypes.append(",");
            entourageTypes.append(Entourage.TYPE_CONTRIBUTION);
        }

        return entourageTypes.toString();
    }

}
