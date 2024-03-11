package social.entourage.android.onboarding.onboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingPhase1Binding
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
    private lateinit var binding: FragmentOnboardingPhase1Binding
    private var firstname:String? = null
    private var lastname:String? = null
    private var phone:String? = null
    private var email:String? = null
    private var hasConsent = false
    private var country: Country? = null

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
        binding = FragmentOnboardingPhase1Binding.inflate(inflater, container, false)
        return binding.root
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

        binding.uiOnboardConsentCheck?.setOnCheckedChangeListener { compoundButton, b ->
            updateButtonNext()
        }

        binding.uiOnboardConsentCheck.isChecked = hasConsent
        binding.uiOnboardEmail.setText(email)
        binding.uiOnboardPhoneCcpCode.selectedCountry = country
        binding.uiOnboardPhoneCcpCode.selectedCountry?.flagTxt = country?.flagTxt
        binding.uiOnboardPhoneEtPhone.setText(phone)
        binding.uiOnboardNamesEtLastname.setText(lastname)
        binding.uiOnboardNamesEtFirstname.setText(firstname)
        binding.uiOnboardEmail.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                (requireActivity() as OnboardingStartActivity).setEmail(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.uiOnboardPhoneCcpCode?.countryCodePickerListener = object : CountryCodePickerListener {
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
            callback?.validateNames(binding.uiOnboardNamesEtFirstname?.text?.toString(),
                binding.uiOnboardNamesEtLastname?.text?.toString(),
                binding.uiOnboardPhoneCcpCode?.selectedCountry,
                binding.uiOnboardPhoneEtPhone?.text?.toString(),
                binding.uiOnboardEmail?.text?.toString(),
                binding.uiOnboardConsentCheck?.isChecked ?: false)
        }
        else {
            if (!binding.uiOnboardNamesEtFirstname?.text.isNullOrEmpty() &&
                !binding.uiOnboardNamesEtLastname?.text.isNullOrEmpty())
                showErrorMessage(true)

            callback?.updateButtonNext(false)
            callback?.validateNames(null, null, null,
                null,null,binding.uiOnboardConsentCheck.isChecked)
        }
    }

    /********
     * Validations
     */
    private fun isValidFirstname() = (binding.uiOnboardNamesEtFirstname?.text?.length ?: 0) >= minChars
    private fun isValidLastname() = (binding.uiOnboardNamesEtLastname?.text?.length ?: 0) >= minChars
    private fun isValidPhone() = (binding.uiOnboardPhoneEtPhone?.text?.length ?: 0) >= minCharsPhone
    private fun isValidEmail() : Boolean {
        val email:String = binding.uiOnboardEmail?.text.toString()
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
                binding.errorMessageFirstname.visibility = View.VISIBLE
            }
            else if (!isValidLastname()) {
                binding.errorMessageLastname.visibility = View.VISIBLE
            }
            else if (!isValidPhone()) {
                binding.errorMessagePhone.visibility = View.VISIBLE
            }
            else if (!isValidEmail()) {
                binding.errorMessageEmail.visibility = View.VISIBLE
            }
        }
        else {
            binding.errorMessageFirstname.visibility = View.GONE
            binding.errorMessageLastname.visibility = View.GONE
            binding.errorMessagePhone.visibility = View.GONE
            binding.errorMessageEmail.visibility = View.GONE
        }
    }

    companion object {
        const val minChars = 2
        const val minCharsPhone = 9
        @JvmStatic
        fun newInstance(firstname: String?, lastname: String?, country: Country?, phone:String?, email:String?, hasConsent:Boolean ) =
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