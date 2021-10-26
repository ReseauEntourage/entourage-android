package social.entourage.android.base.map.filter

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_map_filter.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.tools.log.AnalyticsEvents
import java.util.*

class MapFilterFragment  : BaseMapFilterFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var actionSwitches = HashMap<String, List<SwitchCompat>>()
    private val onCheckedChangeListener = OnCheckedChangeListener()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_filter, container, false)
    }

    override fun initializeView() {
        super.initializeView()
        map_filter_entourage_outing_switch?.setOnClickListener { onOutingSwitch() }
        ui_layout_events?.visibility = View.GONE

        map_filter_entourage_demand_switch?.setOnClickListener { onDemandSwitch() }
        map_filter_entourage_contribution_switch?.setOnClickListener { onContributionSwitch() }
        map_filter_time_days_1?.setOnClickListener { onDays1Click() }
        map_filter_time_days_2?.setOnClickListener { onDays2Click() }
        map_filter_time_days_3?.setOnClickListener { onDays3Click() }

        bottom_action_button?.setOnClickListener { onValidateClicked() }

        ui_map_filter_entourage_alls_switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            onAllSwitch()
        }
        ui_map_filter_entourage_partnersOnly_switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            onPartnersOnlySwitch()
        }
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    private fun onOutingSwitch() {
        if (map_filter_entourage_outing_switch!=null && !map_filter_entourage_outing_switch.isChecked) {
            map_filter_past_events_switch.isChecked = false
        }
    }

    private fun onDemandSwitch() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_FILTER_ONLY_ASK)
        val checked = map_filter_entourage_demand_switch?.isChecked ?: false
        map_filter_entourage_demand_details_layout?.visibility = if (checked) View.VISIBLE else View.GONE
        actionSwitches[BaseEntourage.GROUPTYPE_ACTION_DEMAND]?.forEach { categorySwitch -> categorySwitch.isChecked = checked}
    }

    private fun onContributionSwitch() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_FILTER_ONLY_OFFERS)
        val checked = map_filter_entourage_contribution_switch?.isChecked ?: false
        map_filter_entourage_contribution_details_layout?.visibility = if (checked) View.VISIBLE else View.GONE
        actionSwitches[BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION]?.forEach { categorySwitch -> categorySwitch.isChecked = checked}
    }

    //TODO find another way to have both constraintlayout and radiogroup
    private fun onDays1Click() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_FILTER_FILTER1)
        //map_filter_time_days_1.isSelected = false
        map_filter_time_days_2?.isChecked = false
        map_filter_time_days_3?.isChecked = false
    }

    private fun onDays2Click() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_FILTER_FILTER2)
        map_filter_time_days_1?.isChecked = false
        //map_filter_time_days_2.isChecked = false
        map_filter_time_days_3?.isChecked = false
    }

    private fun onDays3Click() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_FILTER_FILTER3)
        map_filter_time_days_1?.isChecked = false
        map_filter_time_days_2?.isChecked = false
        //map_filter_time_days_3.isChecked = false
    }

    //Switch for Alls / partnersOnly
    private fun onAllSwitch() {
        val isChecked = ui_map_filter_entourage_alls_switch.isChecked
        ui_map_filter_entourage_partnersOnly_switch.isChecked = !isChecked

        ui_tv_map_filter_entourage_alls?.typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        ui_tv_map_filter_entourage_partners_only?.typeface = if (!isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun onPartnersOnlySwitch() {
        val isChecked = ui_map_filter_entourage_partnersOnly_switch.isChecked
        ui_map_filter_entourage_alls_switch.isChecked = !isChecked

        ui_tv_map_filter_entourage_alls?.typeface = if (!isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        ui_tv_map_filter_entourage_partners_only?.typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    override fun loadFilter() {
        val mapFilter = MapFilterFactory.mapFilter

        //map_filter_entourage_outing_switch?.isChecked = mapFilter.entourageTypeOuting //Old version
        map_filter_entourage_outing_switch?.isChecked = false

        map_filter_past_events_switch?.isChecked = mapFilter.showPastEvents
        map_filter_entourage_demand_switch?.isChecked = mapFilter.entourageTypeDemand
        map_filter_entourage_demand_details_layout?.visibility = if (mapFilter.entourageTypeDemand) View.VISIBLE else View.GONE
        addEntourageCategories(BaseEntourage.GROUPTYPE_ACTION_DEMAND, map_filter_entourage_demand_details_layout, mapFilter)
        map_filter_entourage_contribution_switch?.isChecked = mapFilter.entourageTypeContribution
        map_filter_entourage_contribution_details_layout?.visibility = if (mapFilter.entourageTypeContribution) View.VISIBLE else View.GONE
        addEntourageCategories(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION, map_filter_entourage_contribution_details_layout, mapFilter)
        when (mapFilter.getTimeFrame()) {
            MapFilter.DAYS_1 -> map_filter_time_days_1?.isChecked = true
            MapFilter.DAYS_3 -> map_filter_time_days_3?.isChecked = true
            MapFilter.DAYS_2 -> map_filter_time_days_2?.isChecked = true
            else -> map_filter_time_days_2?.isChecked = true
        }

        // check filters All / partners
        ui_map_filter_entourage_alls_switch?.isChecked = mapFilter.isShowAlls
        ui_tv_map_filter_entourage_alls?.typeface = if (mapFilter.isShowAlls) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        ui_map_filter_entourage_partnersOnly_switch?.isChecked = mapFilter.isShowPartnersOnly
        ui_tv_map_filter_entourage_partners_only?.typeface = if (mapFilter.isShowPartnersOnly) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    override fun saveFilter() {
        val mapFilter = MapFilterFactory.mapFilter
       // mapFilter.entourageTypeOuting = map_filter_entourage_outing_switch?.isChecked ?: true -- Old version
        mapFilter.entourageTypeOuting = false

        mapFilter.showPastEvents = map_filter_past_events_switch?.isChecked ?: false
        mapFilter.entourageTypeDemand = map_filter_entourage_demand_switch?.isChecked ?: true
        mapFilter.entourageTypeContribution = map_filter_entourage_contribution_switch?.isChecked ?: true
        mapFilter.isShowAlls = ui_map_filter_entourage_alls_switch?.isChecked ?: true
        mapFilter.isShowPartnersOnly = ui_map_filter_entourage_partnersOnly_switch?.isChecked ?: false
        for (switchList in actionSwitches.values) {
            for (categorySwitch in switchList) {
                if (categorySwitch.tag != null) {
                    mapFilter.setCategoryChecked(categorySwitch.tag as String, categorySwitch.isChecked)
                }
            }
        }
        mapFilter.timeframe = when {
            map_filter_time_days_1?.isChecked ?:false -> MapFilter.DAYS_1
            map_filter_time_days_2?.isChecked ?:false ->  MapFilter.DAYS_2
            map_filter_time_days_3?.isChecked ?:false -> MapFilter.DAYS_3
            else -> MapFilter.DAYS_3
            }
    }

    private fun addEntourageCategories(groupType: String, layout: LinearLayout?, mapFilter: MapFilter) {
        if(layout==null) return
        // create the hashmap entrance
        val switchList: MutableList<SwitchCompat> = ArrayList()
        actionSwitches[groupType] = switchList
        // get the list of categories
        val entourageCategoryList = EntourageCategoryManager.getEntourageCategoriesForGroup(groupType)
        for (entourageCategory in entourageCategoryList) {
            // inflate and add the view to the layout
            val view = layoutInflater.inflate(R.layout.layout_filter_item_map, layout, false)
            view.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            layout.addView(view)

            // populate the view
            val mFilterName = view.findViewById<TextView>(R.id.filter_item_text)
            val mFilterImage = view.findViewById<ImageView>(R.id.filter_item_image)
            val mFilterSwitch = view.findViewById<SwitchCompat>(R.id.filter_item_switch)
            mFilterName.text = entourageCategory.title
            mFilterImage.setImageResource(entourageCategory.iconRes)
            mFilterImage.clearColorFilter()
            if (context != null) {
                mFilterImage.setColorFilter(ContextCompat.getColor(requireContext(), entourageCategory.typeColorRes), PorterDuff.Mode.SRC_IN)
            }
            mFilterSwitch.isChecked = mapFilter.isCategoryChecked(entourageCategory)
            mFilterSwitch.tag = entourageCategory.key
            mFilterSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
            switchList.add(mFilterSwitch)
        }
    }

    private inner class OnCheckedChangeListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
            // if no tag, exit
            if (compoundButton.tag == null) {
                return
            }
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_FILTER_ACTION_CATEGORY)
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage_android.MapFilterFragment"
    }
}