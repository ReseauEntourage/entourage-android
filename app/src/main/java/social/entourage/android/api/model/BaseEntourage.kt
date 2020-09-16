package social.entourage.android.api.model

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.location.EntourageLocation
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

/**
 * Created by Mihai Ionescu on 06/07/2018.
 */
open class BaseEntourage : FeedItem, Serializable {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    @SerializedName("created_at")
    private lateinit var createdTime: Date

    @SerializedName("group_type")
    private var groupType: String

    @SerializedName("entourage_type")
    var actionGroupType: String

    @SerializedName("display_category")
    var category: String? = null
    private var title: String? = null
    private var description: String? = null
    var location: LocationPoint? = null
    var outcome: EntourageCloseOutcome? = null
    var metadata: Metadata? = null

    @SerializedName("recipient_consent_obtained")
    var isRecipientConsentObtained = true

    @SerializedName("public")
    var isJoinRequestPublic = false

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    //needed for deserialize
    constructor() : super() {
        this.groupType = GROUPTYPE_ACTION
        this.actionGroupType = GROUPTYPE_ACTION_CONTRIBUTION
    }

    constructor(groupType: String, actionGroupType: String) : super() {
        this.groupType = groupType
        this.actionGroupType = actionGroupType
    }

    constructor(groupType: String, actionGroupType: String, category: String?,
                title: String, description: String, location: LocationPoint) : super() {
        this.groupType = groupType
        this.actionGroupType = actionGroupType
        this.category = category
        this.title = title
        this.description = description
        this.location = location
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------
    override fun getCreationTime(): Date {
        return createdTime
    }

    fun setCreationTime(creationTime: Date) {
        createdTime = creationTime
    }

    override fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String) {
        this.description = description
    }

    override fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    override fun getGroupType(): String {
        return groupType
    }

