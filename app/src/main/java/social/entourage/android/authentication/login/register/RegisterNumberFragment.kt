package social.entourage.android.authentication.login.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_register_number.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.Utils.checkPhoneNumberFormat

class RegisterNumberFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var mListener: OnRegisterUserListener? = null
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_2)
        return inflater.inflate(R.layout.fragment_register_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register_number_back_button?.setOnClickListener { onBackClicked() }
        register_number_next_button?.setOnClickListener { onNextClicked() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnRegisterUserListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    // ----------------------------------
    // Click handlers
    // ----------------------------------
    fun onBackClicked() {
        dismiss()
    }

    fun onNextClicked() {
        // Check the phone
        register_number_ccp?.let {
            register_number_phone_number?.let { it2 ->
                checkPhoneNumberFormat(it.selectedCountryCodeWithPlus, it2.text.toString())?.let { phoneNumber ->
                    register_number_next_button?.isEnabled = false
                    // Save the phone
                    mListener?.registerSavePhoneNumber(phoneNumber)
                } ?: run {
                    Toast.makeText(activity, R.string.login_text_invalid_format, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun savedPhoneNumber(success: Boolean) {
        register_number_next_button?.isEnabled = true
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.RegisterNumber"
    }
}