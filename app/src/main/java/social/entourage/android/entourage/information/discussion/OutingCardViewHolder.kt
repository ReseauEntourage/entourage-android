package social.entourage.android.entourage.information.discussion

import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.entourage.tour_information_outing_card_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.BusProvider
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * [BaseCardViewHolder] subclass, used to display outing information received as chat message,
 * in the feed item details screen<br></br><br></br>
 * Created by Mihai Ionescu on 30/07/2018.
 */
class OutingCardViewHolder(view: View?) : BaseCardViewHolder(view) {
    private var outingUUID: String? = null

    override fun bindFields() {
        itemView.setOnClickListener {onClick()}
        itemView.tic_outing_view_button?.setOnClickListener {onClick()}
    }

    private fun onClick() {
        if (outingUUID.isNullOrEmpty()) return
        BusProvider.instance.post(OnFeedItemInfoViewRequestedEvent(Entourage.ENTOURAGE_CARD, outingUUID, null))
    }

    override fun populate(chatMessage: TimestampedObject) {
        if (chatMessage !is ChatMessage) return
        val metadata = chatMessage.metadata ?: return
        val context = itemView.context
        val colorHex = String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(context, R.color.action_type_outing))
        val htmlText = context.getString(
                if (ChatMessage.Metadata.OPERATION_CREATED.equals(metadata.operation, ignoreCase = true)) R.string.outing_message_card_created_author else R.string.outing_message_card_updated_author,
                chatMessage.userName,
                colorHex)
        itemView.tic_outing_author?.setHtmlString(htmlText, null) // to allow the itemview to handle the click
        itemView.tic_outing_title?.text = metadata.title
        itemView.tic_outing_address?.text = metadata.displayAddress
        if (metadata.startsAt != null) {
            val df: DateFormat = SimpleDateFormat(context.getString(R.string.entourage_create_date_format), Locale.getDefault())
            itemView.tic_outing_date?.text = df.format(metadata.startsAt)
        }
        outingUUID = metadata.uuid
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.tour_information_outing_card_view
    }
}