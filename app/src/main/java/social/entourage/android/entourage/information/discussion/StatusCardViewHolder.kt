package social.entourage.android.entourage.information.discussion

import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.layout_entourage_information_status_card_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder

/**
 * View Holder that displays information about a change in the status of a feed item
 * Created by Mihai Ionescu on 04/10/2018.
 */
class StatusCardViewHolder(view: View) : BaseCardViewHolder(view) {
    override fun bindFields() {}

    override fun populate(data: TimestampedObject) {
        if (data !is ChatMessage) return
        if(data.metadata == null) return
        val colorHex = String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(itemView.context, R.color.accent))
        val htmlText = itemView.context.getString(
                R.string.status_message_card_details,
                colorHex,
                data.userName,
                data.content
        )
        itemView.tic_status_message?.setHtmlString(htmlText)
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.layout_entourage_information_status_card_view
    }
}