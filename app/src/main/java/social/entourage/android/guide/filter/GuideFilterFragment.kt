package social.entourage.android.guide.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.databinding.FragmentGuideFilterBinding
import social.entourage.android.guide.GuideMapFragment
import social.entourage.android.guide.poi.PoiRenderer
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.log.AnalyticsEvents.ACTION_GUIDE_SUBMITFILTERS

/**
 * Guide Filter Fragment
 */
class GuideFilterFragment : BaseDialogFragment() {
    private var _binding: FragmentGuideFilterBinding? = null
    val binding: FragmentGuideFilterBinding get() = _binding!!    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var items = ArrayList<GuideFilterItemAdapter.GuideFilterItem>()
    private var isPartnersTmpSelected = false
    private var isDonationsTmpSelected = false
    private var isVolunteersTmpSelected = false

    private var adapterRV:GuideFilterRVAdapter? = null
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentGuideFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFiltersList()
        binding.entourageTitleView.binding.titleCloseButton.setOnClickListener {  dismiss() }
        binding.bottomActionButton.setOnClickListener { onValidateClicked() }

        binding.uiBtCancel.setOnClickListener {
            setAllFiltersOn()
            initializeFiltersList()
        }
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    private fun onValidateClicked() {
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
        binding.uiRecyclerView.setHasFixedSize(true)
        binding.uiRecyclerView.layoutManager = linearLayoutManager

        adapterRV = GuideFilterRVAdapter(requireContext(),items,
                isPartnersTmpSelected,isDonationsTmpSelected,
                isVolunteersTmpSelected, isDefaultFilters(), { position ->

            val item = items[position]

            if (item.isChecked && !isDefaultFilters()) {
                setAllFiltersOn()
            }
            else {
                setAllFiltersOff(position)
            }

            adapterRV?.updateData(items,isPartnersTmpSelected,isDonationsTmpSelected,isVolunteersTmpSelected,isDefaultFilters())
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
            adapterRV?.updateData(items,isPartnersTmpSelected,isDonationsTmpSelected,isVolunteersTmpSelected,isDefaultFilters())
            updateButtonText()
        })
        binding.uiRecyclerView.adapter = adapterRV
    }

    private fun updateButtonText() {
        binding.bottomActionButton.text = if (isDefaultFilters()) {
            "Voir tout"
        }
        else {
            getString(R.string.validate)
        }
    }

    /*****
     * Methods for filters
     */
    private fun isDefaultFilters(): Boolean {

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

    private fun setAllFiltersOn() {
        for (item in items) {
            item.isChecked = true
        }
        isPartnersTmpSelected = true
        isDonationsTmpSelected = false
        isVolunteersTmpSelected = false
    }

    private fun setAllFiltersOff(position:Int) {
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

    private fun setupFilters() {
        items = ArrayList()
        for (i in 1 until PoiRenderer.CategoryType.entries.size) {
            val categoryType = PoiRenderer.CategoryType.entries[i]
            if (categoryType != PoiRenderer.CategoryType.PARTNERS) {
                items.add(GuideFilterItemAdapter.GuideFilterItem(categoryType, GuideFilter.instance.valueForCategoryId(categoryType.categoryId)))
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