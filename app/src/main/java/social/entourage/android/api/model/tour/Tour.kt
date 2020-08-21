package social.entourage.android.api.model.tour

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.feed.FeedItem
import java.io.Serializable
import java.util.*

class Tour : FeedItem, Serializable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @SerializedName("user_id")
    @Expose(serialize = false, deserialize = true)
    var userId = 0

    @SerializedName("tour_type")
    @JvmField
    var tourType = TourType.BARE_HANDS.typeName

    @SerializedName("start_time")
    @Expose(serialize = true, deserialize = true)
    private var startTime: Date

    @SerializedName("end_time")
    @Expose(serialize = true, deserialize = true)
    private var endTime: Date? = null

    @Expose(serialize = false, deserialize = false)
    @JvmField
    var duration: String? = null

    @Expose(serialize = true, deserialize = true)
    @JvmField
    var distance = 0f

    @Expose(serialize = false, deserialize = true)
    @SerializedName("tour_points")
    var tourPoints: MutableList<LocationPoint>

    @Expose(serialize = false, deserialize = true)
    @SerializedName("organization_name")
    @JvmField
    val organizationName: String? = null

    @Expose(serialize = false, deserialize = true)
    @SerializedName("organization_description")
    val organizationDescription: String? = null

    @Expose(serialize = false)
    var encounters: MutableList<Encounter>
        private set

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    init {
        startTime = Date()
        tourPoints = ArrayList()
        encounters = ArrayList()
    }

    //needed for deserialize
    constructor() : super()

    constructor(tourType: String) : super() {
        this.tourType = tourType
    }

    override fun getCreationTime(): Date {
        return startTime
    }

    override fun getStartTime(): Date {
        return startTime
    }

    override fun getEndTime(): Date? {
        return endTime
    }

    override fun getDisplayAddress(): String? {
        return null
    }

    fun setStartTime(startTime: Date) {
        this.startTime = startTime
    }

    override fun setEndTime(endTime: Date) {
        this.endTime = endTime
    }

    override fun toString(): String {
        return "tour : " + id + ", type : " + tourType + ", status : " + status + ", points : " + tourPoints.size
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun updateDistance(distance: Float) {
        this.distance += distance
    }

    fun addCoordinate(location: LocationPoint) {
        tourPoints.add(location)
    }

    fun addEncounter(encounter: Encounter) {
        encounters.add(encounter)
    }

    fun updateEncounter(updatedEncounter: Encounter) {
        for (encounter in encounters) {
            if (encounter.id == updatedEncounter.id) {
                encounters.remove(encounter)
                break
            }
        }
        encounters.add(updatedEncounter)
    }

    fun isSame(tour: Tour): Boolean {
        if (id != tour.id) return false
        if (tourPoints.size != tour.tourPoints.size) return false
        if (status != tour.status) return false
        if (numberOfPeople != tour.numberOfPeople) return false
        if (numberOfUnreadMessages != tour.numberOfUnreadMessages) return false
        if (joinStatus != tour.joinStatus) return false
        return author?.isSame(tour.author) ?: false
    }

    @get:DrawableRes
    val iconRes: Int
        get() {
            return when (tourType) {
                TourType.MEDICAL.typeName  -> R.drawable.ic_tour_medical
                TourType.ALIMENTARY.typeName -> R.drawable.ic_tour_distributive
                TourType.BARE_HANDS.typeName -> R.drawable.ic_tour_social
                else -> 0
            }
        }

    override fun getIconDrawable(context: Context): Drawable? {
        //@DrawableRes val iconRes = iconRes
        return if (iconRes != 0) {
            AppCompatResources.getDrawable(context, iconRes)
        } else super.getIconDrawable(context)
    }

    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------
    override val timestamp: Date
        get() = startTime

    override fun hashString(): String {
        return HASH_STRING_HEAD + id
    }

    override fun equals(other: Any?): Boolean {
        return !(other == null || other.javaClass != this.javaClass) && id == (other as Tour).id
    }

    override val type: Int
        get() = TOUR_CARD

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------
    override fun getGroupType(): String {
        return GROUPTYPE_TOUR
    }

    override fun getFeedTypeLong(context: Context): String {
        if (tourType == TourType.MEDICAL.typeName) {
            return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_medical).toLowerCase())
        } else if (tourType == TourType.ALIMENTARY.typeName) {
            return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_alimentary).toLowerCase())
        }
        ////Default Type: if (tourType.equals(TourType.BARE_HANDS.getName())) {
        return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_bare_hands).toLowerCase())
    }

    override fun getTitle(): String? {
        return organizationName
    }

    override fun getDescription(): String? {
        return ""
    }

    override fun getStartPoint(): LocationPoint? {
        return if (tourPoints.size == 0) null else tourPoints[0]
    }

    fun getEndPoint(): LocationPoint? {return if (tourPoints.size < 1) null else tourPoints[tourPoints.size - 1]}

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    class Tours(val tours: List<Tour>) : Serializable {

        companion object {
            private const val serialVersionUID = -9137864560567548841L
        }

    }

    class TourComparatorNewToOld : Comparator<Tour> {
        override fun compare(tour1: Tour, tour2: Tour): Int {
            val date1 = tour1.startTime
            val date2 = tour2.startTime
            return date2.compareTo(date1)
        }
    }

    class TourComparatorOldToNew : Comparator<Tour> {
        override fun compare(tour1: Tour, tour2: Tour): Int {
            val date1 = tour1.startTime
            val date2 = tour2.startTime
            return date1.compareTo(date2)
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val serialVersionUID = -5072043693523981962L
        private const val HASH_STRING_HEAD = "Tour-"
        const val GROUPTYPE_TOUR = "tour"
        const val KEY_TOUR = "social.entourage.android.KEY_TOUR"
        const val KEY_TOUR_ID = "social.entourage.android.KEY_TOUR_ID"
        const val KEY_TOURS = "social.entourage.android.KEY_TOURS"
        const val NEWSFEED_TYPE = "Tour"

        fun getHoursDiffToNow(fromDate: Date?): Long {
            val currentHours = System.currentTimeMillis() / Constants.MILLIS_HOUR
            var startHours = currentHours
            if (fromDate != null) {
                startHours = fromDate.time / Constants.MILLIS_HOUR
            }
            return currentHours - startHours
        }

        fun getStringDiffToNow(fromDate: Date?): String {
            val hours = getHoursDiffToNow(fromDate)
            return if (hours > 24) {
                "" + hours / 24 + "j"
            } else "" + hours + "h"
        }

        @ColorRes
        fun getTypeColorRes(type: String): Int {
            return when (type) {
                TourType.MEDICAL.typeName -> R.color.tour_type_medical
                TourType.ALIMENTARY.typeName -> R.color.tour_type_distributive
                TourType.BARE_HANDS.typeName -> R.color.tour_type_social
                else -> R.color.accent
            }
        }
    }
}