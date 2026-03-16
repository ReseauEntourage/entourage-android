package social.entourage.android.onboarding.onboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import social.entourage.android.R
import social.entourage.android.databinding.ActivityOnboardingAssociationChoiceBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class OnboardingAssociationChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingAssociationChoiceBinding
    private val viewModel: AssociationViewModel by viewModels()

    private var associations: List<String> = listOf("Autre")
    private val firstItemAutre: String get() = associations.firstOrNull() ?: "Autre"

    private var initialAddress: String? = null
    private var initialLat: Double? = null
    private var initialLng: Double? = null
    private var initialPostalCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingAssociationChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        social.entourage.android.tools.log.AnalyticsEvents.logEvent(social.entourage.android.tools.log.AnalyticsEvents.View__Onboarding__AssoSearch)

        initialAddress = intent.getStringExtra(EXTRA_ADDRESS)
        initialLat = intent.getDoubleExtra(EXTRA_LAT, Double.NaN).takeIf { !it.isNaN() }
        initialLng = intent.getDoubleExtra(EXTRA_LNG, Double.NaN).takeIf { !it.isNaN() }
        initialPostalCode = intent.getStringExtra(EXTRA_POSTAL_CODE)

        updatePaddingTopForEdgeToEdge(binding.rootScroll)

        setupTexts()
        setupDropdown()
        setupButtons()
        bindViewModel()

        viewModel.loadAssociations()
    }

    private fun setupTexts() {
        binding.tvTitle.text = getString(R.string.onboard_asso_title)
        binding.tvSubtitle.text = getString(R.string.onboard_asso_subtitle)
        binding.dropdownAssoc.hint = getString(R.string.onboard_asso_dropdown_hint)

        binding.infoPanel.visibility = View.GONE
        binding.inputOtherAssoc.error = null
        binding.inputOtherAssoc.editText?.setText("")
    }

    private fun bindViewModel() {
        viewModel.loading.observe(this, Observer { isLoading ->
            val enabled = !isLoading
            binding.buttonPrevious.isEnabled = enabled
            binding.buttonNext.isEnabled = enabled
            binding.dropdownAssoc.isEnabled = enabled
            binding.inputOtherAssoc.isEnabled = enabled
        })

        viewModel.associationNames.observe(this, Observer { names ->
            associations = if (names.isNullOrEmpty()) listOf("Autre") else names
            val actv = binding.dropdownAssoc.editText as AutoCompleteTextView
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                associations
            )
            actv.setAdapter(adapter)
        })

        viewModel.errorMessageRes.observe(this, Observer { event ->
            val resId = event.getContentIfNotHandled() ?: return@Observer
            Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
        })

        viewModel.errorMessageText.observe(this, Observer { event ->
            val msg = event.getContentIfNotHandled() ?: return@Observer
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })

        viewModel.success.observe(this, Observer { event ->
            event.getContentIfNotHandled() ?: return@Observer
            startActivity(Intent(this, OnboardingEndActivity::class.java))
            finish()
        })
    }

    private fun setupDropdown() {
        val actv = binding.dropdownAssoc.editText as AutoCompleteTextView

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            associations
        )
        actv.setAdapter(adapter)
        actv.threshold = 0

        actv.setOnItemClickListener { _, _, position, _ ->
            val value = associations.getOrNull(position).orEmpty()
            onAssocPicked(value)
        }

        actv.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as? AutoCompleteTextView)?.showDropDown()
        }

        binding.infoPanel.setOnClickListener {
            binding.inputOtherAssoc.editText?.requestFocus()
            showKeyboard(binding.inputOtherAssoc.editText)
        }
    }

    private fun onAssocPicked(value: String) {
        val isOther = value.equals(firstItemAutre, ignoreCase = true)
        binding.infoPanel.visibility = if (isOther) View.VISIBLE else View.GONE
        binding.inputOtherAssoc.error = null

        if (!isOther) {
            binding.inputOtherAssoc.editText?.setText("")
            hideKeyboard(binding.inputOtherAssoc.editText)
        } else {
            showKeyboard(binding.inputOtherAssoc.editText)
        }
    }

    private fun setupButtons() {
        binding.buttonPrevious.setOnClickListener {
            social.entourage.android.tools.log.AnalyticsEvents.logEvent(social.entourage.android.tools.log.AnalyticsEvents.Clic__Back__Onboarding__AssoSearch)
            finish()
        }

        binding.buttonNext.setOnClickListener {
            social.entourage.android.tools.log.AnalyticsEvents.logEvent(social.entourage.android.tools.log.AnalyticsEvents.Clic__Next__Onboarding__AssoSearch)
            val picked = (binding.dropdownAssoc.editText as? AutoCompleteTextView)
                ?.text?.toString()?.trim().orEmpty()

            if (picked.isEmpty()) {
                Toast.makeText(this, R.string.onboard_asso_pick_first, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isOther = picked.equals(firstItemAutre, ignoreCase = true)

            if (isOther) {
                val custom = binding.inputOtherAssoc.editText?.text?.toString()?.trim().orEmpty()
                if (custom.isEmpty()) {
                    binding.inputOtherAssoc.error = getString(R.string.onboard_asso_other_required)
                    binding.inputOtherAssoc.requestFocus()
                    return@setOnClickListener
                }
                binding.inputOtherAssoc.error = null
                viewModel.createAssociation(
                    name = custom,
                    address = initialAddress,
                    latitude = initialLat,
                    longitude = initialLng,
                    postalCode = initialPostalCode
                )
            } else {
                val partner = viewModel.findPartnerByName(picked)
                if (partner == null) {
                    Toast.makeText(this, R.string.onboard_asso_pick_first, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.joinAssociation(
                    partner = partner,
                    postalCodeFallback = initialPostalCode
                )
            }
        }
    }

    private fun showKeyboard(target: View?) {
        target ?: return
        target.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(target: View?) {
        target ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(target.windowToken, 0)
    }

    companion object {
        private const val EXTRA_ADDRESS = "extra_address"
        private const val EXTRA_LAT = "extra_lat"
        private const val EXTRA_LNG = "extra_lng"
        private const val EXTRA_POSTAL_CODE = "extra_postal_code"

        fun newIntent(
            context: Context,
            address: String?,
            lat: Double?,
            lng: Double?,
            postalCode: String?
        ): Intent {
            val i = Intent(context, OnboardingAssociationChoiceActivity::class.java)
            if (!address.isNullOrBlank()) i.putExtra(EXTRA_ADDRESS, address)
            if (lat != null) i.putExtra(EXTRA_LAT, lat)
            if (lng != null) i.putExtra(EXTRA_LNG, lng)
            if (!postalCode.isNullOrBlank()) i.putExtra(EXTRA_POSTAL_CODE, postalCode)
            return i
        }
    }
}
