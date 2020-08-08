package social.entourage.android.tour

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_map_tour_filter.*
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.map.filter.BaseMapFilterFragment

class TourFilterFragment  : BaseMapFilterFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_tour_filter, container, false)
    }

    override fun initializeView() {
        super.initializeView()
        map_filter_tour_all_switch?.setOnClickListener { onAllToursSwitch() }
        map_filter_tour_medical_switch?.setOnClickListener { onMedicalSwitch() }
        map_filter_tour_social_switch?.setOnClickListener { onSocialSwitch() }
        map_filter_tour_distributive_switch?.setOnClickListener { onDistributiveSwitch() }
        map_filter_time_days_1?.setOnClickListener { onDays1Click() }
        map_filter_time_days_2?.setOnClickListener { onDays2Click() }
        map_filter_time_days_3?.setOnClickListener { onDays3Click() }
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    private fun onAllToursSwitch() {
        val checked = map_filter_tour_all_switch?.isChecked ?:true
        map_filter_tour_medical_switch?.isChecked = checked
        map_filter_tour_social_switch?.isChecked = checked
        map_filter_tour_distributive_switch?.isChecked = checked
        map_filter_tour_type_details_layout?.visibility = if (checked) View.VISIBLE else View.GONE
    }

    private fun onMedicalSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_MEDICAL_TOURS)
        map_filter_tour_all_switch?.isChecked = !allToursDisabled()
        map_filter_tour_type_details_layout?.visibility = if (allToursDisabled()) View.GONE else View.VISIBLE
    }

    private fun onSocialSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_SOCIAL_TOURS)
        map_filter_tour_all_switch?.isChecked = !allToursDisabled()
        map_filter_tour_type_details_layout?.visibility = if (allToursDisabled()) View.GONE else View.VISIBLE
    }

    private fun onDistributiveSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_DISTRIBUTION_TOURS)
        map_filter_tour_all_switch?.isChecked = !allToursDisabled()
        map_filter_tour_type_details_layout?.visibility = if (allToursDisabled()) View.GONE else View.VISIBLE
    }

    //TODO find another way to have both constraintlayout and radiogroup
    private fun onDays1Click() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_FILTER1)
        //map_filter_time_days_1.isSelected = false
        map_filter_time_days_2?.isChecked = false
        map_filter_time_days_3?.isChecked = false
    }

    private fun onDays2Click() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_FILTER2)
        map_filter_time_days_1?.isChecked = false
        //map_filter_time_days_2.isChecked = false
        map_filter_time_days_3?.isChecked = false
    }

    private fun onDays3Click() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_FILTER3)
        map_filter_time_days_1?.isChecked = false
        map_filter_time_days_2?.isChecked = false
        //map_filter_time_days_3.isChecked = false
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    override fun loadFilter() {
        map_filter_tour_type_layout?.visibility = View.VISIBLE
        map_filter_tour_medical_switch?.isChecked = TourFilter.tourTypeMedical
        map_filter_tour_social_switch?.isChecked = TourFilter.tourTypeSocial
        map_filter_tour_distributive_switch?.isChecked = TourFilter.tourTypeDistributive
        map_filter_tour_all_switch?.isChecked = !allToursDisabled()
        map_filter_tour_type_details_layout?.visibility = if (allToursDisabled()) View.GONE else View.VISIBLE
        when (TourFilter.getTimeFrame()) {
            TourFilter.DAYS_1 -> map_filter_time_days_1?.isChecked = true
            TourFilter.DAYS_3 -> map_filter_time_days_3?.isChecked = true
            TourFilter.DAYS_2 -> map_filter_time_days_2?.isChecked = true
            else -> map_filter_time_days_2?.isChecked = true
        }
    }

    override fun saveFilter() {
        TourFilter.tourTypeMedical = map_filter_tour_medical_switch?.isChecked ?: true
        TourFilter.tourTypeSocial = map_filter_tour_social_switch?.isChecked ?: true
        TourFilter.tourTypeDistributive = map_filter_tour_distributive_switch?.isChecked ?: true
        TourFilter.timeframe = when {
            map_filter_time_days_1?.isChecked ?: false -> TourFilter.DAYS_1
            map_filter_time_days_2?.isChecked ?: false -> TourFilter.DAYS_2
            map_filter_time_days_3?.isChecked ?: false -> TourFilter.DAYS_3
            else -> TourFilter.DAYS_3
            }
    }

    private fun allToursDisabled(): Boolean {
        return !(map_filter_tour_medical_switch?.isChecked==true || map_filter_tour_social_switch?.isChecked==true || map_filter_tour_distributive_switch?.isChecked==true)
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage_android.MapTourFilterFragment"
    }
}