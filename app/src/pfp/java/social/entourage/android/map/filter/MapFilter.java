package social.entourage.android.map.filter;

import java.io.Serializable;

/**
 * Created by Mihai Ionescu on 04/07/2018.
 */
public class MapFilter implements MapFilterInterface, Serializable {

    private static final long serialVersionUID = 1562838744560618668L;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    public boolean entourageTypeNeighborhood = true;
    public boolean entourageTypePrivateCircle = true;
    public boolean entourageTypeOuting = true;
    public boolean includePastEvents = false;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private static MapFilter ourInstance = new MapFilter();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    // ----------------------------------
    // MapFilterInterface implementation
    // ----------------------------------

    @Override
    public String getTypes() {
        StringBuilder entourageTypes = new StringBuilder("");

        if (entourageTypeNeighborhood) {
            entourageTypes.append("nh");
        }
        if (entourageTypePrivateCircle) {
            if (entourageTypes.length() > 0) entourageTypes.append(",");
            entourageTypes.append("pc");
        }
        if (entourageTypeOuting) {
            if (entourageTypes.length() > 0) entourageTypes.append(",");
            entourageTypes.append("ou");
        }

        return entourageTypes.toString();
    }

    @Override
    public boolean onlyMyEntourages() {
        return false;
    }

    @Override
    public int getTimeFrame() {
        return 720; // 30 days
    }

    @Override
    public boolean onlyMyPartnerEntourages() {
        return false;
    }

    @Override
    public void entourageCreated() {

    }

    @Override
    public void validateCategories() {

    }
}
