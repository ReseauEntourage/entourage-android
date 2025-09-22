package social.entourage.android.onboarding.onboard

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import social.entourage.android.R
import social.entourage.android.api.model.SalesforceEnterprise
import social.entourage.android.api.model.SalesforceEvent
import social.entourage.android.databinding.FragmentOnboardingPhase1Binding
import social.entourage.android.tools.isValidEmail
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.countrycodepicker.Country
import social.entourage.android.tools.view.countrycodepicker.CountryCodePickerListener
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val ARG_FIRST = "firstN"
private const val ARG_LAST = "lastN"
private const val ARG_GENDER = "gender"
private const val ARG_BIRTHDATE = "birthdate"
private const val ARG_COUNTRY = "countryC"
private const val ARG_PHONE = "phone"
private const val ARG_EMAIL = "email"
private const val ARG_CONSENT = "consent"
private const val ARG_HOW_DID_YOU_HEAR = "howDidYouHear"
private const val ARG_COMPANY = "company"
private const val ARG_EVENT = "event"

class OnboardingPhase1Fragment : Fragment() {

    // --- Binding sécurisé ---
    private var _binding: FragmentOnboardingPhase1Binding? = null
    private val binding get() = _binding!!

    // --- State ---
    private var firstname: String? = null
    private var lastname: String? = null
    private var gender: String? = null
    private var birthdate: String? = null
    private var phone: String? = null
    private var email: String? = null
    private var hasConsent = false
    private var country: Country? = null
    private var howDidYouHear: String? = null
    private var company: String? = null
    private var event: String? = null
    private var callback: OnboardingStartCallback? = null

    // Liste des entreprises et événements chargés depuis l'API
    private val enterpriseList = mutableListOf<SalesforceEnterprise>()
    private val eventList = mutableMapOf<String, List<SalesforceEvent>>()
    private var selectedEnterpriseId: String? = null
    private var isLoading = false

