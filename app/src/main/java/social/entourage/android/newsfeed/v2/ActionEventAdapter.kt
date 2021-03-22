package social.entourage.android.newsfeed.v2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

/**
 * ActionEventAdapter.
 */
class ActionEventAdapter(var homecard:HomeCard,val listener:HomeViewHolderListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val minimumItemsToShowMore = 2
    val TYPE_MORE = 0
    val TYPE_CELL = 1

    override fun getItemViewType(position: Int): Int {
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
        val view = inflater.inflate(R.layout.layout_cell_action_more, parent, false)

        return ShowMoreVH(view,homecard.type)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == homecard.arrayCards.size) {
            (holder as ShowMoreVH).bind(listener)
            return
        }
        if (homecard.type == HomeCardType.ACTIONS) {
            (holder as ActionVH).bind(homecard.arrayCards[position].data, listener)
        }
        else {
            (holder as EventVH).bind(homecard.arrayCards[position].data, listener)
        }
    }

    override fun getItemCount(): Int {
        val _showMore = if (homecard.arrayCards.size > minimumItemsToShowMore) 1 else 0
        return homecard.arrayCards.size + _showMore
    }
}
