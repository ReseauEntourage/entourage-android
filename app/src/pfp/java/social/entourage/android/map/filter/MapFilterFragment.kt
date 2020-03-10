package social.entourage.android.map.filter

import android.widget.Switch
import kotlinx.android.synthetic.pfp.fragment_map_filter.*
import social.entourage.android.R
import social.entourage.android.map.filter.MapFilterFactory.mapFilter

class MapFilterFragment  : BaseMapFilterFragment() {
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override protected fun initializeView() {
        super.initializeView()
        map_filter_entourage_outing_switch.setOnClickListener {onOutingSwitchClicked()}

    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    protected fun onOutingSwitchClicked() {
        if (!map_filter_entourage_outing_switch.isChecked) {
            map_filter_past_events_switch.isChecked = false
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    override fun loadFilter() {
        val mapFilter = mapFilter
        map_filter_entourage_neighborhood_switch.isChecked = mapFilter.entourageTypeNeighborhood
        map_filter_entourage_private_circle_switch.isChecked = mapFilter.entourageTypePrivateCircle
        map_filter_entourage_outing_switch.isChecked = mapFilter.entourageTypeOuting
        map_filter_past_events_switch.isChecked = mapFilter.includePastEvents
    }

    override fun saveFilter() {
        val mapFilter = mapFilter
        mapFilter.entourageTypeNeighborhood = map_filter_entourage_neighborhood_switch.isChecked
        mapFilter.entourageTypePrivateCircle = map_filter_entourage_private_circle_switch.isChecked
        mapFilter.entourageTypeOuting = map_filter_entourage_outing_switch.isChecked
        mapFilter.includePastEvents = map_filter_past_events_switch.isChecked
    }
}