package social.entourage.android.home.actions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.home.*
import social.entourage.android.home.expert.VariantCellType

/**
 * ActionEventAdapter.
 */
class ActionEventAdapter(var homecard:HomeCard,val listener:HomeViewHolderListener, val isLoading:Boolean,val variantType: VariantCellType): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val minimumItemsToShowMore = 2
    private val minimalCellForSpecialCell = 1
    val TYPE_MORE = 0
    val TYPE_CELL = 1
    val TYPE_OTHER = 2
    val CELL_EMPTY = 3
    var isSpecialCell = false

    override fun getItemViewType(position: Int): Int {
        if (isLoading) return CELL_EMPTY

        if (isSpecialCell && position == homecard.arrayCards.size) {
            return TYPE_OTHER
        }

        if (position == homecard.arrayCards.size) return TYPE_MORE
        return TYPE_CELL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        var cellEventId = R.layout.layout_cell_event_original
        var cellActionId = R.layout.layout_cell_action_original
        var cellEvent_zoneId = R.layout.layout_cell_event_zone_original
        var cell_action_empty = R.layout.layout_cell_action_empty_original
        var cell_event_empty = R.layout.layout_cell_event_empty_original

        if (variantType == VariantCellType.Original) {
            cellEventId = R.layout.layout_cell_event_original
            cellEvent_zoneId = R.layout.layout_cell_event_zone_original
            cell_event_empty = R.layout.layout_cell_event_empty_original

            cellActionId = R.layout.layout_cell_action_original
            cell_action_empty = R.layout.layout_cell_action_empty_original
        }
        else if (variantType == VariantCellType.VariantA) {
            cellEventId = R.layout.layout_cell_event_variant_a
            cellEvent_zoneId = R.layout.layout_cell_event_zone_variant_a
            cell_event_empty = R.layout.layout_cell_event_empty_variant_a

            cellActionId = R.layout.layout_cell_action_variant_a
            cell_action_empty = R.layout.layout_cell_action_empty_variant_a
        }
        else if (variantType == VariantCellType.VariantB) {
            cellEventId = R.layout.layout_cell_event_variant_b
            cellEvent_zoneId = R.layout.layout_cell_event_zone_variant_b
            cell_event_empty = R.layout.layout_cell_event_empty_variant_b

            cellActionId = R.layout.layout_cell_action_variant_b
            cell_action_empty = R.layout.layout_cell_action_empty_variant_b
        }

        if (isLoading) {
            if (homecard.type == HomeCardType.ACTIONS) {
                val view = inflater.inflate(cell_action_empty, parent, false)

                return ActionVH(view)
            }
            else {
                val view = inflater.inflate(cell_event_empty, parent, false)
                return EventVH(view)
            }
        }

        if (viewType == TYPE_CELL) {
            if (homecard.type == HomeCardType.ACTIONS) {
                val view = inflater.inflate(cellActionId, parent, false)

                return ActionVH(view)
            }
            else {

                val view = inflater.inflate(cellEventId, parent, false)
                return EventVH(view)
            }
        }
        else if (viewType == TYPE_OTHER) {
            val view:View
            if (homecard.type == HomeCardType.ACTIONS) {
                if(homecard.subtype == HomeCardType.ACTIONS_ASK) {
                    view = inflater.inflate(R.layout.layout_cell_action_demand, parent, false)
                }
                else {
                    view = inflater.inflate(R.layout.layout_cell_action_help, parent, false)
                }

            }
            else {

                view = inflater.inflate(cellEvent_zoneId, parent, false)
            }

            return OtherVH(view,homecard.type,homecard.subtype)
        }
        val view = inflater.inflate(R.layout.layout_cell_action_more, parent, false)

        return ShowMoreVH(view,homecard.type,homecard.subtype)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isLoading) return

        if ( isSpecialCell && position == homecard.arrayCards.size) {
            (holder as OtherVH).bind(listener)
            return
        }
        if (position == homecard.arrayCards.size) {
            (holder as ShowMoreVH).bind(listener)
            return
        }
        if (homecard.type == HomeCardType.ACTIONS) {
            var isVariant = false
            if (variantType == VariantCellType.VariantA || variantType == VariantCellType.VariantB) isVariant = true
            (holder as ActionVH).bind(homecard.arrayCards[position].data, listener,position,false,isVariant)
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
