package social.entourage.android.api.model.feed

import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Address
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication.Companion.me
import social.entourage.android.R
import social.entourage.android.api.model.LastMessage
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.TimestampedObject
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.*

/**
 * Created by mihaiionescu on 18/05/16.
 * needed for deserialize
 */
abstract class FeedItem() : TimestampedObject(), Serializable {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    @Expose(serialize = false)
    override var id: Long = 0

    @Expose(serialize = false)
    var uuid: String? = null
        get()  = field ?:""
        private set

    lateinit var status: String

    var author: FeedItemAuthor? = null

    @SerializedName("updated_at")
    var updatedTime: Date = Date()

    @Expose(serialize = false, deserialize = false)
    @Transient
    var startAddress: Address? = null

    @Expose(serialize = false)
    @SerializedName("number_of_people")
    var numberOfPeople = 0

    @Expose(serialize = false)
    @SerializedName("number_of_unread_messages")
    var numberOfUnreadMessages = 0

    @Expose(serialize = false)
    @SerializedName("join_status")
    lateinit var joinStatus: String

    @Expose(serialize = false)
    @SerializedName("last_message")
    var lastMessage: LastMessage? = null
        protected set

    @Expose(serialize = false)
    @SerializedName("share_url")
    var shareURL: String? = null

    //number of notifs received that should be added to number of unread messages
    @Expose(serialize = false, deserialize = false)
    protected var badgeCount = 0


    //CardInfo cache support
    @Expose(serialize = false, deserialize = false)
    @Transient
    lateinit var cachedCardInfoList: MutableList<TimestampedObject>

    @Expose(serialize = false, deserialize = false)
    @Transient
    lateinit var addedCardInfoList: MutableList<TimestampedObject>

    //Flag to indicate a newly created feed item
    @Expose(serialize = false, deserialize = false)
    var isNewlyCreated = false

    var postal_code:String? = null

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    init {
        initializeCardLists()
    }

    fun getUnreadMsgNb(): Int { return badgeCount + numberOfUnreadMessages}

    fun increaseBadgeCount(isChatMessage: Boolean) {
        if (!isChatMessage) {
            badgeCount++
        } else {
            numberOfUnreadMessages++ //numberOfUnreadMessages will be updated elsewhere
        }
    }

    fun decreaseBadgeCount() {
        if (badgeCount > 0) {
            badgeCount--
        }
    }

    fun setLastMessage(text: String, author: String) {
        if (lastMessage == null) {
            lastMessage = LastMessage()
        }
        lastMessage?.setMessage(text, author)
    }

    fun isClosed(): Boolean { return STATUS_CLOSED == status || STATUS_FREEZED == status}

    fun isOngoing(): Boolean { return STATUS_ON_GOING == status}

    fun isPrivate(): Boolean {return JOIN_STATUS_ACCEPTED == joinStatus}

    open fun isFreezed(): Boolean {return STATUS_FREEZED == status}

    fun isMine(context: Context?): Boolean {
        author?.let {
            me(context)?.let { me ->
                return it.userID == me.id
            }
        }
        return false
    }

    fun isSuspended(): Boolean { return STATUS_SUSPENDED == status}

    // ----------------------------------
    // UI METHODS
    // ----------------------------------
    open fun getIconDrawable(context: Context): Drawable? {
        return null
    }

    open fun getIconURL(): String? {return null}

    open fun showHeatmapAsOverlay(): Boolean {return true }

    open fun getHeatmapResourceId(): Int { return R.drawable.heat_zone}

    open fun getFeedTypeColor(): Int {return 0}

    open fun canBeClosed(): Boolean { return true }

    open fun showAuthor(): Boolean { return true }

    @StringRes
    open fun getJoinRequestTitle(): Int { return R.string.tour_info_request_join_title_tour}

    @StringRes
    open fun getJoinRequestButton(): Int {return R.string.tour_info_request_join_button_tour}

    @StringRes
    open fun getQuitDialogTitle(): Int {return R.string.tour_info_quit_tour_title}

    @StringRes
    open fun getQuitDialogMessage(): Int{ return R.string.tour_info_quit_tour_description}

    fun showInviteViewAfterCreation(): Boolean {
        return true
    }

    fun showEditEntourageView(): Boolean {
        return true
    }

    @StringRes
    open fun getFreezedCTAText():  Int {
        return R.string.tour_cell_button_freezed
    }

