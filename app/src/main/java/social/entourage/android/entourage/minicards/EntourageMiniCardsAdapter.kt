package social.entourage.android.entourage.minicards

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.EntourageBaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType

/**
 * Adapter to be used with Entourage Mini Cards RecyclerView<br></br>
 * It handles only Entourage Mini Cards
 * Created by Mihai Ionescu on 13/09/2017.
 */
class EntourageMiniCardsAdapter : EntourageBaseAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cardViewHolder = viewHolderFactory.getViewHolder(parent, viewType)
        if (cardViewHolder != null) {
            // Make the width of the mini card to be a percentage of the parent
            val params = cardViewHolder.itemView.layoutParams
            params.width = (parent.width * MINICARD_WIDTH_PERCENTAGE).toInt()
            cardViewHolder.itemView.layoutParams = params
            cardViewHolder.viewHolderListener = viewHolderListener
        }
        return cardViewHolder
    }

    override fun addItems(addItems: List<TimestampedObject>) {
        items.clear()
        items.addAll(addItems)
        notifyDataSetChanged()
    }

    companion object {
        private const val MINICARD_WIDTH_PERCENTAGE = 0.85f
    }

    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                ViewHolderType(EntourageMiniCardViewHolder::class.java, R.layout.layout_entourage_mini_card)
        )
    }
}