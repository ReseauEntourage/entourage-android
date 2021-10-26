package social.entourage.android.base.newsfeed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.home.ActionVH
import social.entourage.android.home.AnnounceVH
import social.entourage.android.home.HomeCard
import social.entourage.android.home.HomeViewHolderListener

/**
 * HeadlineAdapter.
 */
class HeadlineAdapter(var homecard: HomeCard, val listener: HomeViewHolderListener, val isLoading:Boolean): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val CELL_HEADLINE = 0
    val CELL_ACTION = 1
    val CELL_EMPTY = 2

    override fun getItemViewType(position: Int): Int {
        if (isLoading) return CELL_EMPTY
        val type = homecard.arrayCards[position].data

        if (type is Announcement) {
            return CELL_HEADLINE
        }
        return CELL_ACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (isLoading) {
            val view = inflater.inflate(R.layout.layout_cell_headline_empty, parent, false)
            return AnnounceVH(view)
        }

        if (viewType == CELL_HEADLINE) {
            val view = inflater.inflate(R.layout.layout_cell_headline_announce, parent, false)
            return AnnounceVH(view)
        }
        val view = inflater.inflate(R.layout.layout_cell_headline_action, parent, false)
        return ActionVH(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isLoading) return

        if (getItemViewType(position) == CELL_HEADLINE) {
            (holder as AnnounceVH).bind(homecard.arrayCards[position].data, listener,position)
        }
        else {
            (holder as ActionVH).bind(homecard.arrayCards[position].data, listener,position,true)
        }
    }

    override fun getItemCount(): Int {
        return homecard.arrayCards.size
    }
}
