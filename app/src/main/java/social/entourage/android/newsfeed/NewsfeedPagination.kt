package social.entourage.android.newsfeed

import social.entourage.android.base.EntouragePagination

/**
 * Created by mihaiionescu on 17/05/2017.
 */
class NewsfeedPagination : EntouragePagination() {
    @JvmField
    var distance: Int  // kilometers
    private var distanceIndex: Int
    var lastFeedItemUUID: String? = null
        set(lastFeedItemUUID)  {
            newItemsAvailable = true
            field = lastFeedItemUUID
        }

    override fun reset() {
        super.reset()
        lastFeedItemUUID = null
        distance = availableDistances[0]
        distanceIndex = 0
    }

    val isNextDistanceAvailable: Boolean
        get() = distanceIndex < availableDistances.size - 1

    fun setNextDistance() {
        if (distanceIndex == availableDistances.size - 1) return
        distanceIndex++
        distance = availableDistances[distanceIndex]
    }

    companion object {
        private val availableDistances = intArrayOf(10, 20, 40)
    }

    init {
        distance = availableDistances[0]
        distanceIndex = 0
        lastFeedItemUUID = null
    }
}