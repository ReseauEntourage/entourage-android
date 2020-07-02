package social.entourage.android.authentication.login.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_register_smscode.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.base.EntourageDialogFragment

class RegisterSMSCodeFragment : EntourageDialogFragment() {
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
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_30_3)
        return inflater.inflate(R.layout.fragment_register_smscode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register_smscode_back_button?.setOnClickListener { onBackClicked() }
        register_smscode_validate_button?.setOnClickListener { onValidateClicked() }
        register_smscode_lost_code?.setOnClickListener { onLostCodeClicked() }
        register_smscode_description?.setOnClickListener { onResendByEmailViewClicked() }
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

    fun onValidateClicked() {
        if (!checkValidLocalSMSCode()) {
            Toast.makeText(activity, R.string.registration_smscode_error_code, Toast.LENGTH_SHORT).show()
        } else {
            register_smscode_code?.let { mListener?.registerCheckCode(it.text.toString()) }
        }
    }

    fun onLostCodeClicked() {
        // Resend the code
        mListener?.registerResendCode()
    }

    fun onResendByEmailViewClicked() {
        register_smscode_email?.visibility = View.VISIBLE
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private fun checkValidLocalSMSCode(): Boolean {
        return register_smscode_code?.text?.isNotBlank() ?: false
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.RegisterSMSCode"
    }
}