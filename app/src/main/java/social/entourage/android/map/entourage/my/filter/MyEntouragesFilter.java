package social.entourage.android.map.entourage.my.filter;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Entourage;

/**
 * Created by mihaiionescu on 10/08/16.
 */
public class MyEntouragesFilter implements Serializable {

    private static final long serialVersionUID = 8192790767027490636L;

    public boolean closedEntourages = true;
    public boolean showOwnEntouragesOnly = false;
    public boolean showJoinedEntourages= false;
    public boolean showUnreadOnly = false;
    public boolean showPartnerEntourages = false;

    public boolean entourageTypeDemand = true;
    public boolean entourageTypeContribution = true;

    public boolean showTours = true;

    @Expose(serialize = false, deserialize = false)
    private String allTourTypes;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyEntouragesFilter() {
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
            if (allTourTypes == null) {
                allTourTypes = TourType.MEDICAL.getName()
                        + ','
                        + TourType.BARE_HANDS.getName()
                        + ','
                        + TourType.ALIMENTARY.getName();
            }
            return allTourTypes;
        }
        return "";
    }

}
