package social.entourage.android.enhanced_onboarding.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.databinding.FragmentEnhancedOnboardingAssoBinding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.profile.association.AssociationPresenter
import social.entourage.android.tools.log.AnalyticsEvents

class EnhancedOnboardingAssoFragment : Fragment() {

    private lateinit var binding: FragmentEnhancedOnboardingAssoBinding
    private lateinit var viewModel: OnboardingViewModel
    private val assoPresenter = AssociationPresenter()

    private var partnerId: Int? = null
    private var logoUri: Uri? = null
    private var initialDescription: String? = null
    private var userHasEditedDescription: Boolean = false
    private var userHasPickedLogo: Boolean = false
    private var lastLoadedPartnerId: Int? = null

    private var pendingDescription: String? = null
    private var pendingUploadKey: String? = null
    private var waitingUpload: Boolean = false
    private var waitingUpdate: Boolean = false

    private val pickLogoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            runCatching {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            logoUri = uri
            userHasPickedLogo = true
            Glide.with(this).load(uri).into(binding.ivLogo)
            saveDraft(logoUri = uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEnhancedOnboardingAssoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // On force l'affichage du bouton retour dans l'Activity
        viewModel.shouldDismissBtnBack.postValue(true)

        restoreDraftIntoUi()
        setupKeyboardHandling()

        binding.etDescription.doAfterTextChanged {
            userHasEditedDescription = true
            saveDraft(description = it?.toString())
        }

        binding.ivLogo.setOnClickListener { pickLogoLauncher.launch(arrayOf("image/*")) }
        binding.btnUploadLogo.setOnClickListener { pickLogoLauncher.launch(arrayOf("image/*")) }

        binding.buttonSkip.setOnClickListener {
            AnalyticsEvents.logEvent("onboarding_asso_skip_clic")
            clearDraft()
            viewModel.quitNow()
        }

        binding.buttonFinish.setOnClickListener {
            AnalyticsEvents.logEvent("onboarding_asso_finish_clic")
            onFinishClicked()
        }

        bindPresenter()
        loadMyAssociationIntoUi()
    }

    private fun setupKeyboardHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.scroll.setPadding(0, 0, 0, keyboardHeight)
            if (insets.isVisible(WindowInsetsCompat.Type.ime()) && binding.etDescription.hasFocus()) {
                binding.scroll.postDelayed({
                    binding.scroll.smoothScrollTo(0, binding.scroll.bottom)
                }, 100)
            }
            insets
        }
    }

    private fun bindPresenter() {
        assoPresenter.partner.observe(viewLifecycleOwner) { partner ->
            val desc = partner.description ?: ""
            val logoUrl = partner.largeLogoUrl ?: partner.smallLogoUrl ?: ""
            initialDescription = desc

            if (!userHasEditedDescription && getDraftDescription().isNullOrBlank()) {
                binding.etDescription.setText(desc)
            }
            if (!userHasPickedLogo && getDraftLogoUri() == null && logoUrl.isNotBlank()) {
                Glide.with(this).load(logoUrl).into(binding.ivLogo)
            }
        }

        assoPresenter.presignedUrl.observe(viewLifecycleOwner) { presigned ->
            val uri = logoUri ?: return@observe

            // Si pas de presigned url (erreur), on tente quand même de finir (peut-être juste update desc)
            if (presigned == null || presigned.presignedUrl == null) {
                waitingUpload = false
                tryComplete()
                return@observe
            }

            val bytes = readBytes(uri) ?: return@observe

            assoPresenter.uploadToPresignedUrl(presigned.presignedUrl, "image/jpeg", bytes) { ok ->
                waitingUpload = false
                if (ok) {
                    // CORRECTION : On extrait l'URL de base sans les paramètres de sécurité (?x-amz...)
                    // Cela donne une URL du type : https://.../uuid.jpeg
                    pendingUploadKey = presigned.presignedUrl.substringBefore("?")
                }
                tryComplete()
            }
        }

        assoPresenter.updatePartnerSuccess.observe(viewLifecycleOwner) { success ->
            waitingUpdate = false
            if (success == true) {
                clearDraft()
                viewModel.quitNow()
            }
        }
    }

    private fun loadMyAssociationIntoUi() {
        val me = EntourageApplication.me(requireContext())
        // CORRECTION : Accès direct à l'ID
        val id = me?.partner?.id?.toInt()
        if (id != null && id > 0 && id != lastLoadedPartnerId) {
            partnerId = id
            lastLoadedPartnerId = id
            assoPresenter.getPartnerInfos(id)
        }
    }

    private fun onFinishClicked() {
        val currentDesc = binding.etDescription.text?.toString()?.trim().orEmpty()
        // On ne met à jour que si ça a changé
        pendingDescription = if (currentDesc != initialDescription) currentDesc else null

        if (logoUri != null) {
            waitingUpload = true
            assoPresenter.getPresignedUploadUrl("image/jpeg")
        } else {
            tryComplete()
        }
    }

    private fun tryComplete() {
        val pid = partnerId ?: return
        if (waitingUpload || waitingUpdate) return

        // CORRECTION MAJEURE ICI : Plus de réflexion, appel direct au presenter
        val descToUpdate = pendingDescription
        val keyToUpdate = pendingUploadKey

        val hasChange = descToUpdate != null || keyToUpdate != null

        if (!hasChange) {
            viewModel.quitNow()
        } else {
            waitingUpdate = true
            // Le presenter va mettre keyToUpdate dans le champ "image_url"
            assoPresenter.updatePartner(pid, descToUpdate, keyToUpdate)
        }
    }

    // --- Draft Logic ---
    private fun saveDraft(description: String? = null, logoUri: Uri? = null) {
        draftPrefs().edit {
            putString(KEY_DRAFT_DESC, description ?: binding.etDescription.text?.toString())
            putString(KEY_DRAFT_LOGO_URI, (logoUri ?: this@EnhancedOnboardingAssoFragment.logoUri)?.toString())
        }
    }

    private fun restoreDraftIntoUi() {
        val d = draftPrefs().getString(KEY_DRAFT_DESC, null)
        val l = draftPrefs().getString(KEY_DRAFT_LOGO_URI, null)
        if (!d.isNullOrBlank()) {
            binding.etDescription.setText(d)
            userHasEditedDescription = true
        }
        l?.let {
            logoUri = Uri.parse(it)
            Glide.with(this).load(logoUri).into(binding.ivLogo)
            userHasPickedLogo = true
        }
    }

    private fun clearDraft() = draftPrefs().edit().clear().apply()
    private fun draftPrefs() = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private fun getDraftDescription() = draftPrefs().getString(KEY_DRAFT_DESC, null)
    private fun getDraftLogoUri() = draftPrefs().getString(KEY_DRAFT_LOGO_URI, null)?.let { Uri.parse(it) }

    // --- Helpers ---

    private fun readBytes(uri: Uri): ByteArray? = runCatching {
        requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
    }.getOrNull()

    // J'ai supprimé les méthodes readStringField, readIntField, setFieldIfExists car elles ne servent plus

    companion object {
        private const val PREFS_NAME = "enhanced_onboarding_asso_draft_prefs"
        private const val KEY_DRAFT_DESC = "enhanced_onboarding_asso_draft_desc"
        private const val KEY_DRAFT_LOGO_URI = "enhanced_onboarding_asso_draft_logo_uri"
    }
}