package social.entourage.android.guide.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_guide_filter.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.guide.GuideMapFragment
import social.entourage.android.guide.poi.PoiRenderer
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.log.AnalyticsEvents.ACTION_GUIDE_SUBMITFILTERS

/**
 * Guide Filter Fragment
 */
class GuideFilterFragment : BaseDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var items = ArrayList<GuideFilterAdapter.GuideFilterItem>()
    private var isPartnersTmpSelected = false
    private var isDonationsTmpSelected = false
    private var isVolunteersTmpSelected = false

    private var adapterRV:FilterGuideRVAdapter? = null
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
        title_close_button?.setOnClickListener {  dismiss() }
        bottom_action_button?.setOnClickListener { onValidateClicked() }

        ui_bt_cancel?.setOnClickListener {
            setAllFiltersOn()
            initializeFiltersList()
        }
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    fun onValidateClicked() {
        // Save the filter
        items.forEach { filterItem ->
            GuideFilter.instance.setValueForCategoryId(filterItem.categoryType.categoryId, filterItem.isChecked)
        }

        AnalyticsEvents.logEvent(ACTION_GUIDE_SUBMITFILTERS)
        //Update others filters
        GuideFilter.instance.isPartnersSelected = isPartnersTmpSelected
        GuideFilter.instance.isDonationsSelected = isDonationsTmpSelected
        GuideFilter.instance.isVolunteersSelected = isVolunteersTmpSelected

        GuideFilter.instance.setValueForCategoryId(PoiRenderer.CategoryType.PARTNERS.categoryId, GuideFilter.instance.isPartnersSelected)

        // Apply the filter
        (parentFragmentManager.findFragmentByTag(GuideMapFragment.TAG) as? GuideMapFragment)?.onSolidarityGuideFilterChanged()
        // Dismiss the fragment
        dismiss()
    }

    // ----------------------------------
    // RecyclerView
    // ----------------------------------
    private fun initializeFiltersList() {

        val guideFilter = GuideFilter.instance

        setupFilters()

        isPartnersTmpSelected = guideFilter.isPartnersSelected
        isDonationsTmpSelected = guideFilter.isDonationsSelected
        isVolunteersTmpSelected = guideFilter.isVolunteersSelected

        updateButtonText()

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        ui_recyclerView?.setHasFixedSize(true)
        ui_recyclerView?.layoutManager = linearLayoutManager

        adapterRV = FilterGuideRVAdapter(requireContext(),items,
                isPartnersTmpSelected,isDonationsTmpSelected,
                isVolunteersTmpSelected, isDefaultFilters(), { position ->

            val item = items[position]

            if (item.isChecked && !isDefaultFilters()) {
                setAllFiltersOn()
            }
            else {
                setAllFiltersOff(position)
            }

            adapterRV?.updateDatas(items,isPartnersTmpSelected,isDonationsTmpSelected,isVolunteersTmpSelected,isDefaultFilters())
            updateButtonText()
        } ,{ positionTop ->
            when(positionTop) {
                0 -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SEARCHFILTER_ORGANIZ)
                    if (isPartnersTmpSelected && !isDefaultFilters()) {
                        setAllFiltersOn()
                    }
                    else {
                        setAllFiltersOff(-1)
                    }
                }
                1 -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SEARCHFILTER_DONAT)
                    if (isDonationsTmpSelected && !isDefaultFilters()) {
                        setAllFiltersOn()
                    }
                    else {
                        setAllFiltersOff(-2)
                    }
                }
                2 -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SEARCHFILTER_VOLUNT)
                    if (isVolunteersTmpSelected && !isDefaultFilters()) {
                        setAllFiltersOn()
                    }
                    else {
                        setAllFiltersOff(-3)
                    }
                }
            }
            adapterRV?.updateDatas(items,isPartnersTmpSelected,isDonationsTmpSelected,isVolunteersTmpSelected,isDefaultFilters())
            updateButtonText()
        })
        ui_recyclerView?.adapter = adapterRV
    }

    fun updateButtonText() {
        if (isDefaultFilters()) {
            bottom_action_button?.text = "Voir tout"
        }
        else {
            bottom_action_button?.text = getString(R.string.validate)
        }
    }

    /*****
     * Methods for filters
     */
    fun isDefaultFilters(): Boolean {

        var isDefault = true

        for (item in items) {
            if (!item.isChecked) {
                isDefault = false
                break
            }
        }

        if (!isPartnersTmpSelected || isVolunteersTmpSelected || isDonationsTmpSelected) {
            isDefault = false
        }
        return isDefault
    }

    fun setAllFiltersOn() {
        for (item in items) {
            item.isChecked = true
        }
        isPartnersTmpSelected = true
        isDonationsTmpSelected = false
        isVolunteersTmpSelected = false
    }

    fun setAllFiltersOff(position:Int) {
        for (item in items) {
            item.isChecked = false
        }

        if (position >= 0) {
            items[position].isChecked = true
            isPartnersTmpSelected = false
            isDonationsTmpSelected = false
            isVolunteersTmpSelected = false
        }
        else {
            when(position) {
                -1 -> {
                    isPartnersTmpSelected = true
                }
                -2 -> {
                    isPartnersTmpSelected = true
                    isDonationsTmpSelected = true
                    isVolunteersTmpSelected = false
                }
                -3 -> {
                    isPartnersTmpSelected = true
                    isDonationsTmpSelected = false
                    isVolunteersTmpSelected = true
                }
            }
        }
    }

    fun setupFilters() {
        items = ArrayList()
        for (i in 1 until PoiRenderer.CategoryType.values().size) {
            val categoryType = PoiRenderer.CategoryType.values()[i]
            if (categoryType != PoiRenderer.CategoryType.PARTNERS) {
                items.add(GuideFilterAdapter.GuideFilterItem(categoryType, GuideFilter.instance.valueForCategoryId(categoryType.categoryId)))
            }
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG: String? = GuideFilterFragment::class.java.simpleName
    }
}