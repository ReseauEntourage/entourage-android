package social.entourage.android.authentication;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import social.entourage.android.api.model.map.Tour;
import social.entourage.android.map.filter.MapFilter;

/**
 * Contains an user preferences, that can be saved
 * Created by Mihai Ionescu on 20/10/2017.
 */

public class UserPreferences implements Serializable {

    private boolean userToursOnly;
    @Expose(serialize = false)
    private boolean showNoEntouragesPopup = true;
    @Expose(serialize = false)
    private boolean showNoPOIsPopup = true;
    private boolean showInfoPOIsPopup = true;

    private MapFilter mapFilter = null;

    private Tour ongoingTour;

    public boolean isUserToursOnly() {
        return userToursOnly;
    }

    public void setUserToursOnly(final boolean userToursOnly) {
        this.userToursOnly = userToursOnly;
    }

    public boolean isShowNoEntouragesPopup() {
        return showNoEntouragesPopup;
    }

    public void setShowNoEntouragesPopup(final boolean showNoEntouragesPopup) {
        this.showNoEntouragesPopup = showNoEntouragesPopup;
    }

    public boolean isShowNoPOIsPopup() {
        return showNoPOIsPopup;
    }

    public void setShowNoPOIsPopup(final boolean showNoPOIsPopup) {
        this.showNoPOIsPopup = showNoPOIsPopup;
    }

    public boolean isShowInfoPOIsPopup() {
        return showInfoPOIsPopup;
    }

    public void setShowInfoPOIsPopup(final boolean showInfoPOIsPopup) {
        this.showInfoPOIsPopup = showInfoPOIsPopup;
    }

    public MapFilter getMapFilter() {
        return mapFilter;
    }

    public void setMapFilter(final MapFilter mapFilter) {
        this.mapFilter = mapFilter;
    }

    public Tour getOngoingTour() {
        return ongoingTour;
    }

    public void setOngoingTour(final Tour ongoingTour) {
        this.ongoingTour = ongoingTour;
    }
}
