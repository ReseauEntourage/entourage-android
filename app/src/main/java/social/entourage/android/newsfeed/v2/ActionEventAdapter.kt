package social.entourage.android.newsfeed.v2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

/**
 * ActionEventAdapter.
 */
class ActionEventAdapter(var homecard:HomeCard,val listener:HomeViewHolderListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val minimumItemsToShowMore = 2
    private val minimalCellForSpecialCell = 1
    val TYPE_MORE = 0
    val TYPE_CELL = 1
    val TYPE_OTHER = 2
    var isSpecialCell = false

    override fun getItemViewType(position: Int): Int {
        if (isSpecialCell && position == homecard.arrayCards.size) {
            return TYPE_OTHER
        }

        if (position == homecard.arrayCards.size) return TYPE_MORE
        return TYPE_CELL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == TYPE_CELL) {
            if (homecard.type == HomeCardType.ACTIONS) {
                val view = inflater.inflate(R.layout.layout_cell_action, parent, false)

                return ActionVH(view)
            }
            else {
                val view = inflater.inflate(R.layout.layout_cell_event, parent, false)
                return EventVH(view)
            }
        }
        else if (viewType == TYPE_OTHER) {
            val view:View
            if (homecard.type == HomeCardType.ACTIONS) {
                view = inflater.inflate(R.layout.layout_cell_action_help, parent, false)
            }
            else {
                view = inflater.inflate(R.layout.layout_cell_event_zone, parent, false)
            }

            return OtherVH(view,homecard.type)
        }
        val view = inflater.inflate(R.layout.layout_cell_action_more, parent, false)

        return ShowMoreVH(view,homecard.type)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if ( isSpecialCell && position == homecard.arrayCards.size) {
            (holder as OtherVH).bind(listener)
            return
        }
        if (position == homecard.arrayCards.size) {
            (holder as ShowMoreVH).bind(listener)
            return
        }
        if (homecard.type == HomeCardType.ACTIONS) {
            (holder as ActionVH).bind(homecard.arrayCards[position].data, listener,position,false)
        }
        else {
            (holder as EventVH).bind(homecard.arrayCards[position].data, listener,position,false)
        }
    }

    override fun getItemCount(): Int {

        isSpecialCell = homecard.arrayCards.size <= minimalCellForSpecialCell

        if (isSpecialCell) {
            return homecard.arrayCards.size + 1
        }

        val _showMore = if (homecard.arrayCards.size >= minimumItemsToShowMore) 1 else 0
        return homecard.arrayCards.size + _showMore
    }
}