    fun setGroupType(groupType: String) {
        this.groupType = groupType
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    override fun isFreezed(): Boolean {
        return STATUS_CLOSED == status
    }

    fun isSame(entourage: BaseEntourage): Boolean {
        if (id != entourage.id) return false
        if (status != entourage.status) return false
        if (joinStatus != entourage.joinStatus) return false
        if (numberOfPeople != entourage.numberOfPeople) return false
        if (numberOfUnreadMessages != entourage.numberOfUnreadMessages) return false
        if (actionGroupType != entourage.actionGroupType) return false
        if (category != null && category != entourage.category) return false
        if (isJoinRequestPublic != entourage.isJoinRequestPublic) return false

        return (author?.isSame(entourage.author)==true)
    }

    /**
     * Returns the distance from the entourage starting point to the current location
     * If the current location or the starting point is null, it returns zero
     * @return distance in kilometers
     */
    fun distanceToCurrentLocation(): Int {
        val newLocation = EntourageLocation.currentLocation ?: return 0
        val distance = this.location?.distanceTo(LocationPoint(newLocation.latitude, newLocation.longitude)) ?:0.0f
        return floor(distance /1000.0f).toInt()
    }

    /**
     * Returns the distance from the entourage starting point to the given location
     * If the location or the starting point is null, it returns Integer.MAX_VALUE
     * @return distance in meters
     */
    fun distanceToLocation(newLocation: LatLng?): Int {
        if (newLocation == null) {
            return Int.MAX_VALUE
        }
        val distance: Float = this.location?.distanceTo(LocationPoint(newLocation.latitude, newLocation.longitude))
                ?: return Int.MAX_VALUE
        return floor(distance).toInt()
    }

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------
   override fun getFeedTypeLong(context: Context): String {
        return ""
    }

    override fun getFeedTypeColor(): Int {
        val entourageCategory = EntourageCategoryManager.findCategory(this)
        return entourageCategory?.typeColorRes ?: super.getFeedTypeColor()
    }

    override fun getStartTime(): Date {
        return createdTime
    }

    override fun getEndTime(): Date? {
        return updatedTime
    }

    override fun setEndTime(endTime: Date) {}
    override fun getStartPoint(): LocationPoint? {
        return location
    }

    override fun getDisplayAddress(): String? {
        return metadata?.displayAddress
    }

    override fun getIconDrawable(context: Context): Drawable? {
        EntourageCategoryManager.findCategory(this)?.let { entourageCategory ->
            AppCompatResources.getDrawable(context, entourageCategory.iconRes)?.let { categoryIcon ->
                categoryIcon.mutate()
                categoryIcon.clearColorFilter()
                categoryIcon.setColorFilter(ContextCompat.getColor(context, entourageCategory.typeColorRes), PorterDuff.Mode.SRC_IN)
                return categoryIcon
            }
        }
        return super.getIconDrawable(context)
    }

    @StringRes
    override fun getJoinRequestTitle(): Int {
        return R.string.tour_info_request_join_title_entourage
    }

    @StringRes
    override fun getJoinRequestButton(): Int {
        return R.string.tour_info_request_join_button_entourage
    }

    override fun getQuitDialogTitle(): Int {
        return R.string.entourage_info_quit_entourage_title
    }

    override fun getQuitDialogMessage(): Int {
        return R.string.entourage_info_quit_entourage_description
    }

    @StringRes
    override fun getFreezedCTAText(): Int {
        return if (outcome?.success==true) R.string.tour_cell_button_freezed_success else super.getFreezedCTAText()
    }

    @ColorRes
    override fun getFreezedCTAColor(): Int {
        return if (outcome?.success==true) R.color.accent else super.getFreezedCTAColor()
    }

    override fun getClosingLoaderMessage(): Int {
        return R.string.loader_title_action_finish
    }

    override fun getClosedToastMessage(): Int {
        return R.string.entourage_info_text_close
    }

    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------
    override val timestamp: Date
        get() = createdTime

    override fun hashString(): String {
        return HASH_STRING_HEAD + id
    }

    override fun equals(other: Any?): Boolean {
        return !(other == null || other.javaClass != this.javaClass) && id == (other as BaseEntourage).id
    }

    override val type: Int
        get() = ENTOURAGE_CARD

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    class EntourageJoinInfo(var distance: Int)

    class Metadata() : Serializable {

        @SerializedName("starts_at")
        var startDate: Date? = null
            private set

        @SerializedName("ends_at")
        var endDate: Date? = null
            private set

        @SerializedName("display_address")
        var displayAddress: String? = null

        @SerializedName("place_name")
        var placeName: String? = null

        @SerializedName("street_address")
        var streetAddress: String? = null

        @SerializedName("google_place_id")
        var googlePlaceId: String? = null
            private set

        fun setStartDate(startDate: Date) {
            this.startDate = startDate
        }

        fun getStartDateAsString(context: Context): String {
            startDate?.let {
                return SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_format), Locale.FRENCH)
                        .format(it)
            }
            return ""
        }

