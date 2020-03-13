package social.entourage.android.map.filter

import java.io.Serializable

/**
 * Created by Mihai Ionescu on 04/07/2018.
 */
class MapFilter : MapFilterInterface, Serializable {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    @JvmField
    var entourageTypeNeighborhood = true
    @JvmField
    var entourageTypePrivateCircle = true
    @JvmField
    var entourageTypeOuting = true
    @JvmField
    var includePastEvents = false

    // ----------------------------------
    // MapFilterInterface implementation
    // ----------------------------------
    override fun getTypes(): String? {
        val entourageTypes = StringBuilder("")
        if (entourageTypeNeighborhood) {
            entourageTypes.append("nh")
        }
        if (entourageTypePrivateCircle) {
            if (entourageTypes.length > 0) entourageTypes.append(",")
            entourageTypes.append("pc")
        }
        if (entourageTypeOuting) {
            if (entourageTypes.length > 0) entourageTypes.append(",")
            entourageTypes.append("ou")
        }
        return entourageTypes.toString()
    }

    override fun getTimeFrame(): Int {
        return 720 // 30 days
    }

    override fun showPastEvents(): Boolean {
        return includePastEvents
    }

    override fun entourageCreated() {}
    override fun validateCategories() {}
    override fun isDefaultFilter(): Boolean {
        //TODO
        return when {
            !entourageTypeNeighborhood -> true
            !entourageTypePrivateCircle -> true
            !entourageTypeOuting -> true
            else -> false
        }
    }

    override fun setDefaultValues(isProUser: Boolean) {
        entourageTypeNeighborhood = true
        entourageTypePrivateCircle = true
        entourageTypeOuting = true
        includePastEvents = false
    }

    companion object {
        private const val serialVersionUID = 1562838744560618668L

        // ----------------------------------
        // Lifecycle
        // ----------------------------------
        val instance = MapFilter()
    }
}