package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_onboarding_email_pwd.*
import social.entourage.android.R
import social.entourage.android.tools.Logger
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.isValidEmail

private const val ARG_EMAIL = "email"

class OnboardingEmailPwdFragment : Fragment() {
    private var tempEmail: String? = null

    private var callback:OnboardingCallback? = null
    private var isAllreadyCall = false

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tempEmail = it.getString(ARG_EMAIL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_email_pwd, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (tempEmail == null || !tempEmail!!.isValidEmail()) {
            callback?.updateButtonNext(false)
        }
        else {
            ui_onboard_email_pwd_et_mail?.setText(tempEmail)
            callback?.updateButtonNext(true)
        }

        setupViews()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    //**********//**********//**********
    // Methods UI
    //**********//**********//**********

    fun setupViews() {
        onboard_email_pwd_mainlayout?.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            view.performClick()
            true
        }

        ui_onboard_email_pwd_et_mail?.setOnEditorActionListener { _, event, _ ->
            Logger("ON key listener $event -- ${EditorInfo.IME_ACTION_DONE}")
            if (event == EditorInfo.IME_ACTION_DONE) {
                Logger("Call from Editor Action")
                isAllreadyCall = true
                updateButtonNext()
            }
            false
        }

        ui_onboard_email_pwd_et_mail?.setOnFocusChangeListener { _, b ->
            Logger("Call from Focus change")
            if (!b && !isAllreadyCall) updateButtonNext()
            if (b) isAllreadyCall = false
        }
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    private fun updateButtonNext() {
        if (ui_onboard_email_pwd_et_mail?.text!= null && ui_onboard_email_pwd_et_mail.text.toString().isValidEmail()) {
            callback?.updateButtonNext(true)
            callback?.updateEmailPwd(ui_onboard_email_pwd_et_mail.text.toString(),null,null)
        }
        else {
            callback?.updateButtonNext(false)
            callback?.updateEmailPwd(null,null,null)
        }
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        @JvmStatic
        fun newInstance(email: String?) =
                OnboardingEmailPwdFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_EMAIL, email)
                    }
                }
    }
}
