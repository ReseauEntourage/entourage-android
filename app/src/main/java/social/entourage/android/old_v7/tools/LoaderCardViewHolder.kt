package social.entourage.android.old_v7.tools

import android.view.View
import kotlinx.android.synthetic.main.layout_loader_card.view.*
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder

class LoaderCardViewHolder(view: View) : BaseCardViewHolder(view) {
    override fun bindFields() {
    }

    override fun populate(data: TimestampedObject) {
        itemView.layout_loader_card_progressbar?.visibility = View.VISIBLE
    }
}