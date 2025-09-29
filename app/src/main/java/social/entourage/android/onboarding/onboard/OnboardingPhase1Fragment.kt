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
import android.widget.Spinner
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
import timber.log.Timber
import java.util.Calendar
import java.util.Locale

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

/**
 * Architecture "Fetch -> Build -> UI" sans fallback local :
 * - /home/metadata => genres & discovery sources
 * - /salesforce/entreprises => entreprises
 * - /salesforce/entreprises/{id}/outings => événements d'une entreprise
 */
class OnboardingPhase1Fragment : Fragment() {

    // --- Binding ---
    private var _binding: FragmentOnboardingPhase1Binding? = null
    private val binding get() = _binding!!

    // --- State envoyé / conservé ---
    private var firstname: String? = null
    private var lastname: String? = null
    private var genderKey: String? = null
    private var birthdate: String? = null
    private var phone: String? = null
    private var email: String? = null
    private var hasConsent = false
    private var country: Country? = null
    private var howDidYouHearKey: String? = null
    private var company: String? = null
    private var event: String? = null
    private var callback: OnboardingStartCallback? = null

    // --- Données API ---
    private val enterpriseList = mutableListOf<SalesforceEnterprise>()
    private val eventListByEnterpriseId = mutableMapOf<String, List<SalesforceEvent>>()
    private var selectedEnterpriseId: String? = null
    private var isLoading = false

    // Options (key->label)
    private data class LabeledOption(val key: String, val label: String)
    private var genderOptions: List<LabeledOption> = emptyList()
    private var hearOptions: List<LabeledOption> = emptyList()

