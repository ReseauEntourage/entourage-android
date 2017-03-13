package social.entourage.android.map.entourage.my.filter;

import social.entourage.android.api.model.map.Entourage;

/**
 * Created by mihaiionescu on 10/08/16.
 */
public class MyEntouragesFilter {

    public boolean closedEntourages = true;
    public boolean showOwnEntourages = false;
    public boolean showJoinedEntourages= false;

    public boolean entourageTypeDemand = true;
    public boolean entourageTypeContribution = true;

    public boolean showTours = true;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    private static MyEntouragesFilter ourInstance = new MyEntouragesFilter();

    public static MyEntouragesFilter getInstance() {
        return ourInstance;
    }

    private MyEntouragesFilter() {
    }

    // ----------------------------------
    // Methods
    // ----------------------------------

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

    public String getStatus() {
        if (closedEntourages) return "all";
        return "active";
    }

    public String getTourTypes() {
        if (showTours) {
            return "medical,social,distributive";
        }
        return "";
    }

}
