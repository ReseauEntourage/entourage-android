package social.entourage.android.map.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.pfp.fragment_map_filter.*
import social.entourage.android.R
import social.entourage.android.map.filter.MapFilterFactory

class MapFilterFragment  : BaseMapFilterFragment() {
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_filter, container, false)
    }

    override fun initializeView() {
        super.initializeView()
        map_filter_entourage_outing_switch?.setOnClickListener {onOutingSwitchClicked()}

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
        map_filter_entourage_neighborhood_switch?.isChecked = mapFilter.entourageTypeNeighborhood
        map_filter_entourage_private_circle_switch?.isChecked = mapFilter.entourageTypePrivateCircle
        map_filter_entourage_outing_switch?.isChecked = mapFilter.entourageTypeOuting
        map_filter_past_events_switch?.isChecked = false//mapFilter.includePastEvents
    }

    override fun saveFilter() {
        val mapFilter = mapFilter
        mapFilter.entourageTypeNeighborhood = map_filter_entourage_neighborhood_switch.isChecked
        mapFilter.entourageTypePrivateCircle = map_filter_entourage_private_circle_switch.isChecked
        mapFilter.entourageTypeOuting = map_filter_entourage_outing_switch.isChecked
        mapFilter.includePastEvents = false//map_filter_past_events_switch.isChecked
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage.android.MapFilterFragment"
    }
}