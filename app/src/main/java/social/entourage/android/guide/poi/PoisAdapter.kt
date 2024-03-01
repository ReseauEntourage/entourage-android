package social.entourage.android.guide.poi

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.OnMapReadyCallback
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.guide.poi.ViewHolderFactory.ViewHolderType
import java.util.Date

/**
 * Point of interest adapter
 *
 * Created by mihaiionescu on 26/04/2017.
 */
class PoisAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items: ArrayList<TimestampedObject> = ArrayList()
    private val viewHolderFactory = ViewHolderFactory()

    override fun getItemCount(): Int {
        return items.size + positionOffset
    }

    val dataItemCount: Int
        get() = items.size

    private val mDaySections = HashMap<Date, Int>() //Move from discussionAdapter . use here to clear array before refresh - To fix date separator
    // visibily error
    fun addItems(addItems: List<TimestampedObject>) {
        mDaySections.clear()
        val positionStart = (if (items.size == 0) 0 else items.size - 1) + positionOffset
        for (to in addItems) {
            addCardInfo(to, false)
        }
        notifyItemRangeInserted(positionStart, addItems.size)
    }

    fun addCardInfo(cardInfo: TimestampedObject) {
        addCardInfo(cardInfo, true)
    }

    private fun addCardInfo(cardInfo: TimestampedObject, notifyView: Boolean) {
        items.add(cardInfo)
        if (notifyView) {
            notifyItemInserted(items.size - 1 + positionOffset)
        }
    }

    private fun insertCardInfo(cardInfo: TimestampedObject, position: Int) {
        //add the card
        items.add(position, cardInfo)
        notifyItemInserted(position + positionOffset)
    }

    @Synchronized
    fun addCardInfoAfterTimestamp(cardInfo: TimestampedObject) {
        //search for the insert point
        for (i in items.indices) {
            val timestampedObject = items[i]
            timestampedObject.timestamp?.let { objectTimestamp ->
                cardInfo.timestamp?.let { cardInfoTimestamp ->
                    if (objectTimestamp.after(cardInfoTimestamp)) {
                        //we found the insert point
                        insertCardInfo(cardInfo, i)
                        return
                    }
                }
            }
        }
        //not found, add it at the end of the list
        addCardInfo(cardInfo, true)
    }

    fun addCardInfoBeforeTimestamp(cardInfo: TimestampedObject) {
        //search for the insert point
        for (i in items.indices) {
            val timestampedObject = items[i]
            timestampedObject.timestamp?.let { objectTimestamp ->
                cardInfo.timestamp?.let { cardInfoTimestamp ->
                    if (objectTimestamp.before(cardInfoTimestamp)) {
                        //we found the insert point
                        insertCardInfo(cardInfo, i)
                        return
                    }
                }
            }
        }
        //not found, add it at the end of the list
        addCardInfo(cardInfo, true)
    }

    fun getCardAt(position: Int): TimestampedObject? {
        return if (position < 0 || position >= items.size) null else items[position]
    }

    fun findCard(card: TimestampedObject): TimestampedObject? {
        for (timestampedObject in items) {
            if (timestampedObject == card) {
                return timestampedObject
            }
        }
        return null
    }

    fun findCard(type: Int, id: Long): TimestampedObject? {
        for (timestampedObject in items) {
            if (timestampedObject.type == type && timestampedObject.id == id) {
                return timestampedObject
            }
        }
        return null
    }

    fun findCard(type: Int, uuid: String?): TimestampedObject? {
        items.filterIsInstance<FeedItem>().forEach { timestampedObject ->
            if (timestampedObject.type == type && timestampedObject.uuid.equals(uuid, ignoreCase = true)) {
                return timestampedObject
            }
        }
        return null
    }

    fun updateCard(card: TimestampedObject?) {
        if (card == null) {
            return
        }
        for (i in items.indices) {
            val timestampedObject = items[i]
            if (timestampedObject == card) {
                card.copyLocalFields(timestampedObject)
                items.removeAt(i)
                items.add(i, card)
                notifyItemChanged(i + positionOffset)
                return
            }
        }
    }

    fun removeCard(card: TimestampedObject?) {
        if (card == null) {
            return
        }
        for (i in items.indices) {
            val timestampedObject = items[i]
            if (timestampedObject == card) {
                items.removeAt(i)
                notifyItemRangeRemoved(i + positionOffset, 1)
                return
            }
        }
    }

    fun removeAll() {
        val oldCount = dataItemCount
        if (oldCount == 0) return
        items.clear()
        notifyItemRangeRemoved(positionOffset, oldCount)
    }

    private var mapViewHolder: MapViewHolder? = null
    private var onMapReadyCallback: OnMapReadyCallback? = null
    private var onFollowButtonClickListener: View.OnClickListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < positionOffset) { //header position
            if (mapViewHolder == null && holder is MapViewHolder) {
                mapViewHolder = holder
            }
            //we populate with  no data
            mapViewHolder?.populate()
            return
        }
        (holder as MapViewHolder).populate(items[position - positionOffset])
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TimestampedObject.TOP_VIEW
        } else items[position - positionOffset].type
    }

    fun setMapHeight(height: Int) {
        mapViewHolder?.setHeight(height)
    }

    fun setGeolocStatusIcon(visible: Boolean) {
        mapViewHolder?.setGeolocStatusIcon(visible)
    }

    fun displayGeolocStatusIcon(active: Boolean) {
        //TODO check that we display this
        mapViewHolder?.displayGeolocStatusIcon(active)
    }

    private val positionOffset: Int
        get() = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cardViewHolder = viewHolderFactory.getViewHolder(parent, viewType)
        if (viewType == TimestampedObject.TOP_VIEW) {
            mapViewHolder = cardViewHolder
            onMapReadyCallback?.let {cardViewHolder.setMapReadyCallback(it)}
            cardViewHolder.setFollowButtonOnClickListener(onFollowButtonClickListener)
            cardViewHolder.setGeolocStatusIcon(LocationUtils.isLocationPermissionGranted())
        }
        return cardViewHolder
    }

    fun setOnMapReadyCallback(onMapReadyCallback: OnMapReadyCallback) {
        this.onMapReadyCallback = onMapReadyCallback
    }

    fun setOnFollowButtonClickListener(onFollowButtonClickListener: View.OnClickListener) {
        this.onFollowButtonClickListener = onFollowButtonClickListener
    }

    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
                ViewHolderType(MapViewHolder::class.java, R.layout.layout_feed_full_map_card)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.GUIDE_POI,
                ViewHolderType(PoiViewHolder::class.java, R.layout.layout_poi_card)
        )
        setHasStableIds(false)
    }
}