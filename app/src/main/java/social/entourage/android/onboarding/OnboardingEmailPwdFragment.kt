package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_email_pwd.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import social.entourage.android.R
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.isValidEmail
import social.entourage.android.tools.log.AnalyticsEvents

private const val ARG_EMAIL = "email"

class OnboardingEmailPwdFragment : Fragment() {
    private var tempEmail: String? = null

    private var callback:OnboardingCallback? = null

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

        if (tempEmail.isValidEmail()) {
            ui_onboard_email_pwd_et_mail?.setText(tempEmail)
            callback?.updateButtonNext(true)
        } else {
            callback?.updateButtonNext(false)
        }

        setupViews()

        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_INPUT_EMAIL)
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
            if (event == EditorInfo.IME_ACTION_DONE) {
                updateButtonNext(true)
            }
            false
        }

        //Listen to keyboard visibility
        activity?.let {
            KeyboardVisibilityEvent.setEventListener(it) { isOpen ->
                if (isOpen)
                    showErrorMessage(false)
                else
                    updateButtonNext(false)
            }
        }
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    private fun updateButtonNext(isFromEmail:Boolean) {
        if (ui_onboard_email_pwd_et_mail?.text.toString().isValidEmail()) {
            showErrorMessage(false)
            callback?.updateButtonNext(true)
            callback?.updateEmailPwd(ui_onboard_email_pwd_et_mail.text.toString(),null,null)

            if (isFromEmail) {
                callback?.goNextManually()
            }
        }
        else {
            showErrorMessage(true)
            callback?.updateButtonNext(false)
            callback?.updateEmailPwd(null,null,null)
        }
    }

    private fun showErrorMessage(show: Boolean) {
        error_message_tv?.visibility = if (show) View.VISIBLE else View.GONE
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        fun newInstance(email: String?) =
                OnboardingEmailPwdFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_EMAIL, email)
                    }
                }
    }
}
