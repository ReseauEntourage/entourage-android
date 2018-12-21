package social.entourage.android.newsfeed;

import java.util.Date;

import social.entourage.android.Constants;
import social.entourage.android.base.EntouragePagination;

/**
 * Created by mihaiionescu on 17/05/2017.
 */

public class NewsfeedPagination extends EntouragePagination {

    private static final int[] availableDistances = { 10, 20, 40 };

    public int distance; // kilometers
    private int distanceIndex;

    private String lastFeedItemUUID = null;

    public NewsfeedPagination() {
        super();
        this.distance = availableDistances[0];
        this.distanceIndex = 0;
    }

    public void reset() {
        page = 1;
        itemsPerPage = Constants.ITEMS_PER_PAGE;
        beforeDate = new Date();
        newestDate = null;
        lastFeedItemUUID = null;
        isLoading = false;
        isRefreshing = false;
        nextPageAvailable = false;
    }

    public boolean isNextDistanceAvailable() {
        return distanceIndex < (availableDistances.length - 1);
    }

    public void setNextDistance() {
        if (distanceIndex == (availableDistances.length - 1)) return;
        distanceIndex++;
        this.distance = availableDistances[distanceIndex];
    }

    public String getLastFeedItemUUID() {
        return lastFeedItemUUID;
    }

    public void setLastFeedItemUUID(final String lastFeedItemUUID) {
        this.lastFeedItemUUID = lastFeedItemUUID;
        newItemsAvailable = true;
    }
}
