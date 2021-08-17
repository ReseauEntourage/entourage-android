package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_names.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import social.entourage.android.R
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents

private const val ARG_FIRSTNAME = "firstname"
private const val ARG_LASTNAME = "lastname"

class OnboardingNamesFragment : Fragment() {
    private var firstname: String? = null
    private var lastname: String? = null

    private var callback: OnboardingCallback? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRSTNAME)
            lastname = it.getString(ARG_LASTNAME)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_names, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateButtonNext(checkAndValidateInput())

        setupViews()
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_NAMES)
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
    // methods
    //**********//**********//**********

    fun setupViews() {
        onboard_names_mainlayout?.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            view.performClick()
            true
        }

        ui_onboard_names_et_lastname?.setOnEditorActionListener { _, event, _ ->
            if (event == EditorInfo.IME_ACTION_DONE) {
                updateButtonNext(true)
            }
            false
        }

        ui_onboard_names_et_firstname?.setText(firstname)
        ui_onboard_names_et_lastname?.setText(lastname)

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

    fun updateButtonNext(isValidate: Boolean) {
        if (checkAndValidateInput()) {
            showErrorMessage(false)
            callback?.updateButtonNext(true)
            callback?.validateNames(ui_onboard_names_et_firstname?.text?.toString(), ui_onboard_names_et_lastname?.text?.toString(), isValidate)
        }
        else {
            if (!ui_onboard_names_et_firstname?.text.isNullOrEmpty() &&
                !ui_onboard_names_et_lastname?.text.isNullOrEmpty())
                showErrorMessage(true)

            callback?.updateButtonNext(false)
            callback?.validateNames(null, null, false)
        }
    }

    fun checkAndValidateInput(): Boolean {
        if (isValidFirstname() && isValidLastname()) {
            return true
        }
        return false
    }

    private fun isValidFirstname() = ui_onboard_names_et_firstname?.text?.length ?: 0 >= minChars
    private fun isValidLastname() = ui_onboard_names_et_lastname?.text?.length ?: 0 >= minChars

    private fun showErrorMessage(show: Boolean) {
        error_message_tv?.text = getString(
            if (!isValidFirstname()) R.string.user_edit_profile_invalid_firstname
            else R.string.user_edit_profile_invalid_lastname
        )
        error_message_tv?.visibility = if (show) View.VISIBLE else View.GONE
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val minChars = 2

        fun newInstance(firstName: String?, lastName: String?) =
                OnboardingNamesFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FIRSTNAME, firstName)
                        putString(ARG_LASTNAME, lastName)
                    }
                }
    }
}
