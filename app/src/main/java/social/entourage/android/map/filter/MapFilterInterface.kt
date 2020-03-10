package social.entourage.android.map.filter

/**
 * Created by mihaiionescu on 17/05/16.
 */
interface MapFilterInterface {
    fun getTypes(): String?
    fun getTimeFrame(): Int
    fun showPastEvents(): Boolean
    fun entourageCreated()
    fun validateCategories()
    fun isDefaultFilter(): Boolean
    fun setDefaultValues(isProUser: Boolean = false)
}