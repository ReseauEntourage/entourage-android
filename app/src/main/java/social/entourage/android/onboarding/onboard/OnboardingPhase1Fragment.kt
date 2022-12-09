package social.entourage.android.onboarding.onboard

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_onboarding_phase1.*
import kotlinx.android.synthetic.main.layout_code_picker.view.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import social.entourage.android.R
import social.entourage.android.tools.isValidEmail
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.countrycodepicker.Country
import social.entourage.android.tools.view.countrycodepicker.CountryCodePickerListener

private const val ARG_FIRST = "firstN"
private const val ARG_LAST = "lastN"
private const val ARG_COUNTRY = "countryC"
private const val ARG_PHONE = "phone"
private const val ARG_EMAIL = "email"
private const val ARG_CONSENT = "consent"

class OnboardingPhase1Fragment : Fragment() {
    private var firstname:String? = null
    private var lastname:String? = null
    private var phone:String? = null
    private var email:String? = null
    private var hasConsent = false
    private var country:Country? = null


    private var callback:OnboardingStartCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRST)
            lastname = it.getString(ARG_LAST)
            phone = it.getString(ARG_PHONE)
            hasConsent = it.getBoolean(ARG_CONSENT)
            email = it.getString(ARG_EMAIL)
            country = it.getSerializable(ARG_COUNTRY) as? Country
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_phase1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateButtonNext(checkAndValidateInput())

        setupViews()
        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_name)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingStartCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    fun setupViews() {
        //Listen to keyboard visibility
        activity?.let {
            KeyboardVisibilityEvent.setEventListener(it) { isOpen ->
                if (isOpen)
                    showErrorMessage(false)
                else
                    updateButtonNext()
            }
        }

        ui_onboard_consent_check?.setOnCheckedChangeListener { compoundButton, b ->
            updateButtonNext()
        }

        ui_onboard_consent_check.isChecked = hasConsent
        ui_onboard_email.setText(email)
        ui_onboard_phone_ccp_code.selectedCountry = country
        ui_onboard_phone_ccp_code.selected_country_tv.text = country?.flagTxt
        ui_onboard_phone_et_phone.setText(phone)
        ui_onboard_names_et_lastname.setText(lastname)
        ui_onboard_names_et_firstname.setText(firstname)

        ui_onboard_phone_ccp_code?.countryCodePickerListener = object : CountryCodePickerListener {
            override fun updatedCountry(newCountry: Country) {
                country = newCountry
                updateButtonNext()
            }
        }

        updateButtonNext()
    }

    fun updateButtonNext() {
        if (checkAndValidateInput()) {
            showErrorMessage(false)
            callback?.updateButtonNext(true)
            callback?.validateNames(ui_onboard_names_et_firstname?.text?.toString(),
                ui_onboard_names_et_lastname?.text?.toString(),
                ui_onboard_phone_ccp_code?.selectedCountry,
                ui_onboard_phone_et_phone?.text?.toString(),
                ui_onboard_email?.text?.toString(),
                ui_onboard_consent_check?.isChecked ?: false)
        }
        else {
            if (!ui_onboard_names_et_firstname?.text.isNullOrEmpty() &&
                !ui_onboard_names_et_lastname?.text.isNullOrEmpty())
                showErrorMessage(true)

            callback?.updateButtonNext(false)
            callback?.validateNames(null, null, null,
                null,null,ui_onboard_consent_check.isChecked)
        }
    }

    /********
     * Validations
     */
    private fun isValidFirstname() = (ui_onboard_names_et_firstname?.text?.length ?: 0) >= minChars
    private fun isValidLastname() = (ui_onboard_names_et_lastname?.text?.length ?: 0) >= minChars
    private fun isValidPhone() = (ui_onboard_phone_et_phone?.text?.length ?: 0) >= minCharsPhone
    private fun isValidEmail() : Boolean {
        val email:String = ui_onboard_email?.text.toString()
        if (email.isNotEmpty()) {
            return email.isNotEmpty() && email.isValidEmail()
        }
        return true
    }

    fun checkAndValidateInput(): Boolean {
        if (!isValidFirstname()) {
            return false
        }
        else if (!isValidLastname()) {
            return false
        }
        else if (!isValidPhone()) {
            return false
        }
        else if (!isValidEmail()) {
            return false
        }
        return true
    }

    private fun showErrorMessage(show: Boolean) {

        if (show) {
            if (!isValidFirstname()) {
                error_message_firstname?.visibility = View.VISIBLE
            }
            else if (!isValidLastname()) {
                error_message_lastname?.visibility = View.VISIBLE
            }
            else if (!isValidPhone()) {
                error_message_phone?.visibility = View.VISIBLE
            }
            else if (!isValidEmail()) {
                error_message_email?.visibility = View.VISIBLE
            }
        }
        else {
            error_message_firstname?.visibility = View.GONE
            error_message_lastname?.visibility = View.GONE
            error_message_phone?.visibility = View.GONE
            error_message_email?.visibility = View.GONE
        }
    }

    companion object {
        const val minChars = 2
        const val minCharsPhone = 9
        @JvmStatic
        fun newInstance(firstname: String?, lastname: String?, country: Country?,phone:String?,email:String?,hasConsent:Boolean ) =
            OnboardingPhase1Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FIRST, firstname)
                    putString(ARG_LAST, lastname)
                    putString(ARG_PHONE, phone)
                    putSerializable(ARG_COUNTRY, country)
                    putString(ARG_EMAIL, email)
                    putBoolean(ARG_CONSENT,hasConsent)
                }
            }
    }
}