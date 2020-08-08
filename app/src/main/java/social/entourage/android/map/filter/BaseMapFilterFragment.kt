package social.entourage.android.map.filter

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.api.tape.Events.OnMapFilterChanged
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.BusProvider

abstract class BaseMapFilterFragment : EntourageDialogFragment() {

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
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
        BusProvider.instance.post(OnMapFilterChanged())
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