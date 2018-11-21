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

    private boolean closedEntourages = true;
    private boolean showOwnEntouragesOnly = false;
    private boolean showJoinedEntourages = false;
    private boolean showUnreadOnly = false;
    private boolean showPartnerEntourages = false;

    private boolean entourageTypeDemand = true;
    private boolean entourageTypeContribution = true;

    private boolean showTours = true;

    @Expose(serialize = false, deserialize = false)
    private String allTourTypes;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyEntouragesFilter() {
    }

    // ----------------------------------
    // Getters and setters
    // ----------------------------------

    public boolean isClosedEntourages() {
        return closedEntourages;
    }

    public void setClosedEntourages(final boolean closedEntourages) {
        this.closedEntourages = closedEntourages;
    }

    public boolean isShowOwnEntouragesOnly() {
        return false; // in 5.0+ we ignore this setting
    }

    public void setShowOwnEntouragesOnly(final boolean showOwnEntouragesOnly) {
        this.showOwnEntouragesOnly = showOwnEntouragesOnly;
    }

    public boolean isShowJoinedEntourages() {
        return false;  // in 5.0+ we ignore this setting
    }

    public void setShowJoinedEntourages(final boolean showJoinedEntourages) {
        this.showJoinedEntourages = showJoinedEntourages;
    }

    public boolean isShowUnreadOnly() {
        return showUnreadOnly;
    }

    public void setShowUnreadOnly(final boolean showUnreadOnly) {
        this.showUnreadOnly = showUnreadOnly;
    }

    public boolean isShowPartnerEntourages() {
        return false; // in 5.0+ we ignore this setting
    }

    public void setShowPartnerEntourages(final boolean showPartnerEntourages) {
        this.showPartnerEntourages = showPartnerEntourages;
    }

    public boolean isEntourageTypeDemand() {
        return entourageTypeDemand;
    }

    public void setEntourageTypeDemand(final boolean entourageTypeDemand) {
        this.entourageTypeDemand = entourageTypeDemand;
    }

    public boolean isEntourageTypeContribution() {
        return entourageTypeContribution;
    }

    public void setEntourageTypeContribution(final boolean entourageTypeContribution) {
        this.entourageTypeContribution = entourageTypeContribution;
    }

    public boolean isShowTours() {
        return showTours;
    }

    public void setShowTours(final boolean showTours) {
        this.showTours = showTours;
    }


    // ----------------------------------
    // Methods
    // ----------------------------------

    public String getEntourageTypes() {
        StringBuilder entourageTypes = new StringBuilder();

        entourageTypeDemand = entourageTypeContribution = true; // in 5.0+ force to show all entourages

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
        closedEntourages = true; // in 5.0+ force to show closed entourages
        if (closedEntourages) return "all";
        return "active";
    }

    public String getTourTypes() {
        showTours = true; // in 5.0+ force to show all the tours
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
