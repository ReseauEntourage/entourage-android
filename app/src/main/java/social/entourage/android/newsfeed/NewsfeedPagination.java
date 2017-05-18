package social.entourage.android.newsfeed;

import java.util.Date;

import social.entourage.android.Constants;
import social.entourage.android.base.EntouragePagination;

/**
 * Created by mihaiionescu on 17/05/2017.
 */

public class NewsfeedPagination extends EntouragePagination {

    private static final int[] availableRadius = { 10, 20, 40 };

    public int radius; // kilometers
    private int radiusIndex;

    public NewsfeedPagination() {
        super();
        this.radius = availableRadius[0];
        this.radiusIndex = 0;
    }

    public void reset() {
        page = 1;
        itemsPerPage = Constants.ITEMS_PER_PAGE;
        beforeDate = new Date();
        newestDate = null;
        isLoading = false;
        isRefreshing = false;
    }

    public boolean isNextRadiusAvailable() {
        return radiusIndex < availableRadius.length -1;
    }

    public void setNextRadius() {
        radiusIndex ++;
        if (radiusIndex == availableRadius.length) return;
        this.radius = availableRadius[radiusIndex];
    }
}
