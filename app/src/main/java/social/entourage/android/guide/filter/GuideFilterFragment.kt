package social.entourage.android.guide.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_guide_filter.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnSolidarityGuideFilterChanged
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.guide.poi.PoiRenderer
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.tools.log.EntourageEvents.ACTION_GUIDE_SUBMITFILTERS

/**
 * Guide Filter Fragment
 */
class GuideFilterFragment : EntourageDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var filterAdapter: GuideFilterAdapter? = null
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guide_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFiltersList()
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    fun onValidateClicked() {
        // Save the filter
        filterAdapter?.items?.forEach { filterItem ->
            GuideFilter.instance.setValueForCategoryId(filterItem.categoryType.categoryId, filterItem.isChecked)
        }

        EntourageEvents.logEvent(ACTION_GUIDE_SUBMITFILTERS)
        //Update others filters
        GuideFilter.instance.isPartnersSelected = GuideFilter.instance.isPartnersTmpSelected
        GuideFilter.instance.isDonationsSelected = GuideFilter.instance.isDonationsTmpSelected
        GuideFilter.instance.isVolunteersSelected = GuideFilter.instance.isVolunteersTmpSelected

        GuideFilter.instance.setValueForCategoryId(PoiRenderer.CategoryType.PARTNERS.categoryId, GuideFilter.instance.isPartnersSelected)

        // Apply the filter
        BusProvider.instance.post(OnSolidarityGuideFilterChanged())
        // Dismiss the fragment
        dismiss()
    }

    // ----------------------------------
    // ListView
    // ----------------------------------
    private fun initializeFiltersList() {
        filterAdapter = GuideFilterAdapter()
        guide_filter_list?.adapter = filterAdapter
        title_close_button?.setOnClickListener {  dismiss() }
        bottom_action_button?.setOnClickListener { onValidateClicked() }

        forceResizeListview(guide_filter_list)

        //Setup Others filters
        filter_item_switch_partner?.isChecked = GuideFilter.instance.isPartnersSelected
        filter_item_switch_donate?.isChecked = GuideFilter.instance.isDonationsSelected
        filter_item_switch_volunteer?.isChecked = GuideFilter.instance.isVolunteersSelected

        GuideFilter.instance.isPartnersTmpSelected = GuideFilter.instance.isPartnersSelected
        GuideFilter.instance.isDonationsTmpSelected = GuideFilter.instance.isDonationsSelected
        GuideFilter.instance.isVolunteersTmpSelected = GuideFilter.instance.isVolunteersSelected

        filter_item_switch_partner?.setOnCheckedChangeListener { buttonView, isChecked ->
            GuideFilter.instance.isPartnersTmpSelected = isChecked
            changeTopViews(isChecked)
        }
        filter_item_switch_donate.setOnCheckedChangeListener { buttonView, isChecked ->
            GuideFilter.instance.isDonationsTmpSelected = isChecked
        }

        filter_item_switch_volunteer?.setOnCheckedChangeListener { buttonView, isChecked ->
            GuideFilter.instance.isVolunteersTmpSelected = isChecked
        }

        changeTopViews(GuideFilter.instance.isPartnersSelected)
    }

    private fun changeTopViews(isVisible: Boolean) {
        if (isVisible) {
            filter_layout_donate?.visibility = View.VISIBLE
            filter_layout_volunteer?.visibility = View.VISIBLE
        }
        else {
            GuideFilter.instance.isDonationsTmpSelected = false
            GuideFilter.instance.isVolunteersTmpSelected = false
            filter_item_switch_donate?.isChecked = GuideFilter.instance.isDonationsTmpSelected
            filter_item_switch_volunteer?.isChecked = GuideFilter.instance.isVolunteersTmpSelected

            filter_layout_donate?.visibility = View.GONE
            filter_layout_volunteer?.visibility = View.GONE
        }
    }

    fun forceResizeListview(myListView: ListView) {
        val listAdapter: ListAdapter = myListView.adapter ?: return

        var totalHeight = 0
        for (size in 0 until listAdapter.count) {
            val listItem: View = listAdapter.getView(size, null, myListView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        
        val params: ViewGroup.LayoutParams = myListView.layoutParams
        params.height = totalHeight + myListView.dividerHeight * (listAdapter.count - 1)
        myListView.layoutParams = params
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG = GuideFilterFragment::class.java.simpleName
    }
}