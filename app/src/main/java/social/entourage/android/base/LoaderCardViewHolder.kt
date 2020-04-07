package social.entourage.android.base

import android.view.View
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder
import kotlinx.android.synthetic.main.layout_loader_card.view.*

class LoaderCardViewHolder(view: View?) : BaseCardViewHolder(view) {
    override fun bindFields() {
    }

    override fun populate(data: TimestampedObject) {
        itemView.layout_loader_card_progressbar.visibility = View.VISIBLE
    }
}