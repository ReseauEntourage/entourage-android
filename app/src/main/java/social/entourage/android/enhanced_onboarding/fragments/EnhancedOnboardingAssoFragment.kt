package social.entourage.android.enhanced_onboarding.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
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
    private var initialLogoUrl: String? = null

    private var pendingDescription: String? = null
    private var pendingUploadKey: String? = null
    private var waitingUpload: Boolean = false
    private var waitingUpdate: Boolean = false

    private var userHasEditedDescription: Boolean = false
    private var userHasPickedLogo: Boolean = false

    private var lastLoadedPartnerId: Int? = null

    private val pickLogoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult

            runCatching {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            logoUri = uri
            userHasPickedLogo = true

            Glide.with(this).load(uri).into(binding.ivLogo)
            saveDraft(logoUri = uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreDraftIntoMemory()
        if (!getDraftDescription().isNullOrBlank()) userHasEditedDescription = true
        if (getDraftLogoUri() != null) userHasPickedLogo = true
        logoUri = getDraftLogoUri()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnhancedOnboardingAssoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreDraftIntoUi()

        binding.etDescription.setOnTouchListener { v, event ->
            v.parent?.requestDisallowInterceptTouchEvent(true)
            if (event.actionMasked == android.view.MotionEvent.ACTION_UP ||
                event.actionMasked == android.view.MotionEvent.ACTION_CANCEL
            ) {
                v.parent?.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        binding.etDescription.doAfterTextChanged {
            userHasEditedDescription = true
            saveDraft(description = it?.toString())
        }

        bindPresenter()

        binding.btnUploadLogo.setOnClickListener {
            pickLogoLauncher.launch(arrayOf("image/*"))
        }

        binding.buttonSkip.setOnClickListener {
            AnalyticsEvents.logEvent("onboarding_asso_skip_clic")
            clearDraft()
            viewModel.registerAndQuit()
        }

        binding.buttonFinish.setOnClickListener {
            AnalyticsEvents.logEvent("onboarding_asso_finish_clic")
            onFinishClicked()
        }

        loadMyAssociationIntoUi()
    }

    override fun onPause() {
        saveDraft(description = binding.etDescription.text?.toString(), logoUri = logoUri)
        super.onPause()
    }

    private fun bindPresenter() {
        assoPresenter.partner.observe(viewLifecycleOwner) { partner ->
            val desc = readStringField(partner, "about")
                ?: readStringField(partner, "description")
                ?: ""

            val logoUrl = readStringField(partner, "logoUrl")
                ?: readStringField(partner, "logo_url")
                ?: readStringField(partner, "avatarUrl")
                ?: readStringField(partner, "avatar_url")
                ?: readStringField(partner, "imageUrl")
                ?: readStringField(partner, "image_url")

            initialDescription = desc
            initialLogoUrl = logoUrl

            val hasDraftDesc = !getDraftDescription().isNullOrBlank()
            val hasDraftLogo = getDraftLogoUri() != null

            if (!userHasEditedDescription && !hasDraftDesc && binding.etDescription.text?.toString().isNullOrBlank()) {
                binding.etDescription.setText(desc)
            }

            if (!userHasPickedLogo && !hasDraftLogo && logoUri == null && !logoUrl.isNullOrBlank()) {
                Glide.with(this).load(logoUrl).into(binding.ivLogo)
            }
        }

        assoPresenter.presignedUrl.observe(viewLifecycleOwner) { presigned ->
            if (presigned == null) {
                waitingUpload = false
                tryComplete()
                return@observe
            }

            val uri = logoUri
            if (uri == null) {
                waitingUpload = false
                tryComplete()
                return@observe
            }

            val presignedUrl = presigned.presignedUrl
            val uploadKey = presigned.uploadKey

            if (presignedUrl.isNullOrBlank() || uploadKey.isNullOrBlank()) {
                waitingUpload = false
                tryComplete()
                return@observe
            }

            val ct = resolveContentType(uri) ?: "image/jpeg"
            val bytes = readBytes(uri)

            if (bytes == null || bytes.isEmpty()) {
                waitingUpload = false
                tryComplete()
                return@observe
            }

            assoPresenter.uploadToPresignedUrl(
                uploadUrl = presignedUrl,
                contentType = ct,
                bytes = bytes
            ) { ok ->
                waitingUpload = false
                if (ok) {
                    pendingUploadKey = uploadKey
                }
                tryComplete()
            }
        }

        assoPresenter.updatePartnerSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                waitingUpdate = false
                clearDraft()
                viewModel.registerAndQuit()
            }
        }
    }

    private fun loadMyAssociationIntoUi() {
        val me = activity?.let { EntourageApplication.me(it) }
        val partner = me?.partner
        val id = readIntField(partner, "id")
            ?: readIntField(partner, "partnerId")
            ?: readIntField(partner, "partner_id")

        if (id != null && id > 0) {
            partnerId = id
            if (lastLoadedPartnerId == id) return
            lastLoadedPartnerId = id
            assoPresenter.getPartnerInfos(id)
        }
    }

    private fun onFinishClicked() {
        val pid = partnerId
        if (pid == null || pid <= 0) {
            val description = binding.etDescription.text?.toString()?.trim()
            if (!description.isNullOrEmpty()) {
                viewModel.user?.about = description
            }
            clearDraft()
            viewModel.registerAndQuit()
            return
        }

        val currentDesc = binding.etDescription.text?.toString()?.trim().orEmpty()
        val descChanged = currentDesc != (initialDescription ?: "")

        pendingDescription = if (descChanged) currentDesc else null

        val hasNewLogo = logoUri != null
        if (hasNewLogo) {
            waitingUpload = true
            val ct = resolveContentType(logoUri!!) ?: "image/jpeg"
            assoPresenter.getPresignedUploadUrl(ct)
        } else {
            tryComplete()
        }
    }

    private fun tryComplete() {
        val pid = partnerId ?: return

        if (waitingUpload) return
        if (waitingUpdate) return

        val needsDesc = !pendingDescription.isNullOrEmpty()
        val needsLogo = logoUri != null

        if (!needsDesc && !needsLogo) {
            clearDraft()
            viewModel.registerAndQuit()
            return
        }

        if (needsLogo && pendingUploadKey.isNullOrBlank()) {
            clearDraft()
            viewModel.registerAndQuit()
            return
        }

        val updateData = assoPresenter.newPartnerUpdateData()

        if (needsDesc) {
            setFieldIfExists(updateData, "about", pendingDescription)
            setFieldIfExists(updateData, "description", pendingDescription)
        }

        if (needsLogo) {
            val key = pendingUploadKey
            setFieldIfExists(updateData, "logoKey", key)
            setFieldIfExists(updateData, "logo_key", key)
            setFieldIfExists(updateData, "imageKey", key)
            setFieldIfExists(updateData, "image_key", key)
            setFieldIfExists(updateData, "avatarKey", key)
            setFieldIfExists(updateData, "avatar_key", key)
            setFieldIfExists(updateData, "uploadKey", key)
            setFieldIfExists(updateData, "upload_key", key)
        }

        waitingUpdate = true
        assoPresenter.updatePartner(pid, updateData)
    }

    private fun restoreDraftIntoMemory() {
        val desc = draftPrefs().getString(KEY_DRAFT_DESC, null)
        val logo = draftPrefs().getString(KEY_DRAFT_LOGO_URI, null)
        cachedDraftDesc = desc
        cachedDraftLogo = logo?.let { runCatching { Uri.parse(it) }.getOrNull() }
    }

    private fun restoreDraftIntoUi() {
        val draftDesc = getDraftDescription()
        if (!draftDesc.isNullOrBlank()) {
            if (binding.etDescription.text?.toString().isNullOrBlank()) {
                binding.etDescription.setText(draftDesc)
            }
            userHasEditedDescription = true
        }

        val draftLogo = getDraftLogoUri()
        if (draftLogo != null) {
            logoUri = draftLogo
            runCatching {
                Glide.with(this).load(draftLogo).into(binding.ivLogo)
            }.onFailure {
                logoUri = null
                saveDraft(logoUri = null)
            }
            userHasPickedLogo = logoUri != null
        }
    }

    private fun saveDraft(description: String? = null, logoUri: Uri? = null) {
        val d = description ?: binding.etDescription.text?.toString()
        val l = logoUri ?: this.logoUri
        cachedDraftDesc = d
        cachedDraftLogo = l
        draftPrefs().edit {
            putString(KEY_DRAFT_DESC, d)
            putString(KEY_DRAFT_LOGO_URI, l?.toString())
        }
    }

    private fun clearDraft() {
        cachedDraftDesc = null
        cachedDraftLogo = null
        draftPrefs().edit {
            remove(KEY_DRAFT_DESC)
            remove(KEY_DRAFT_LOGO_URI)
        }
    }

    private fun getDraftDescription(): String? = cachedDraftDesc
    private fun getDraftLogoUri(): Uri? = cachedDraftLogo

    private fun draftPrefs() =
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun resolveContentType(uri: Uri): String? {
        return try {
            requireContext().contentResolver.getType(uri)
        } catch (_: Throwable) {
            null
        }
    }

    private fun readBytes(uri: Uri): ByteArray? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Throwable) {
            null
        }
    }

    private fun readStringField(obj: Any?, fieldName: String): String? {
        if (obj == null) return null
        return try {
            val f = obj.javaClass.getDeclaredField(fieldName)
            f.isAccessible = true
            (f.get(obj) as? String)
        } catch (_: Throwable) {
            try {
                val getter = "get" + fieldName.replaceFirstChar { it.uppercase() }
                val m = obj.javaClass.methods.firstOrNull { it.name == getter && it.parameterTypes.isEmpty() }
                (m?.invoke(obj) as? String)
            } catch (_: Throwable) {
                null
            }
        }
    }

    private fun readIntField(obj: Any?, fieldName: String): Int? {
        if (obj == null) return null
        return try {
            val f = obj.javaClass.getDeclaredField(fieldName)
            f.isAccessible = true
            val v = f.get(obj)
            when (v) {
                is Int -> v
                is Long -> v.toInt()
                is String -> v.toIntOrNull()
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun setFieldIfExists(obj: Any, fieldName: String, value: Any?) {
        try {
            val f = obj.javaClass.getDeclaredField(fieldName)
            f.isAccessible = true
            f.set(obj, value)
            return
        } catch (_: Throwable) {
        }

        try {
            val setterName = "set" + fieldName.replaceFirstChar { it.uppercase() }
            val m = obj.javaClass.methods.firstOrNull { it.name == setterName && it.parameterTypes.size == 1 }
            if (m != null) {
                m.invoke(obj, value)
            }
        } catch (_: Throwable) {
        }
    }

    companion object {
        private const val PREFS_NAME = "enhanced_onboarding_asso_draft_prefs"
        private const val KEY_DRAFT_DESC = "enhanced_onboarding_asso_draft_desc"
        private const val KEY_DRAFT_LOGO_URI = "enhanced_onboarding_asso_draft_logo_uri"

        private var cachedDraftDesc: String? = null
        private var cachedDraftLogo: Uri? = null
    }
}
