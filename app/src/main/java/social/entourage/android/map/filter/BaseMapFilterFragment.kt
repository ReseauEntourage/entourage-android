package social.entourage.android.map.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnMapFilterChanged
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.BusProvider

abstract class BaseMapFilterFragment : EntourageDialogFragment() {

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    fun onCloseClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_CLOSE)
        dismiss()
    }

    fun onValidateClicked() {
        // save the values to the filter
        saveFilter()

        // inform the map screen to refresh the newsfeed
        BusProvider.getInstance().post(OnMapFilterChanged())
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_SUBMIT)

        // dismiss the dialog
        dismiss()
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    protected open fun initializeView() {
        title_close_button?.setOnClickListener {onCloseClicked()}
        title_action_button?.setOnClickListener {onValidateClicked()}
        loadFilter()
    }

    protected abstract fun loadFilter()
    protected abstract fun saveFilter()
}