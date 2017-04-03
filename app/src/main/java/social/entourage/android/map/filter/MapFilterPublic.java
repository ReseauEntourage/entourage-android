package social.entourage.android.map.filter;

/**
 * Created by mihaiionescu on 27/10/16.
 */

public class MapFilterPublic extends MapFilter {

    private static final long serialVersionUID = 4224570539967770078L;

    private static MapFilter ourInstance = new MapFilterPublic();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    protected MapFilterPublic() {
        tourTypeMedical = false;
        tourTypeSocial = false;
        tourTypeDistributive = false;

        entourageTypeDemand = true;
        entourageTypeContribution = true;

        showTours = false;
    }
}