    @ColorRes
    open fun getFreezedCTAColor(): Int {
        return R . color . greyish
    }

    @StringRes
    open fun getClosingLoaderMessage(): Int {return R.string.loader_title_tour_finish}

    @StringRes
    open fun getClosedToastMessage(): Int {return if (isFreezed()) R.string.tour_freezed else R.string.local_service_stopped}

    // ----------------------------------
    // COPY OBJECT METHODS
    // ----------------------------------
    override fun copyLocalFields(other: TimestampedObject) {
        super.copyLocalFields(other)
        val otherFeedItem = other as FeedItem
        badgeCount = otherFeedItem.badgeCount
    }

    // ----------------------------------
    // CARD INFO METHODS
    // ----------------------------------
    private fun initializeCardLists() {
        cachedCardInfoList = ArrayList()
        addedCardInfoList = ArrayList()
    }

    fun addCardInfo(cardInfo: TimestampedObject) {
        if (cachedCardInfoList.contains(cardInfo)) {
            return
        }
        cachedCardInfoList.add(cardInfo)
        addedCardInfoList.add(cardInfo)
        Collections.sort(cachedCardInfoList, TimestampedObjectComparatorOldToNew())
    }

    fun removeCardInfo(cardInfo: TimestampedObject) {
        cachedCardInfoList.remove(cardInfo)
    }

    fun addCardInfoList(cardInfoList: List<TimestampedObject>): Int {
        for (timestampedObject in cardInfoList) {
            if (cachedCardInfoList.contains(timestampedObject)) {
                continue
            }
            cachedCardInfoList.add(timestampedObject)
            addedCardInfoList.add(timestampedObject)
        }
        if (cachedCardInfoList.size > 0) {
            Collections.sort(cachedCardInfoList, TimestampedObjectComparatorOldToNew())
        }
        if (addedCardInfoList.size > 0) {
            Collections.sort(addedCardInfoList, TimestampedObjectComparatorOldToNew())
        }
        return addedCardInfoList.size
    }

    fun getTypedCardInfoList(cardType: Int): List<TimestampedObject> {
        val typedCardInfoList: MutableList<TimestampedObject> = ArrayList()
        for (timestampedObject in cachedCardInfoList) {
            if (timestampedObject.type == cardType) {
                typedCardInfoList.add(timestampedObject)
            }
        }
        return typedCardInfoList
    }

    fun clearAddedCardInfoList() {
        addedCardInfoList.clear()
    }

    // ----------------------------------
    // SERIALIZATION METHODS
    // ----------------------------------
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        initializeCardLists()
    }

    // ----------------------------------
    // ABSTRACT METHODS
    // ----------------------------------
    abstract fun getGroupType(): String
    abstract fun getFeedTypeLong(context: Context): String
    abstract fun getTitle(): String?
    abstract fun getDescription(): String?
    abstract fun getCreationTime(): Date
    abstract fun getStartTime(): Date
    abstract fun getEndTime(): Date?
    abstract fun setEndTime(endTime: Date)
    abstract fun getDisplayAddress(): String?
    abstract fun getStartPoint(): LocationPoint?

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val serialVersionUID = 6130064134883067122L
        const val KEY_FEEDITEM = "social.entourage.android.KEY_FEEDITEM"
        const val KEY_FEEDITEM_ID = "social.entourage.android.KEY_FEEDITEM_ID"
        const val KEY_FEEDITEM_UUID = "social.entourage.android.KEY_FEEDITEM_UUID"
        const val KEY_FEEDITEM_TYPE = "social.entourage.android.KEY_FEEDITEM_TYPE"
        const val STATUS_OPEN = "open"
        const val STATUS_CLOSED = "closed"
        const val STATUS_ON_GOING = "ongoing"
        const val STATUS_FREEZED = "freezed"
        const val STATUS_SUSPENDED = "suspended"
        const val JOIN_STATUS_NOT_REQUESTED = "not_requested"
        const val JOIN_STATUS_PENDING = "pending"
        const val JOIN_STATUS_ACCEPTED = "accepted"
        const val JOIN_STATUS_REJECTED = "rejected"
        const val JOIN_STATUS_CANCELLED = "cancelled"
        const val JOIN_STATUS_QUITED = "quited" // This status does not exist on server, just locally
    }
}