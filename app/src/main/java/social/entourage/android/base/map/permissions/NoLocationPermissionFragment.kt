package social.entourage.android.base.map.permissions

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_no_location_permission.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R

class NoLocationPermissionFragment : DialogFragment() {
    private var showingGeolocationSettings = false
    private var enableGeolocation = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_no_location_permission, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                onBackButton()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        no_location_back_button?.setOnClickListener {onBackButton()}
        no_location_activate_button?.setOnClickListener {onActivateButton()}
        no_location_ignore_button?.setOnClickListener {onBackButton()} //optional button
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onResume() {
        super.onResume()
        if (showingGeolocationSettings) {
            onBackButton()
        }
    }

    fun onBackButton() {
        dismiss()
    }

    fun onActivateButton() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_GEOLOCATION_ACTIVATE_04_4A)
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        showingGeolocationSettings = true
        enableGeolocation = true
    }

    companion object {
        const val TAG = "fragment_no_location_permission"
    }
}