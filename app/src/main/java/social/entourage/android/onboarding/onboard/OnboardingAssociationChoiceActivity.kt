package social.entourage.android.onboarding.onboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.R
import social.entourage.android.databinding.ActivityOnboardingAssociationChoiceBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class OnboardingAssociationChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingAssociationChoiceBinding

    // Liste mockée (avec "Autre" en premier)
    private val associations = listOf(
        "Autre",
        "Croix-Rouge",
        "Emmaüs",
        "Fondation des femmes",
        "La Cravate Solidaire",
        "Secours Populaire",
        "Utopia 56"
    )
    private val firstItemAutre: String get() = associations.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingAssociationChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updatePaddingTopForEdgeToEdge(binding.rootScroll)

        setupTexts()
        setupDropdown()
        setupButtons()
    }

    // ----- UI text (si besoin d'i18n plus tard) -----
    private fun setupTexts() {
        binding.tvTitle.text = getString(R.string.onboard_asso_title)
        binding.tvSubtitle.text = getString(R.string.onboard_asso_subtitle)
        binding.dropdownAssoc.hint = getString(R.string.onboard_asso_dropdown_hint)

        // bulle orange cachée par défaut
        binding.infoPanel.visibility = View.GONE
        binding.inputOtherAssoc.error = null
        binding.inputOtherAssoc.editText?.setText("")
    }

    // ----- Dropdown -----
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

        // afficher la liste dès le focus
        actv.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as? AutoCompleteTextView)?.showDropDown()
        }

        // clic dans la bulle => focus sur l’edittext
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

    // ----- Boutons -----
    private fun setupButtons() {
        binding.buttonPrevious.setOnClickListener { finish() }

        binding.buttonNext.setOnClickListener {
            val assocName = getChosenAssociationOrError() ?: return@setOnClickListener
            val data = Intent().putExtra(EXTRA_ASSOC_NAME, assocName)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    // ----- Validation -----
    private fun getChosenAssociationOrError(): String? {
        val picked = (binding.dropdownAssoc.editText as? AutoCompleteTextView)
            ?.text?.toString()?.trim().orEmpty()

        if (picked.isEmpty()) {
            Toast.makeText(this, R.string.onboard_asso_pick_first, Toast.LENGTH_SHORT).show()
            return null
        }

        return if (picked.equals(firstItemAutre, ignoreCase = true)) {
            val custom = binding.inputOtherAssoc.editText?.text?.toString()?.trim().orEmpty()
            if (custom.isEmpty()) {
                binding.inputOtherAssoc.error = getString(R.string.onboard_asso_other_required)
                binding.inputOtherAssoc.requestFocus()
                null
            } else {
                binding.inputOtherAssoc.error = null
                custom
            }
        } else {
            picked
        }
    }

    // ----- Keyboard helpers -----
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
        const val EXTRA_ASSOC_NAME = "extra_assoc_name"
        fun newIntent(context: Context): Intent =
            Intent(context, OnboardingAssociationChoiceActivity::class.java)
    }
}
