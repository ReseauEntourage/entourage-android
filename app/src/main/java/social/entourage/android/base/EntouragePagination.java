package social.entourage.android.base;

import java.util.Date;

import social.entourage.android.Constants;

/**
 * Created by mihaiionescu on 10/05/16.
 */
public class EntouragePagination {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    public int page = 1;
    public int itemsPerPage;
    protected Date beforeDate = new Date();
    protected Date newestDate = null;
    public boolean isLoading = false;
    public boolean isRefreshing = false;
    public boolean newItemsAvailable = false;
    public boolean nextPageAvailable = false;

    // ----------------------------------
    // CONSTRUCTORS
    // ---------------------------------

    public EntouragePagination() {
        this.itemsPerPage = Constants.ITEMS_PER_PAGE;
    }

    public EntouragePagination(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ---------------------------------

    public Date getBeforeDate() {
        return (isRefreshing ? new Date() : beforeDate);
    }

    public void setBeforeDate(final Date beforeDate) {
        this.beforeDate = beforeDate;
    }


    // ----------------------------------
    // METHODS
    // ---------------------------------

    public void loadedItems(int loadedItems) {
        nextPageAvailable = false;
        if (!isRefreshing && loadedItems >= itemsPerPage) {
            page++;
            nextPageAvailable = true;
        }
        isLoading = false;
    }

    public void loadedItems(Date newestDate, Date oldestDate) {
        if (newestDate == null) {
            newItemsAvailable = false;
        }
        else {
            if (this.newestDate == null) {
                this.newestDate = newestDate;
            }
            if (!isRefreshing) {
                beforeDate = oldestDate;
            }
            else {
                if (this.newestDate.before(newestDate)) {
                    this.newestDate = newestDate;
                    newItemsAvailable = true;
                }
            }
        }
        isLoading = false;
    }

}
