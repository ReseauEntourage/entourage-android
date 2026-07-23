package social.entourage.android.onboarding.onboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.Partner
import social.entourage.android.databinding.ActivityPartnerOnboardingBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class PartnerOnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartnerOnboardingBinding

    private val partners = ArrayList<Partner>()
    private lateinit var adapter: ArrayAdapter<String>

    private var selectedPartner: Partner? = null

    private var baseAddress: String? = null
    private var baseLat: Double? = null
    private var baseLng: Double? = null

    private val otherLabel by lazy {
        getString(R.string.onboarding_partner_other_option) // "Autre"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartnerOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupérer les infos de localisation passées depuis OnboardingZoneChoiceActivity
        baseAddress = intent.getStringExtra(EXTRA_BASE_ADDRESS)
        baseLat = intent.getDoubleExtra(EXTRA_BASE_LAT, Double.NaN).let {
            if (it.isNaN()) null else it
        }
        baseLng = intent.getDoubleExtra(EXTRA_BASE_LNG, Double.NaN).let {
            if (it.isNaN()) null else it
        }

        setupUi()
        setupAutocomplete()
        setupButtons()
        loadPartners()
    }

    // ------------------------------------------------------------------------
    // UI
    // ------------------------------------------------------------------------

    private fun setupUi() {
        updatePaddingTopForEdgeToEdge(binding.layoutPartnerOnboarding)

        binding.tvTitle.text = getString(R.string.onboarding_partner_title)
        binding.tvSubtitle.text = getString(R.string.onboarding_partner_subtitle)

        showNewPartnerSection(false)
    }

    private fun setupAutocomplete() {
        val actv: AutoCompleteTextView = binding.autoCompletePartnerName

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )

        actv.threshold = 0
        actv.setAdapter(adapter)

        // Ouvre la liste quand on clique dans le champ
        actv.setOnClickListener {
            if (!actv.isPopupShowing && adapter.count > 0) {
                actv.showDropDown()
            }
        }

        // Ouvre la liste quand on prend le focus
        actv.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !actv.isPopupShowing && adapter.count > 0) {
                actv.post { actv.showDropDown() }
            }
        }

        actv.setOnItemClickListener { _, _, position, _ ->
            val label = adapter.getItem(position) ?: return@setOnItemClickListener
            handleSelection(label)
        }
    }

    private fun setupButtons() {
        binding.buttonPrevious.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonNext.setOnClickListener {
            onValidate()
        }
    }

    // ------------------------------------------------------------------------
    // Data
    // ------------------------------------------------------------------------

    private fun loadPartners() {
        OnboardingAPI.getInstance().getAssociationsList { list ->
            runOnUiThread {
                partners.clear()
                list?.let {
                    partners.addAll(it.sortedBy { p -> p.name?.lowercase() })
                }

                val labels = mutableListOf<String>()
                labels.add(otherLabel) // toujours en tête
                labels.addAll(partners.map { it.name.orEmpty() })

                adapter.clear()
                adapter.addAll(labels)
                adapter.notifyDataSetChanged()

                binding.autoCompletePartnerName.setText("", false)
                selectedPartner = null
                showNewPartnerSection(false)
            }
        }
    }

    private fun handleSelection(label: String) {
        val actv = binding.autoCompletePartnerName

        if (label == otherLabel) {
            selectedPartner = null
            showNewPartnerSection(true)
        } else {
            selectedPartner = partners.firstOrNull { it.name == label }
            showNewPartnerSection(false)
        }

        actv.setText(label, false)
        actv.setSelection(label.length)

        // Fermer le clavier si besoin
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(actv.windowToken, 0)
    }

    private fun showNewPartnerSection(show: Boolean) {
        binding.layoutNewPartnerContainer.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    // ------------------------------------------------------------------------
    // Validation / appels API
    // ------------------------------------------------------------------------

    private fun onValidate() {
        val currentLabel =
            binding.autoCompletePartnerName.text?.toString()?.trim().orEmpty()

        if (currentLabel.isEmpty()) {
            Toast.makeText(
                this,
                R.string.onboarding_partner_error_select,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (currentLabel == otherLabel) {
            val customName =
                binding.editNewPartnerName.text?.toString()?.trim().orEmpty()
            if (customName.isEmpty()) {
                Toast.makeText(
                    this,
                    R.string.onboarding_partner_error_custom_name,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            createAndJoinNewPartner(customName)
        } else {
            val partner = partners.firstOrNull { it.name == currentLabel }
            if (partner == null) {
                Toast.makeText(
                    this,
                    R.string.onboarding_partner_error_select,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            joinExistingPartner(partner)
        }
    }

    /**
     * Cas 1 : l'utilisateur choisit une asso existante
     * -> on appelle updateAssoInfos pour faire le join_request (partners/join_request).
     */
    private fun joinExistingPartner(partner: Partner) {
        OnboardingAPI.getInstance().updateAssoInfos(partner) { isOK, _: ResponseBody? ->
            runOnUiThread {
                if (isOK) {
                    goToEnd()
                } else {
                    Toast.makeText(
                        this,
                        R.string.user_action_asso_send_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Cas 2 : l'utilisateur crée une nouvelle asso
     * -> /partners crée l'asso
     * -> puis /partners/join_request via updateAssoInfos(createdPartner)
     */
    private fun createAndJoinNewPartner(name: String) {
        // petite sécurité : si on n’a pas de coordonnées, le back va refuser de toute façon
        if (baseLat == null || baseLng == null || baseAddress.isNullOrBlank()) {
            Toast.makeText(
                this,
                R.string.onboarding_partner_error_missing_location,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        OnboardingAPI.getInstance().createPartner(
            name = name,
            address = baseAddress,
            latitude = baseLat,
            longitude = baseLng
        ) { ok, created ->
            runOnUiThread {
                if (!ok || created == null) {
                    Toast.makeText(
                        this,
                        R.string.user_action_asso_send_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // On a maintenant un Partner avec un id
                    // -> on fait le join_request exactement comme pour une asso existante
                    OnboardingAPI.getInstance().updateAssoInfos(created) { isOK, _ ->
                        runOnUiThread {
                            if (isOK) {
                                goToEnd()
                            } else {
                                Toast.makeText(
                                    this,
                                    R.string.user_action_asso_send_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun goToEnd() {
        startActivity(Intent(this, OnboardingEndActivity::class.java))
        finish()
    }

    // ------------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------------

    companion object {
        private const val EXTRA_BASE_ADDRESS = "extra_base_address"
        private const val EXTRA_BASE_LAT = "extra_base_lat"
        private const val EXTRA_BASE_LNG = "extra_base_lng"

        fun newIntent(
            context: Context,
            address: String?,
            lat: Double?,
            lng: Double?
        ): Intent {
            return Intent(context, PartnerOnboardingActivity::class.java).apply {
                putExtra(EXTRA_BASE_ADDRESS, address)
                lat?.let { putExtra(EXTRA_BASE_LAT, it) }
                lng?.let { putExtra(EXTRA_BASE_LNG, it) }
            }
        }
    }
}
