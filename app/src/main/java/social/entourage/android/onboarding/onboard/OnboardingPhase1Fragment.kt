package social.entourage.android.onboarding.onboard

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import social.entourage.android.R
import social.entourage.android.api.model.SalesforceEnterprise
import social.entourage.android.api.model.SalesforceEvent
import social.entourage.android.databinding.FragmentOnboardingPhase1Binding
import social.entourage.android.tools.isValidEmail
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

class OnboardingPhase1Fragment : Fragment() {

    private var _binding: FragmentOnboardingPhase1Binding? = null
    private val binding get() = _binding!!

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

    private val enterpriseList = mutableListOf<SalesforceEnterprise>()
    private val eventListByEnterpriseId = mutableMapOf<String, List<SalesforceEvent>>()
    private var selectedEnterpriseId: String? = null
    private var isLoading = false

    private data class LabeledOption(val key: String, val label: String)
    private var hearOptions: List<LabeledOption> = emptyList()
    private var enterpriseModeKey: String? = null
    private var isDatePickerShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRST)
            lastname = it.getString(ARG_LAST)
            genderKey = it.getString(ARG_GENDER)
            birthdate = it.getString(ARG_BIRTHDATE)
            phone = it.getString(ARG_PHONE)
            hasConsent = it.getBoolean(ARG_CONSENT)
            email = it.getString(ARG_EMAIL)
            country = it.getSerializable(ARG_COUNTRY) as? Country
            howDidYouHearKey = it.getString(ARG_HOW_DID_YOU_HEAR)
            company = it.getString(ARG_COMPANY)
            event = it.getString(ARG_EVENT)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = activity as? OnboardingStartCallback
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
        loadMetadata()
        loadEnterprises()
    }

    private fun isViewUsable(): Boolean = isAdded && _binding != null && view != null
    private inline fun safeUI(block: () -> Unit) { if (isViewUsable()) block() }

    private fun setupViews() {
        setEditTextAlignmentBasedOnLocale()

        // ==== Champ date : pas de clavier, clic uniquement ====
        binding.uiOnboardBirthdate.apply {
            // Empêche le clavier coûte que coûte
            keyListener = null
            try { showSoftInputOnFocus = false } catch (_: Throwable) {}
            isCursorVisible = false
            isLongClickable = false
            setTextIsSelectable(false)

            // On ne prend pas le focus (sinon certains OEM affichent le clavier)
            isFocusable = false
            isFocusableInTouchMode = false

            setOnClickListener { showDatePicker() }
        }

        activity?.let { act ->
            KeyboardVisibilityEvent.setEventListener(act) { isOpen ->
                if (!isViewUsable()) return@setEventListener
                if (isOpen) showErrorMessage(false) else updateButtonNext()
            }
        }

        binding.uiOnboardConsentCheck.setOnCheckedChangeListener { _, _ -> updateButtonNext() }
        binding.uiOnboardConsentCheck.isChecked = hasConsent

        // Préremplissage
        binding.uiOnboardEmail.setText(email)
        binding.uiOnboardPhoneCcpCode.selectedCountry = country
        binding.uiOnboardPhoneCcpCode.selectedCountry?.flagTxt = country?.flagTxt
        binding.uiOnboardPhoneEtPhone.setText(phone)
        binding.uiOnboardNamesEtLastname.setText(lastname)
        binding.uiOnboardNamesEtFirstname.setText(firstname)
        binding.uiOnboardBirthdate.setText(birthdate)

        // Email change → propage à l'activité
        binding.uiOnboardEmail.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                (activity as? OnboardingStartActivity)?.setEmail(s?.toString().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Country picker
        binding.uiOnboardPhoneCcpCode.countryCodePickerListener = object : CountryCodePickerListener {
            override fun updatedCountry(newCountry: Country) {
                country = newCountry
                updateButtonNext()
            }
        }

        // Cachés au départ
        binding.tilCompany.isVisible = false
        binding.tilEvent.isVisible = false

        setupFixedGenderDropdown()

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
                it.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                it.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            } else {
                it.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            }
        }
    }

    // ====== Chargements distants ======
    private fun loadMetadata() {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading(true)

            val metadata = runCatching { PreonboardingApiModuleKtorClient.fetchSummaryBeforeLogin() }
                .onFailure { e ->
                    Timber.wtf("metadata ${e.javaClass.simpleName}")
                    showError(getString(R.string.onboard_welcome_error_load_failed) + " (metadata)")
                }
                .getOrNull() ?: run { showLoading(false); return@launch }

            hearOptions = metadata.user.discoverySources
                .map { (key, label) -> LabeledOption(key, label) }
                .sortedBy { it.label.lowercase(Locale.getDefault()) }

            enterpriseModeKey = hearOptions.firstOrNull { it.key == "entreprise" }?.key
                ?: hearOptions.firstOrNull {
                    val l = it.label.lowercase(Locale.getDefault())
                    l.contains("entreprise") || l.contains("corporate")
                }?.key

            runCatching { setupHowDidYouHearDropdown(hearOptions) }
                .onFailure { showError("UI error (howDidYouHear)") }

            showLoading(false)
        }
    }

    private fun loadEnterprises() {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val enterprises = runCatching { PreonboardingApiModuleKtorClient.fetchEnterprises() }
                .onFailure { showError(getString(R.string.onboard_welcome_error_load_failed) + " (entreprises)") }
                .getOrNull()

            if (!isViewUsable()) { isLoading = false; showLoading(false); return@launch }
            if (enterprises == null) { isLoading = false; showLoading(false); return@launch }

            enterpriseList.clear()
            enterpriseList.addAll(enterprises)

            runCatching { updateEnterpriseDropdown() }
                .onFailure { showError("UI error (entreprises)") }

            isLoading = false
            showLoading(false)
        }
    }

    private fun loadEventsForEnterprise(enterpriseId: String) {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val events = runCatching { PreonboardingApiModuleKtorClient.fetchEventsForEnterprise(enterpriseId) }
                .onFailure { showError(getString(R.string.onboard_welcome_error_load_failed) + " (events)") }
                .getOrNull()

            if (!isViewUsable()) { isLoading = false; showLoading(false); return@launch }
            if (events == null) { isLoading = false; showLoading(false); return@launch }

            eventListByEnterpriseId[enterpriseId] = events

            runCatching { updateEventDropdown(enterpriseId) }
                .onFailure { showError("UI error (events)") }

            isLoading = false
            showLoading(false)
        }
    }

    // ====== Dropdowns Material (remplacent les Spinners) ======
    private fun findPreselectIndex(options: List<LabeledOption>, keyOrLabel: String?): Int {
        if (keyOrLabel.isNullOrBlank()) return -1
        val needle = keyOrLabel.trim()
        val byKey = options.indexOfFirst { it.key == needle }
        if (byKey >= 0) return byKey
        return options.indexOfFirst { it.label.equals(needle, ignoreCase = true) }
    }

    private fun setupFixedGenderDropdown() {
        if (!isViewUsable()) return
        val ctx = binding.root.context

        val fixed = listOf(
            LabeledOption("female", getString(R.string.onboard_welcome_gender_female)),
            LabeledOption("male", getString(R.string.onboard_welcome_gender_male)),
            LabeledOption("other", getString(R.string.onboard_welcome_gender_other))
        )
        val labels = fixed.map { it.label }

        val view = binding.uiOnboardSpinnerGender as MaterialAutoCompleteTextView
        view.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_list_item_1, labels))

        // préselect exact (clé OU libellé)
        val pre = findPreselectIndex(fixed, genderKey)
        view.setText(if (pre >= 0) fixed[pre].label else "", /*filter*/ false)

        view.setOnItemClickListener { _, _, position, _ ->
            genderKey = fixed.getOrNull(position)?.key
            updateButtonNext()
        }
    }

    private fun setupHowDidYouHearDropdown(options: List<LabeledOption>) {
        if (!isViewUsable()) return
        val ctx = binding.root.context
        val labels = options.map { it.label }

        val view = binding.uiOnboardSpinnerHowDidYouHear as MaterialAutoCompleteTextView
        view.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_list_item_1, labels))

        val pre = findPreselectIndex(options, howDidYouHearKey)
        view.setText(if (pre >= 0) options[pre].label else "", false)

        view.setOnItemClickListener { _, _, position, _ ->
            howDidYouHearKey = options.getOrNull(position)?.key
            updateCompanyEventVisibility()
            updateButtonNext()
        }
    }

    private fun updateEnterpriseDropdown() {
        if (!isViewUsable()) return
        val ctx = binding.root.context
        val names = enterpriseList.map { it.Name }

        val view = binding.uiOnboardSpinnerCompany as MaterialAutoCompleteTextView
        view.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_list_item_1, names))

        // préselect par ID (company contient un Id)
        val preIndex = enterpriseList.indexOfFirst { it.Id == company }
        if (preIndex >= 0) {
            view.setText(enterpriseList[preIndex].Name, false)
            selectedEnterpriseId = enterpriseList[preIndex].Id
            // charge les events liés si pas déjà faits
            if (!eventListByEnterpriseId.containsKey(selectedEnterpriseId!!)) {
                loadEventsForEnterprise(selectedEnterpriseId!!)
            } else {
                updateEventDropdown(selectedEnterpriseId!!)
            }
        } else {
            view.setText("", false)
            selectedEnterpriseId = null
        }

        view.setOnItemClickListener { _, _, position, _ ->
            val selected = enterpriseList.getOrNull(position) ?: return@setOnItemClickListener
            selectedEnterpriseId = selected.Id
            company = selected.Id
            // on recharge la liste d’événements associée
            loadEventsForEnterprise(selected.Id)
            updateButtonNext()
        }
    }

    private fun updateEventDropdown(enterpriseId: String) {
        if (!isViewUsable()) return
        val ctx = binding.root.context
        val events = eventListByEnterpriseId[enterpriseId] ?: emptyList()
        val names = events.map { it.Name }

        val view = binding.uiOnboardSpinnerEvent as MaterialAutoCompleteTextView
        view.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_list_item_1, names))

        // préselect par Id (event contient un Id)
        val preIndex = events.indexOfFirst { it.Id == event }
        view.setText(if (preIndex >= 0) events[preIndex].Name else "", false)

        view.setOnItemClickListener { _, _, position, _ ->
            event = events.getOrNull(position)?.Id
            updateButtonNext()
        }
    }




    private fun clearDropdown(dropdown: MaterialAutoCompleteTextView) {
        dropdown.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, emptyList<String>()))
        dropdown.setText("", false)
    }

    private fun updateCompanyEventVisibility() {
        val isEnterprise = enterpriseModeKey != null && howDidYouHearKey == enterpriseModeKey
        binding.tilCompany.isVisible = isEnterprise
        binding.tilEvent.isVisible = isEnterprise
        if (!isEnterprise) {
            company = null
            event = null
            selectedEnterpriseId = null
            clearDropdown(binding.uiOnboardSpinnerCompany as MaterialAutoCompleteTextView)
            clearDropdown(binding.uiOnboardSpinnerEvent as MaterialAutoCompleteTextView)
        }
    }

    // ====== Date ======
    private fun showDatePicker() {
        if (!isViewUsable() || isDatePickerShowing) return
        isDatePickerShowing = true

        val ctx = requireContext()
        val cal = Calendar.getInstance()

        // Pré-remplir depuis le champ si au format dd/MM/yyyy
        binding.uiOnboardBirthdate.text?.toString()
            ?.takeIf { it.matches(Regex("""\d{2}/\d{2}/\d{4}""")) }
            ?.split("/")?.let { (dd, mm, yyyy) ->
                runCatching {
                    cal.set(yyyy.toInt(), mm.toInt() - 1, dd.toInt())
                }
            }

        // On crée le dialog SANS OnDateSetListener => on gère nous-mêmes l'auto-close
        val dlg = DatePickerDialog(
            ctx, /* listener = */ null,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        val dp = dlg.datePicker

        // --- MODE CALENDRIER : auto-close quand l'utilisateur tape un jour ---
        var calendarHooked = false
        try {
            val cv = dp.calendarView   // null si en mode "spinners"
            if (cv != null) {
                calendarHooked = true
                cv.setOnDateChangeListener { _, y, m, d ->
                    val newDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)
                    birthdate = newDate
                    safeUI {
                        binding.uiOnboardBirthdate.setText(newDate)
                        binding.uiOnboardBirthdate.clearFocus()
                        updateButtonNext()
                    }
                    dlg.dismiss()
                }
            }
        } catch (_: Throwable) { /* certains OEM plantent si pas en mode calendrier */ }

        // --- MODE SPINNERS : auto-close dès que la date change (1er changement) ---
        if (!calendarHooked) {
            var first = true
            dp.init(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ) { _, y, m, d ->
                if (first) { first = false; return@init } // ignore l'init
                val newDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)
                birthdate = newDate
                safeUI {
                    binding.uiOnboardBirthdate.setText(newDate)
                    binding.uiOnboardBirthdate.clearFocus()
                    updateButtonNext()
                }
                dlg.dismiss()
            }
        }

        // --- Fallback : si l'utilisateur appuie sur OK, on prend la valeur sélectionnée ---
        dlg.setOnShowListener {
            dlg.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val y = dp.year
                val m = dp.month
                val d = dp.dayOfMonth
                val newDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)
                birthdate = newDate
                safeUI {
                    binding.uiOnboardBirthdate.setText(newDate)
                    binding.uiOnboardBirthdate.clearFocus()
                    updateButtonNext()
                }
                dlg.dismiss()
            }
        }

        // Quel que soit le chemin (auto-close ou cancel), on libère le flag
        dlg.setOnDismissListener { isDatePickerShowing = false }
        dlg.show()
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

    // ====== Validation ======
    fun updateButtonNext() {
        if (!isViewUsable()) return
        val ok = checkAndValidateInput()

        // Gestion des erreurs de formulaire
        if (ok) {
            showErrorMessage(false)
            showCompanyEventError(false)
        } else {
            if (!binding.uiOnboardNamesEtFirstname.text.isNullOrEmpty() &&
                !binding.uiOnboardNamesEtLastname.text.isNullOrEmpty()
            ) {
                showErrorMessage(true)
                showCompanyEventError(true)
            }
        }

        // On notifie l'activité de l'état du bouton suivant
        callback?.updateButtonNext(ok)

        // On envoie les infos uniquement si tout est valide
        if (ok) {
            callback?.validateNames(
                binding.uiOnboardNamesEtFirstname.text.toString(),
                binding.uiOnboardNamesEtLastname.text.toString(),
                genderKey,
                formatBirthdateForAPI(binding.uiOnboardBirthdate.text?.toString()),
                binding.uiOnboardPhoneCcpCode.selectedCountry,
                binding.uiOnboardPhoneEtPhone.text?.toString(),
                binding.uiOnboardEmail.text?.toString(),
                binding.uiOnboardConsentCheck.isChecked,
                howDidYouHearKey,
                company,
                event
            )
        } else {
            callback?.validateNames(
                null, null, null, null, null, null, null,
                false, null, null, null
            )
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
        val needsCorp = enterpriseModeKey != null && howDidYouHearKey == enterpriseModeKey
        if (!needsCorp) return true
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
        binding.errorMessageFirstname.isVisible = show && !isValidFirstname()
        binding.errorMessageLastname.isVisible = show && !isValidLastname()
        binding.errorMessagePhone.isVisible = show && !isValidPhone()
        binding.errorMessageEmail.isVisible = show && !isValidEmail()
    }

    private fun showCompanyEventError(show: Boolean) {
        if (!isViewUsable()) return
        val needsCorp = enterpriseModeKey != null && howDidYouHearKey == enterpriseModeKey
        binding.errorMessageCompany.isVisible = show && company.isNullOrEmpty() && needsCorp
        binding.errorMessageEvent.isVisible = show && event.isNullOrEmpty() && needsCorp
    }

    private fun showLoading(show: Boolean) {
        if (!isViewUsable()) return
        // loader si besoin
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
