package social.entourage.android.guide.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_guide_filter.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnSolidarityGuideFilterChanged
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.BusProvider

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
        title_action_button?.setOnClickListener { onValidateClicked() }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        @JvmField
        val TAG = GuideFilterFragment::class.java.simpleName
    }
}