    // On mémorise le label “entreprise” une seule fois pour éviter getString() à chaud plus tard
    private var labelCorporateAwareness: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRST)
            lastname = it.getString(ARG_LAST)
            gender = it.getString(ARG_GENDER)
            birthdate = it.getString(ARG_BIRTHDATE)
            phone = it.getString(ARG_PHONE)
            hasConsent = it.getBoolean(ARG_CONSENT)
            email = it.getString(ARG_EMAIL)
            country = it.getSerializable(ARG_COUNTRY) as? Country
            howDidYouHear = it.getString(ARG_HOW_DID_YOU_HEAR)
            company = it.getString(ARG_COMPANY)
            event = it.getString(ARG_EVENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPhase1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mémorise les libellés dès que la vue est disponible (contexte garanti ici)
        labelCorporateAwareness = getString(R.string.onboard_welcome_how_did_you_hear_corporate_awareness)

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

    override fun onDestroyView() {
        super.onDestroyView()
        // évite les NPE / leaks
        _binding = null
    }

    // --- Helpers de sûreté UI ---
    private fun isViewUsable(): Boolean = isAdded && _binding != null && view != null

    private inline fun safeUI(block: () -> Unit) {
        if (isViewUsable()) block()
    }

    // --- UI setup ---
    private fun setEditTextAlignmentBasedOnLocale() {
        val locale = Locale.getDefault()
        val editTexts = listOf(
            binding.uiOnboardNamesEtFirstname,
            binding.uiOnboardNamesEtLastname,
            binding.uiOnboardPhoneEtPhone,
            binding.uiOnboardEmail
        )
        for (editText in editTexts) {
            setEditTextGravity(editText, locale)
        }
    }

    private fun setEditTextGravity(editText: EditText?, locale: Locale) {
        editText?.let {
            if (locale.language == "ar") {
                it.gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.END
                it.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            } else {
                it.gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.START
                it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            }
        }
    }

    fun setupViews() {
        setEditTextAlignmentBasedOnLocale()

        // Genre
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.gender_options).toList()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerGender.adapter = adapter
        }
        binding.uiOnboardSpinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                gender = parent?.getItemAtPosition(position).toString()
                updateButtonNext()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Comment vous nous avez connu ?
        val howDidYouHearOptions = listOf(
            getString(R.string.onboard_welcome_how_did_you_hear_word_of_mouth),
            getString(R.string.onboard_welcome_how_did_you_hear_internet),
            getString(R.string.onboard_welcome_how_did_you_hear_tv_media),
            getString(R.string.onboard_welcome_how_did_you_hear_social_media),
            getString(R.string.onboard_welcome_how_did_you_hear_corporate_awareness)
        )
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            howDidYouHearOptions
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerHowDidYouHear.adapter = adapter
        }
        binding.uiOnboardSpinnerHowDidYouHear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                howDidYouHear = parent?.getItemAtPosition(position).toString()
                updateCompanyEventVisibility()
                updateButtonNext()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Date d'anniversaire
        binding.uiOnboardBirthdate.setOnClickListener { showDatePicker() }

        // Clavier — protège si détaché
        activity?.let { act ->
            KeyboardVisibilityEvent.setEventListener(act) { isOpen ->
                if (!isViewUsable()) return@setEventListener
                if (isOpen) showErrorMessage(false) else updateButtonNext()
            }
        }

        // Consentement
        binding.uiOnboardConsentCheck.setOnCheckedChangeListener { _, _ -> updateButtonNext() }

        // Remplir les champs
        binding.uiOnboardConsentCheck.isChecked = hasConsent
        binding.uiOnboardEmail.setText(email)
        binding.uiOnboardPhoneCcpCode.selectedCountry = country
        binding.uiOnboardPhoneCcpCode.selectedCountry?.flagTxt = country?.flagTxt
        binding.uiOnboardPhoneEtPhone.setText(phone)
        binding.uiOnboardNamesEtLastname.setText(lastname)
        binding.uiOnboardNamesEtFirstname.setText(firstname)
        binding.uiOnboardBirthdate.setText(birthdate)

        // Écouteurs
        binding.uiOnboardEmail.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                // Ne pas crasher si activity est null / détachée
                (activity as? OnboardingStartActivity)?.setEmail(s?.toString().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.uiOnboardPhoneCcpCode.countryCodePickerListener = object : CountryCodePickerListener {
            override fun updatedCountry(newCountry: Country) {
                country = newCountry
                updateButtonNext()
            }
        }

        // Charge les entreprises au démarrage
        loadEnterprises()

        updateButtonNext()
    }

    private fun updateCompanyEventVisibility() {
        val isEnterprise = howDidYouHear == labelCorporateAwareness
        binding.constraintLayoutCompany.visibility = if (isEnterprise) View.VISIBLE else View.GONE
        binding.constraintLayoutEvent.visibility = if (isEnterprise) View.VISIBLE else View.GONE
        if (!isEnterprise) {
            company = null
            event = null
            selectedEnterpriseId = null
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        // Utilise context? pour éviter crash si détaché (au pire, ne montre pas le picker)
        val ctx = context ?: return
        DatePickerDialog(ctx, { _, y, m, d ->
            birthdate = "$d/${m + 1}/$y"
            safeUI {
                binding.uiOnboardBirthdate.setText(birthdate)
                updateButtonNext()
            }
        }, year, month, day).show()
    }

    private fun formatBirthdateForAPI(displayDate: String?): String? {
        if (displayDate.isNullOrEmpty()) return null
        val parts = displayDate.split("/")
        if (parts.size != 3) return null
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2]
        return "$day-$month-$year"
    }

    fun updateButtonNext() {
        if (!isViewUsable()) return
        if (checkAndValidateInput()) {
            showErrorMessage(false)
            showCompanyEventError(false)
            callback?.updateButtonNext(true)
            callback?.validateNames(
                binding.uiOnboardNamesEtFirstname.text.toString(),
                binding.uiOnboardNamesEtLastname.text.toString(),
                binding.uiOnboardSpinnerGender.selectedItem?.toString(),
                formatBirthdateForAPI(binding.uiOnboardBirthdate.text?.toString()),
                binding.uiOnboardPhoneCcpCode.selectedCountry,
                binding.uiOnboardPhoneEtPhone.text?.toString(),
                binding.uiOnboardEmail.text?.toString(),
                binding.uiOnboardConsentCheck.isChecked,
                howDidYouHear,
                company,
                event
            )
        } else {
            if (!binding.uiOnboardNamesEtFirstname.text.isNullOrEmpty() &&
                !binding.uiOnboardNamesEtLastname.text.isNullOrEmpty()
            ) {
                showErrorMessage(true)
                showCompanyEventError(true)
            }
            callback?.updateButtonNext(false)
            callback?.validateNames(null, null, null, null, null, null, null, false, null, null, null)
        }
    }

    private fun isValidFirstname() = (binding.uiOnboardNamesEtFirstname.text?.length ?: 0) >= minChars
    private fun isValidLastname() = (binding.uiOnboardNamesEtLastname.text?.length ?: 0) >= minChars
    private fun isValidPhone() = (binding.uiOnboardPhoneEtPhone.text?.length ?: 0) >= minCharsPhone
    private fun isValidEmail(): Boolean {
        val email = binding.uiOnboardEmail.text?.toString().orEmpty()
        return if (email.isNotEmpty()) email.isValidEmail() else true
    }

    private fun isValidCompanyEvent(): Boolean {
        // Si le label n’a pas pu être initialisé (vue pas prête), on ne bloque pas (true)
        val corp = labelCorporateAwareness ?: return true
        if (howDidYouHear != corp) return true
        return !company.isNullOrEmpty() && !event.isNullOrEmpty()
    }

    fun checkAndValidateInput(): Boolean {
        return isValidFirstname() &&
                isValidLastname() &&
                isValidPhone() &&
                isValidEmail() &&
                isValidCompanyEvent()
    }

    private fun showErrorMessage(show: Boolean) {
        if (!isViewUsable()) return
        binding.errorMessageFirstname.visibility =
            if (show && !isValidFirstname()) View.VISIBLE else View.GONE
        binding.errorMessageLastname.visibility =
            if (show && !isValidLastname()) View.VISIBLE else View.GONE
        binding.errorMessagePhone.visibility =
            if (show && !isValidPhone()) View.VISIBLE else View.GONE
        binding.errorMessageEmail.visibility =
            if (show && !isValidEmail()) View.VISIBLE else View.GONE
    }

    private fun showCompanyEventError(show: Boolean) {
        if (!isViewUsable()) return
        val corp = labelCorporateAwareness
        val isCorp = corp != null && howDidYouHear == corp
        binding.errorMessageCompany.visibility =
            if (show && company.isNullOrEmpty() && isCorp) View.VISIBLE else View.GONE
        binding.errorMessageEvent.visibility =
            if (show && event.isNullOrEmpty() && isCorp) View.VISIBLE else View.GONE
    }

    // ====================== KTOR ======================

    private fun loadEnterprises() {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val enterprises = fetchEnterprises()
                if (!isViewUsable()) return@launch
                enterpriseList.clear()
                enterpriseList.addAll(enterprises)
                updateEnterpriseSpinner()
            } catch (e: Exception) {
                if (!isViewUsable()) {
                    isLoading = false
                    showLoading(false)
                    return@launch
                }
                //TODO RESET THIS LINE
                //showError(getString(R.string.onboard_welcome_error_load_failed))
                fallbackToHardcodedEnterprises()
            } finally {
                if (isViewUsable()) showLoading(false)
                isLoading = false
            }
        }
    }

    private fun loadEventsForEnterprise(enterpriseId: String) {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val events = fetchEventsForEnterprise(enterpriseId)
                if (!isViewUsable()) return@launch
                eventList[enterpriseId] = events
                updateEventSpinner(enterpriseId)
            } catch (e: Exception) {
                if (!isViewUsable()) {
                    isLoading = false
                    showLoading(false)
                    return@launch
                }
                showError(getString(R.string.onboard_welcome_error_load_failed))
                fallbackToHardcodedEvents()
            } finally {
                if (isViewUsable()) showLoading(false)
                isLoading = false
            }
        }
    }

    private suspend fun fetchEnterprises(): List<SalesforceEnterprise> = suspendCoroutine { continuation ->
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = PreonboardingApiModuleKtorClient.fetchEnterprises()
                continuation.resume(response)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    private suspend fun fetchEventsForEnterprise(enterpriseId: String): List<SalesforceEvent> = suspendCoroutine { continuation ->
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = PreonboardingApiModuleKtorClient.fetchEventsForEnterprise(enterpriseId)
                continuation.resume(response)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    private fun updateEnterpriseSpinner() {
        if (!isViewUsable()) return
        val enterpriseNames = enterpriseList.map { it.Name }
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            enterpriseNames
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerCompany.adapter = adapter
            binding.uiOnboardSpinnerCompany.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedEnterprise = enterpriseList.getOrNull(position) ?: return
                    selectedEnterpriseId = selectedEnterprise.Id
                    company = selectedEnterprise.Name
                    loadEventsForEnterprise(selectedEnterprise.Id)
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun updateEventSpinner(enterpriseId: String) {
        if (!isViewUsable()) return
        val events = eventList[enterpriseId] ?: emptyList()
        val eventNames = events.map { it.Name }
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            eventNames
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerEvent.adapter = adapter
            binding.uiOnboardSpinnerEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    event = events.getOrNull(position)?.Name
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun fallbackToHardcodedEnterprises() {
        if (!isViewUsable()) return
        val ctx = context ?: return
        val companyOptions = listOf(
            ctx.getString(R.string.onboard_welcome_company_a),
            ctx.getString(R.string.onboard_welcome_company_b),
            ctx.getString(R.string.onboard_welcome_company_c),
            ctx.getString(R.string.onboard_welcome_company_d),
            ctx.getString(R.string.onboard_welcome_company_e)
        )
        ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_item,
            companyOptions
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerCompany.adapter = adapter
            binding.uiOnboardSpinnerCompany.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    company = parent?.getItemAtPosition(position).toString()
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun fallbackToHardcodedEvents() {
        if (!isViewUsable()) return
        val ctx = context ?: return
        val eventOptions = listOf(
            ctx.getString(R.string.onboard_welcome_event_1),
            ctx.getString(R.string.onboard_welcome_event_2),
            ctx.getString(R.string.onboard_welcome_event_3),
            ctx.getString(R.string.onboard_welcome_event_4),
            ctx.getString(R.string.onboard_welcome_event_5)
        )
        ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_item,
            eventOptions
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerEvent.adapter = adapter
            binding.uiOnboardSpinnerEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    event = parent?.getItemAtPosition(position).toString()
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun showLoading(show: Boolean) {
        // Exemple: binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (!isViewUsable()) return
        // TODO: implémenter si nécessaire
    }

    private fun showError(message: String) {
        // Utiliser un Toast sûr
        context?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        const val minChars = 2
        const val minCharsPhone = 9

        @JvmStatic
        fun newInstance(
            firstname: String?, lastname: String?, gender: String?, birthdate: String?,
            country: Country?, phone: String?, email: String?, hasConsent: Boolean,
            howDidYouHear: String?, company: String?, event: String?
        ) = OnboardingPhase1Fragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FIRST, firstname)
                putString(ARG_LAST, lastname)
                putString(ARG_GENDER, gender)
                putString(ARG_BIRTHDATE, birthdate)
                putString(ARG_PHONE, phone)
                putSerializable(ARG_COUNTRY, country)
                putString(ARG_EMAIL, email)
                putBoolean(ARG_CONSENT, hasConsent)
                putString(ARG_HOW_DID_YOU_HEAR, howDidYouHear)
                putString(ARG_COMPANY, company)
                putString(ARG_EVENT, event)
            }
        }
    }
}