    // Clé de la source "entreprise" (on tente la clé exacte, sinon détection par libellé)
    private var enterpriseModeKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRST)
            lastname  = it.getString(ARG_LAST)
            genderKey = it.getString(ARG_GENDER)          // clé OU libellé
            birthdate = it.getString(ARG_BIRTHDATE)
            phone     = it.getString(ARG_PHONE)
            hasConsent= it.getBoolean(ARG_CONSENT)
            email     = it.getString(ARG_EMAIL)
            country   = it.getSerializable(ARG_COUNTRY) as? Country
            howDidYouHearKey = it.getString(ARG_HOW_DID_YOU_HEAR) // clé OU libellé
            company   = it.getString(ARG_COMPANY)
            event     = it.getString(ARG_EVENT)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingStartCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPhase1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_name)

        // PIPELINE: fetch -> build -> UI
        loadMetadata()     // genres + howDidYouHear
        loadEnterprises()  // entreprises (UI masquée tant que howDidYouHear != entreprise)
    }

    // ------------------------------------------------------------
    // Setup & Utils
    // ------------------------------------------------------------

    private fun isViewUsable(): Boolean = isAdded && _binding != null && view != null
    private inline fun safeUI(block: () -> Unit) { if (isViewUsable()) block() }

    private fun setupViews() {
        setEditTextAlignmentBasedOnLocale()

        binding.uiOnboardBirthdate.setOnClickListener { showDatePicker() }

        activity?.let { act ->
            KeyboardVisibilityEvent.setEventListener(act) { isOpen ->
                if (!isViewUsable()) return@setEventListener
                if (isOpen) showErrorMessage(false) else updateButtonNext()
            }
        }

        binding.uiOnboardConsentCheck.setOnCheckedChangeListener { _, _ -> updateButtonNext() }

        // Remplissage initial
        binding.uiOnboardConsentCheck.isChecked = hasConsent
        binding.uiOnboardEmail.setText(email)
        binding.uiOnboardPhoneCcpCode.selectedCountry = country
        binding.uiOnboardPhoneCcpCode.selectedCountry?.flagTxt = country?.flagTxt
        binding.uiOnboardPhoneEtPhone.setText(phone)
        binding.uiOnboardNamesEtLastname.setText(lastname)
        binding.uiOnboardNamesEtFirstname.setText(firstname)
        binding.uiOnboardBirthdate.setText(birthdate)

        binding.uiOnboardEmail.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
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

        // Masquer la zone entreprise/event jusqu'à choix "entreprise"
        binding.constraintLayoutCompany.visibility = View.GONE
        binding.constraintLayoutEvent.visibility = View.GONE

        updateButtonNext()
    }

    private fun setEditTextAlignmentBasedOnLocale() {
        val locale = Locale.getDefault()
        val editTexts = listOf(
            binding.uiOnboardNamesEtFirstname,
            binding.uiOnboardNamesEtLastname,
            binding.uiOnboardPhoneEtPhone,
            binding.uiOnboardEmail
        )
        for (editText in editTexts) setEditTextGravity(editText, locale)
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

    // ------------------------------------------------------------
    // FETCH (réseau) -> BUILD (mapping) -> UI (adapters)
    // ------------------------------------------------------------

    // ---------- Metadata ----------
    private fun loadMetadata() {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading(true)

            // FETCH
            val metadata = runCatching { PreonboardingApiModuleKtorClient.fetchSummaryBeforeLogin() }
                .onFailure { e ->
                    Timber.wtf("wtf metadata" + " (metadata: ${e.javaClass.simpleName})")
                    showError(getString(R.string.onboard_welcome_error_load_failed) + " (metadata: ${e.javaClass.simpleName})")
                }
                .getOrNull() ?: run {
                showLoading(false); return@launch
            }

            // BUILD
            genderOptions = metadata.user.genders
                .map { (key, label) -> LabeledOption(key, label) }
                .sortedBy { it.label.lowercase(Locale.getDefault()) }

            hearOptions = metadata.user.discoverySources
                .map { (key, label) -> LabeledOption(key, label) }
                .sortedBy { it.label.lowercase(Locale.getDefault()) }

            // Essaye d'abord la clé canonique "entreprise" (présente dans tes données),
            // sinon détecte par libellé.
            enterpriseModeKey = hearOptions.firstOrNull { it.key == "entreprise" }?.key
                ?: hearOptions.firstOrNull {
                    val l = it.label.lowercase(Locale.getDefault())
                    l.contains("entreprise") || l.contains("corporate")
                }?.key

            // UI
            runCatching {
                setupGenderSpinner(genderOptions)
                setupHowDidYouHearSpinner(hearOptions)
            }.onFailure { e ->
                showError("Erreur d’affichage (metadata UI): ${e.javaClass.simpleName}")
            }

            showLoading(false)
        }
    }

    // ---------- Entreprises ----------
    private fun loadEnterprises() {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            // FETCH
            val enterprises = runCatching { PreonboardingApiModuleKtorClient.fetchEnterprises() }
                .onFailure { e ->
                    showError(getString(R.string.onboard_welcome_error_load_failed) + " (entreprises: ${e.javaClass.simpleName})")
                }
                .getOrNull()

            if (!isViewUsable()) { isLoading = false; showLoading(false); return@launch }
            if (enterprises == null) { isLoading = false; showLoading(false); return@launch }

            // BUILD/STATE
            enterpriseList.clear()
            enterpriseList.addAll(enterprises)

            // UI
            runCatching { updateEnterpriseSpinner() }
                .onFailure { e -> showError("Erreur d’affichage (entreprises UI): ${e.javaClass.simpleName}") }

            isLoading = false
            showLoading(false)
        }
    }

    private fun updateEnterpriseSpinner() {
        if (!isViewUsable()) return
        val ctx = binding.root.context
        val names = enterpriseList.map { it.Name }

        ArrayAdapter(ctx, android.R.layout.simple_spinner_item, names).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerCompany.adapter = adapter

            // Pré-sélection si l'ID est déjà présent (on stocke l'ID dans `company`)
            val preIndex = enterpriseList.indexOfFirst { it.Id == company }
            if (preIndex >= 0) binding.uiOnboardSpinnerCompany.setSelection(preIndex)

            binding.uiOnboardSpinnerCompany.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selected = enterpriseList.getOrNull(position) ?: return
                    selectedEnterpriseId = selected.Id
                    company = selected.Id            // ⚠️ on stocke l'ID pour l'envoi back
                    loadEventsForEnterprise(selected.Id)
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }


    // ---------- Events ----------
    private fun loadEventsForEnterprise(enterpriseId: String) {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            // FETCH
            val events = runCatching { PreonboardingApiModuleKtorClient.fetchEventsForEnterprise(enterpriseId) }
                .onFailure { e ->
                    showError(getString(R.string.onboard_welcome_error_load_failed) + " (events: ${e.javaClass.simpleName})")
                }
                .getOrNull()

            if (!isViewUsable()) { isLoading = false; showLoading(false); return@launch }
            if (events == null) { isLoading = false; showLoading(false); return@launch }

            // BUILD/STATE
            eventListByEnterpriseId[enterpriseId] = events

            // UI
            runCatching { updateEventSpinner(enterpriseId) }
                .onFailure { e -> showError("Erreur d’affichage (events UI): ${e.javaClass.simpleName}") }

            isLoading = false
            showLoading(false)
        }
    }

    // ------------------------------------------------------------
    // UI binders (adapters/visibility) — robustes et séparés
    // ------------------------------------------------------------
    private fun findPreselectIndex(
        options: List<LabeledOption>,
        keyOrLabel: String?
    ): Int {
        if (keyOrLabel.isNullOrBlank()) return -1
        val needle = keyOrLabel.trim()
        // D'abord on tente la clé exacte
        val byKey = options.indexOfFirst { it.key == needle }
        if (byKey >= 0) return byKey
        // Sinon on tente le libellé (sans tenir compte de la casse)
        return options.indexOfFirst { it.label.equals(needle, ignoreCase = true) }
    }

    private fun setupGenderSpinner(options: List<LabeledOption>) {
        if (!isViewUsable()) return
        val ctx = binding.root.context
        val labels = options.map { it.label }

        ArrayAdapter(ctx, android.R.layout.simple_spinner_item, labels).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerGender.adapter = adapter

            // pré-sélection clé/libellé
            val pre = findPreselectIndex(options, genderKey)
            if (pre >= 0) binding.uiOnboardSpinnerGender.setSelection(pre)

            binding.uiOnboardSpinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    genderKey = options.getOrNull(position)?.key
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setupHowDidYouHearSpinner(options: List<LabeledOption>) {
        if (!isViewUsable()) return
        val ctx = binding.root.context
        val labels = options.map { it.label }

        ArrayAdapter(ctx, android.R.layout.simple_spinner_item, labels).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerHowDidYouHear.adapter = adapter

            val pre = findPreselectIndex(options, howDidYouHearKey)
            if (pre >= 0) binding.uiOnboardSpinnerHowDidYouHear.setSelection(pre)

            binding.uiOnboardSpinnerHowDidYouHear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    howDidYouHearKey = options.getOrNull(position)?.key
                    updateCompanyEventVisibility()
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }


    private fun updateEventSpinner(enterpriseId: String) {
        if (!isViewUsable()) return
        val ctx = binding.root.context

        val events = eventListByEnterpriseId[enterpriseId] ?: emptyList()
        val names = events.map { it.Name }

        ArrayAdapter(ctx, android.R.layout.simple_spinner_item, names).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.uiOnboardSpinnerEvent.adapter = adapter

            // Pré-sélection si l'ID est déjà présent (on stocke l'ID dans `event`)
            val preIndex = events.indexOfFirst { it.Id == event }
            if (preIndex >= 0) binding.uiOnboardSpinnerEvent.setSelection(preIndex)

            binding.uiOnboardSpinnerEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    event = events.getOrNull(position)?.Id   // ⚠️ on stocke l'ID pour l'envoi back
                    updateButtonNext()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }


    private fun clearSpinner(spinner: Spinner) {
        if (!isViewUsable()) return
        val ctx = binding.root.context
        spinner.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, emptyList<String>()).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun updateCompanyEventVisibility() {
        val isEnterprise = enterpriseModeKey != null && howDidYouHearKey == enterpriseModeKey
        binding.constraintLayoutCompany.visibility = if (isEnterprise) View.VISIBLE else View.GONE
        binding.constraintLayoutEvent.visibility   = if (isEnterprise) View.VISIBLE else View.GONE

        if (!isEnterprise) {
            company = null   // ⚠️ on vide l'ID
            event = null     // ⚠️ on vide l'ID
            selectedEnterpriseId = null
            clearSpinner(binding.uiOnboardSpinnerCompany)
            clearSpinner(binding.uiOnboardSpinnerEvent)
        }
    }

    // ------------------------------------------------------------
    // Date & validations
    // ------------------------------------------------------------

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val ctx = context ?: return
        DatePickerDialog(ctx, { _, y, m, d ->
            birthdate = "$d/${m + 1}/$y"
            safeUI {
                binding.uiOnboardBirthdate.setText(birthdate)
                updateButtonNext()
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatBirthdateForAPI(displayDate: String?): String? {
        if (displayDate.isNullOrEmpty()) return null
        val p = displayDate.split("/")
        if (p.size != 3) return null
        val day = p[0].padStart(2, '0')
        val month = p[1].padStart(2, '0')
        val year = p[2]
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
                genderKey,
                formatBirthdateForAPI(binding.uiOnboardBirthdate.text?.toString()), // dd-MM-yyyy
                binding.uiOnboardPhoneCcpCode.selectedCountry,
                binding.uiOnboardPhoneEtPhone.text?.toString(),
                binding.uiOnboardEmail.text?.toString(),
                binding.uiOnboardConsentCheck.isChecked,
                howDidYouHearKey,
                company, // 👈 ID entreprise
                event    // 👈 ID event
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
    private fun isValidPhone()    = (binding.uiOnboardPhoneEtPhone.text?.length ?: 0) >= minCharsPhone
    private fun isValidEmail(): Boolean {
        val email = binding.uiOnboardEmail.text?.toString().orEmpty()
        return if (email.isNotEmpty()) email.isValidEmail() else true
    }

    private fun isValidCompanyEvent(): Boolean {
        val needsCorp = enterpriseModeKey != null && howDidYouHearKey == enterpriseModeKey
        if (!needsCorp) return true
        // Ici `company` et `event` contiennent des IDs
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
        val needsCorp = enterpriseModeKey != null && howDidYouHearKey == enterpriseModeKey
        binding.errorMessageCompany.visibility =
            if (show && company.isNullOrEmpty() && needsCorp) View.VISIBLE else View.GONE
        binding.errorMessageEvent.visibility =
            if (show && event.isNullOrEmpty() && needsCorp) View.VISIBLE else View.GONE
    }

    // ------------------------------------------------------------
    // UX helpers
    // ------------------------------------------------------------

    private fun showLoading(show: Boolean) {
        if (!isViewUsable()) return
        // Branche ton loader ici (progress/shimmer) si besoin.
    }

    private fun showError(message: String) {
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
                putSerializable(ARG_COUNTRY, country)
                putString(ARG_PHONE, phone)
                putString(ARG_EMAIL, email)
                putBoolean(ARG_CONSENT, hasConsent)
                putString(ARG_HOW_DID_YOU_HEAR, howDidYouHear)
                putString(ARG_COMPANY, company)
                putString(ARG_EVENT, event)
            }
        }
    }
}
