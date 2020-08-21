package social.entourage.android.api.model.tour

import android.graphics.Bitmap
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.TimestampedObject
import java.util.*

/**
 * Created by mihaiionescu on 29/02/16.
 */
class TourInformation : TimestampedObject {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    var startDate: Date
    override var timestamp: Date? = null
    var feedType = 0
        private set
    var status: String
    var locationPoint: LocationPoint? = null
    var duration: Long  //millis
    var distance: Float  //meters
    var snapshot: Bitmap? = null

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    constructor() {
        startDate = Date()
        status = ""
        duration = 0
        distance = 0.0f
    }

    constructor(date: Date, timestamp: Date?, feedType: Int, status: String, locationPoint: LocationPoint?, duration: Long, distance: Float) {
        this.startDate = date
        this.timestamp = timestamp
        this.feedType = feedType
        this.status = status
        this.locationPoint = locationPoint
        this.duration = duration
        this.distance = distance
    }

    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------
    override fun hashString(): String {
        return HASH_STRING_HEAD + duration
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other.javaClass != this.javaClass) false else duration == (other as TourInformation).duration
        //return this.date.equals( ((TourTimestamp)o).date );
    }

    override val type: Int
        get() = TOUR_STATUS

    override val id: Long
        get() = 0

    companion object {
        private const val HASH_STRING_HEAD = "TourTimestamp-"
    }
}