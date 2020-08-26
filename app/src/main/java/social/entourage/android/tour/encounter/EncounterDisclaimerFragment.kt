package social.entourage.android.tour.encounter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_encounter_disclaimer.*
import social.entourage.android.R
import social.entourage.android.tools.view.EntourageSnackbar

class EncounterDisclaimerFragment : DialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var mListener: OnFragmentInteractionListener? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_encounter_disclaimer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        encounter_disclaimer_close_button?.setOnClickListener {onCloseClicked()}
        encounter_disclaimer_ok_button?.setOnClickListener {onOkClicked()}
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is OnFragmentInteractionListener){
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
        mListener = context
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.CustomDialogFragmentSlide
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------
    fun onCloseClicked() {
        dismiss()
    }

    private fun onOkClicked() {
        if (encounter_disclaimer_checkbox?.isChecked == true) {
            //inform the listener that the user accepted the CGU
            mListener?.onEncounterDisclaimerAccepted(this)
        } else {
            view?.let {EntourageSnackbar.make(it, R.string.encounter_disclaimer_error_notaccepted, Snackbar.LENGTH_SHORT).show()}
        }
    }

    // ----------------------------------
    // Listener
    // ----------------------------------
    interface OnFragmentInteractionListener {
        fun onEncounterDisclaimerAccepted(fragment: EncounterDisclaimerFragment)
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage.android.entourage.prodisclaimer"
    }
}