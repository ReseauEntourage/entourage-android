package social.entourage.android.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import timber.log.Timber
import java.util.*

/**
 * Created by mihaiionescu on 05/05/16.
 */
open class EntourageBaseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items: ArrayList<TimestampedObject> = ArrayList()
    protected val viewHolderFactory = ViewHolderFactory()
    var viewHolderListener: EntourageViewHolderListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cardViewHolder = viewHolderFactory.getViewHolder(parent, viewType)
        cardViewHolder.viewHolderListener = viewHolderListener
        return cardViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < positionOffset) {
            Timber.e("Adapter trying to populate Top View")
            return
        }
        (holder as BaseCardViewHolder).populate(items[position - positionOffset])
    }

    override fun getItemCount(): Int {
        return items.size + positionOffset
    }

    open val dataItemCount: Int
        get() = items.size

    override fun getItemViewType(position: Int): Int {
        return if (position < positionOffset) {
            TimestampedObject.TOP_VIEW
        } else items[position - positionOffset].type
    }

    open fun addItems(addItems: List<TimestampedObject>) {
        val positionStart = (if (items.size == 0) 0 else items.size - 1) + positionOffset
        for (to in addItems) {
            addCardInfo(to, false)
        }
        notifyItemRangeInserted(positionStart, addItems.size)
    }

    fun addCardInfo(cardInfo: TimestampedObject) {
        addCardInfo(cardInfo, true)
    }

    open fun addCardInfo(cardInfo: TimestampedObject, notifyView: Boolean) {
        items.add(cardInfo)
        if (notifyView) {
            notifyItemInserted(items.size - 1 + positionOffset)
        }
    }

    open fun insertCardInfo(cardInfo: TimestampedObject, position: Int) {
        //add the card
        items.add(position, cardInfo)
        notifyItemInserted(position + positionOffset)
    }

    @Synchronized
    fun addCardInfoAfterTimestamp(cardInfo: TimestampedObject) {
        //search for the insert point
        for (i in items.indices) {
            val timestampedObject = items[i]
            if (timestampedObject.timestamp != null && cardInfo.timestamp != null) {
                if (timestampedObject.timestamp!!.after(cardInfo.timestamp)) {
                    //we found the insert point
                    insertCardInfo(cardInfo, i)
                    return
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
            if (timestampedObject.timestamp != null && cardInfo.timestamp != null) {
                if (timestampedObject.timestamp!!.before(cardInfo.timestamp)) {
                    //we found the insert point
                    insertCardInfo(cardInfo, i)
                    return
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
        items.filterIsInstance<FeedItem>().forEach {timestampedObject ->
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

    protected open val positionOffset: Int
        get() = 0
}