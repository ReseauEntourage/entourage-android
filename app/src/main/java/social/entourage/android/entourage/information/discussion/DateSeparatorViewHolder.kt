package social.entourage.android.entourage.information.discussion

import android.view.View
import kotlinx.android.synthetic.main.layout_entourage_information_date_separator_card.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.Utils

/**
 * Created by mihaiionescu on 15/03/2017.
 */
class DateSeparatorViewHolder(view: View) : BaseCardViewHolder(view) {
    override fun bindFields() {
    }

    override fun populate(data: TimestampedObject) {
        itemView.tic_date_separator_timestamp?.text = Utils.dateAsStringFromNow((data as DateSeparator).timestamp, itemView.context)
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_entourage_information_date_separator_card
    }
}