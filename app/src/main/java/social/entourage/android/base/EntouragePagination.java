package social.entourage.android.base;

import social.entourage.android.Constants;

/**
 * Created by mihaiionescu on 10/05/16.
 */
public class EntouragePagination {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    public int page = 1;
    public boolean isLoading = false;
    public int itemsPerPage = 0;
    public boolean isRefreshing = false;

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

    // ----------------------------------
    // METHODS
    // ---------------------------------

    public void loadedItems(int loadedItems) {
        if (!isRefreshing && loadedItems >= itemsPerPage) {
            page++;
        }
        isLoading = false;
    }

}
