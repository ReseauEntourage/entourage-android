package social.entourage.android.map.filter;

/**
 * Created by mihaiionescu on 27/10/16.
 */

public class MapFilterPro extends MapFilter {

    private static MapFilter ourInstance = new MapFilterPro();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    protected MapFilterPro() {
        tourTypeMedical = true;
        tourTypeSocial = true;
        tourTypeDistributive = true;

        entourageTypeDemand = false;
        entourageTypeContribution = false;

        showTours = true;
    }

}
