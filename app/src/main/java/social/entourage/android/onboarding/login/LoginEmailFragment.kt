package social.entourage.android.onboarding.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_onboarding_email_pwd.*
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.isValidEmail

class LoginEmailFragment : Fragment() {
    private var callback:LoginNextCallback? = null
    private var isAllreadyCall = false

    //******************************
    // Lifecycle
    //******************************

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_email_pwd, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateButtonNext(false)

        ui_onboard_email_tv_title?.text = getString(R.string.login_email_title)
        ui_onboard_email_tv_description?.text = getString(R.string.login_email_description)

        setupViews()

        EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_LOGIN_INPUT_EMAIL)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? LoginNextCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    //******************************
    // Methods UI
    //******************************

    fun setupViews() {
        onboard_email_pwd_mainlayout?.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            view.performClick()
            true
        }

        ui_onboard_email_pwd_et_mail?.setOnEditorActionListener { _, event, _ ->
            if (event == EditorInfo.IME_ACTION_DONE) {
                isAllreadyCall = true
                updateButtonNext()
            }
            false
        }

        ui_onboard_email_pwd_et_mail?.setOnFocusChangeListener { _, b ->
            if (!b && !isAllreadyCall) updateButtonNext()
            if (b) isAllreadyCall = false
        }
    }

    //******************************
    // Methods
    //******************************

    private fun updateButtonNext() {
        if (ui_onboard_email_pwd_et_mail?.text!= null && ui_onboard_email_pwd_et_mail.text.toString().isValidEmail()) {
            callback?.updateButtonNext(true)
            callback?.updateEmailPwd(ui_onboard_email_pwd_et_mail.text.toString())
        }
        else {
            callback?.updateButtonNext(false)
            callback?.updateEmailPwd(null)
        }
    }

    //******************************
    // Companion
    //******************************

    companion object {
        fun newInstance() = LoginEmailFragment()
    }
}