        fun getStartDateFullAsString(context: Context): String {
            startDate?.let {
                return SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_format_full), Locale.FRENCH)
                        .format(it)
            }
            return ""
        }

        fun getStartTimeAsString(context: Context): String {
            startDate?.let {
                //round the minutes to multiple of 15
                val calendar = Calendar.getInstance(Locale.getDefault())
                calendar.time = it
                calendar[Calendar.MINUTE] = calendar[Calendar.MINUTE] / 15 * 15
                //format it
                val df = SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_time_format), Locale.FRENCH)
                return df.format(calendar.time)
            }
            return ""
        }

        fun setEndDate(endDate: Date) {
            this.endDate = endDate
        }

        fun getEndDateFullAsString(context: Context): String {
            endDate?.let {
                val df = SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_format_full), Locale.FRENCH)
                return df.format(it)
            }
            return ""
        }

        fun getEndTimeAsString(context: Context): String {
            endDate?.let {
                //round the minutes to multiple of 15
                val calendar = Calendar.getInstance(Locale.getDefault())
                calendar.time = it
                calendar[Calendar.MINUTE] = calendar[Calendar.MINUTE]  / 15 * 15
                //format it
                val df = SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_time_format), Locale.FRENCH)
                return df.format(calendar.time)
            }
            return ""
        }

        fun getStartEndDatesAsString(context: Context): String {
            startDate?.let { start->
                endDate?.let{ end ->
                    val df = SimpleDateFormat("dd/MM", Locale.FRENCH)
                    return String.format(context.getString(R.string.entourage_metadata_date_startAt_endAt), df.format(start), df.format(end))
                }
            }
            return ""
        }

        fun getStartEndTimesAsString(context: Context): String {
            val calendarStart = Calendar.getInstance(Locale.getDefault())
            val calendarEnd = Calendar.getInstance(Locale.getDefault())
            calendarStart.time = startDate ?: return ""
            calendarEnd.time = endDate ?: return ""
            calendarStart[Calendar.MINUTE] = calendarStart[Calendar.MINUTE] / 15 * 15
            calendarEnd[Calendar.MINUTE] = calendarEnd[Calendar.MINUTE] / 15 * 15
            val df = SimpleDateFormat("HH'h'mm", Locale.FRENCH)
            return String.format(context.resources.getString(R.string.entourage_metadata_time_startAt_endAt), df.format(calendarStart.time), df.format(calendarEnd.time))
        }

        fun setGooglePlaceId(googlePlaceId: String) {
            this.googlePlaceId = googlePlaceId
        }
    }

    class EntourageCloseOutcome(val success: Boolean) : Serializable {

        companion object {
            private const val serialVersionUID = 4175623577343446888L
        }

    }

    companion object {
        private const val HASH_STRING_HEAD = "Entourage-"
        const val GROUPTYPE_ACTION_CONTRIBUTION = "contribution"
        const val GROUPTYPE_ACTION_DEMAND = "ask_for_help"
        const val GROUPTYPE_PRIVATE_CIRCLE = "private_circle"
        const val GROUPTYPE_NEIGHBORHOOD = "neighborhood"
        const val GROUPTYPE_CONVERSATION = "conversation"
        const val GROUPTYPE_OUTING = "outing"
        const val GROUPTYPE_ACTION = "action"
        const val HEATMAP_SIZE = 500f //meters
        private var MARKER_SIZE = 0
        fun getMarkerSize(context: Context): Int {
            if (MARKER_SIZE == 0) {
                MARKER_SIZE = context.resources.getDimensionPixelOffset(R.dimen.entourage_map_marker)
            }
            return MARKER_SIZE
        }
        fun getClassFromString(groupType: String?, actionGroupType: String?): Class<*>? {
            return when(groupType) {
                GROUPTYPE_ACTION -> {
                    when(actionGroupType) {
                        GROUPTYPE_ACTION_CONTRIBUTION -> EntourageContribution::class.java
                        GROUPTYPE_ACTION_DEMAND -> EntourageDemand::class.java
                        else -> EntourageDemand::class.java
                    }
                }
                GROUPTYPE_CONVERSATION -> EntourageConversation::class.java
                GROUPTYPE_OUTING -> EntourageEvent::class.java
                GROUPTYPE_NEIGHBORHOOD -> EntourageNeighborhood::class.java
                GROUPTYPE_PRIVATE_CIRCLE -> EntouragePrivateCircle::class.java
                else -> EntourageDemand::class.java
            }
        }

        fun create(groupType: String?, actionGroupType: String?, category: String?,
                   title: String, description: String, location: LocationPoint) : BaseEntourage
        {
            return when(groupType) {
                GROUPTYPE_ACTION -> {
                    when(actionGroupType) {
                        GROUPTYPE_ACTION_CONTRIBUTION -> EntourageContribution(category, title, description, location)
                        GROUPTYPE_ACTION_DEMAND -> EntourageDemand(category, title, description, location)
                        else -> EntourageDemand(category, title, description, location)
                    }
                }
                GROUPTYPE_CONVERSATION -> EntourageConversation(category, title, description, location)
                GROUPTYPE_OUTING -> EntourageEvent(category, title, description, location)
                GROUPTYPE_NEIGHBORHOOD -> EntourageNeighborhood(category, title, description, location)
                GROUPTYPE_PRIVATE_CIRCLE -> EntouragePrivateCircle(category, title, description, location)
                else -> EntourageDemand(category, title, description, location)
            }
        }

        private const val serialVersionUID = -1228955044085412292L
        const val NEWSFEED_TYPE = "Entourage"
    }
}