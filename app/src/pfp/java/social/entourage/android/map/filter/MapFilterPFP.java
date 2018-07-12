package social.entourage.android.map.filter;

import java.io.Serializable;

/**
 * Created by Mihai Ionescu on 04/07/2018.
 */
public class MapFilterPFP extends MapFilter implements Serializable {

    private static final long serialVersionUID = 1562838744560618668L;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    public boolean entourageTypeNeighborhood = true;
    public boolean entourageTypePrivateCircle = true;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private static MapFilter ourInstance = new MapFilterPFP();

    public static MapFilter getInstance() {
        return ourInstance;
    }

    // ----------------------------------
    // Methods
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

        return entourageTypes.toString();
    }
}
