package social.entourage.android.base

import social.entourage.android.Constants
import java.util.*

/**
 * Created by mihaiionescu on 10/05/16.
 */
open class EntouragePagination {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    var page = 1
    @JvmField
    var itemsPerPage: Int
    var beforeDate:Date
        get() = if (isRefreshing) Date() else field
        //set(newBeforeDate) { this.beforeDate = newBeforeDate}
    protected var newestDate: Date? = null
    @JvmField
    var isLoading = false
    @JvmField
    var isRefreshing = false
    var newItemsAvailable = false
    var nextPageAvailable = false

    // ----------------------------------
    // CONSTRUCTORS
    // ---------------------------------
    constructor() {
        itemsPerPage = Constants.ITEMS_PER_PAGE
    }

    constructor(itemsPerPage: Int) {
        this.itemsPerPage = itemsPerPage
    }

    open fun reset() {
        page = 1
        itemsPerPage = Constants.ITEMS_PER_PAGE
        beforeDate = Date()
        newestDate = null
        isLoading = false
        isRefreshing = false
        newItemsAvailable = false
        nextPageAvailable = false
    }

    // ----------------------------------
    // METHODS
    // ---------------------------------
    fun loadedItems(loadedItems: Int) {
        nextPageAvailable = false
        if (!isRefreshing && loadedItems >= itemsPerPage) {
            page++
            nextPageAvailable = true
        }
        isLoading = false
    }

    fun loadedItems() {
        newItemsAvailable = false
        isLoading = false
    }

    fun loadedItems(newestDate: Date, oldestDate: Date) {
        if (this.newestDate == null) {
            this.newestDate = newestDate
        }
        if (!isRefreshing) {
            beforeDate = oldestDate
        } else {
            if (this.newestDate?.before(newestDate)==true) {
                this.newestDate = newestDate
                newItemsAvailable = true
            }
        }
        isLoading = false
    }

    init {
        beforeDate = Date()
    }
}