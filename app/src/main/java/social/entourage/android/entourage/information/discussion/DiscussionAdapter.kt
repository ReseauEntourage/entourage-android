package social.entourage.android.entourage.information.discussion

import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType
import java.util.*

/**
 * Created by mihaiionescu on 02/03/16.
 */
class DiscussionAdapter : BaseAdapter() {
    private val mDaySections = HashMap<Date, Int>()

    private fun addDateSeparator(date: Date, notifyView: Boolean) {
        val separator = DateSeparator(date)
        items.add(separator)
        mDaySections[date] = items.size - 1
        if (notifyView) {
            notifyItemInserted(items.size - 1)
        }
    }

    private fun insertDateSeparator(date: Date, position: Int) {
        val separator = DateSeparator(date)
        items.add(position, separator)
        mDaySections[date] = position
        notifyItemInserted(position)
    }

    override fun addCardInfo(cardInfo: TimestampedObject, notifyView: Boolean) {
        val timestamp = cardInfo.timestamp
        if (timestamp != null) {
            val dateOnly = Date(0)
            dateOnly.year = timestamp.year
            dateOnly.month = timestamp.month
            dateOnly.date = timestamp.date
            if (!mDaySections.containsKey(dateOnly)) {
                addDateSeparator(dateOnly, notifyView)
            }
        }
        items.add(cardInfo)
        if (notifyView) {
            notifyItemInserted(items.size - 1)
        }
    }

    override fun insertCardInfo(cardInfo: TimestampedObject, position: Int) {
        var realPosition = position
        val timestamp = cardInfo.timestamp
        if (timestamp != null) {
            val dateOnly = Date(0)
            dateOnly.year = timestamp.year
            dateOnly.month = timestamp.month
            dateOnly.date = timestamp.date
            if (!mDaySections.containsKey(dateOnly)) {
                insertDateSeparator(dateOnly, realPosition)
                realPosition++
            }
        }
        //add the card
        items.add(realPosition, cardInfo)
        notifyItemInserted(realPosition)
    }

    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.DATE_SEPARATOR,
                ViewHolderType(DateSeparatorViewHolder::class.java, DateSeparatorViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_OTHER,
                ViewHolderType(ChatMessageCardViewHolder::class.java, ChatMessageCardViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_ME,
                ViewHolderType(ChatMessageMeCardViewHolder::class.java, ChatMessageMeCardViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_STATUS,
                ViewHolderType(LocationCardViewHolder::class.java, LocationCardViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_USER_JOIN,
                ViewHolderType(TourUserJoinCardViewHolder::class.java, TourUserJoinCardViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENCOUNTER,
                ViewHolderType(EncounterCardViewHolder::class.java, EncounterCardViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.CHAT_MESSAGE_OUTING,
                ViewHolderType(OutingCardViewHolder::class.java, OutingCardViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.STATUS_UPDATE_CARD,
                ViewHolderType(StatusCardViewHolder::class.java, StatusCardViewHolder.layoutResource)
        )
        setHasStableIds(false)
    }
}