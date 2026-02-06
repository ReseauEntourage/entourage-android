package social.entourage.android.enhanced_onboarding.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import social.entourage.android.EntourageApplication
import social.entourage.android.databinding.FragmentEnhancedOnboardingAssoBinding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.profile.association.AssociationPresenter
import social.entourage.android.tools.log.AnalyticsEvents

class EnhancedOnboardingAssoFragment : Fragment() {

    private lateinit var binding: FragmentEnhancedOnboardingAssoBinding
    private lateinit var viewModel: OnboardingViewModel
    private val assoPresenter = AssociationPresenter()

    // L'ID du partenaire chargé
    private var partnerId: Int? = null
    // L'URI locale si l'utilisateur change l'image (sinon null)
    private var newLogoUri: Uri? = null
    // La description initiale pour vérifier s'il y a eu modif
    private var initialDescription: String? = null

    // Drapeaux pour éviter les doubles clics pendant l'upload
    private var isSaving: Boolean = false

    private val pickLogoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult

            // On garde les droits de lecture (nécessaire sur certains appareils)
            runCatching {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            // On stocke la nouvelle URI et on l'affiche direct
            newLogoUri = uri
            // AJOUT ICI : .transform(CircleCrop()) pour arrondir l'image locale choisie
            Glide.with(this)
                .load(uri)
                .transform(CircleCrop())
                .into(binding.ivLogo)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEnhancedOnboardingAssoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.View__EnhancedOnboardingAssoFragment)

        viewModel.shouldDismissBtnBack.postValue(true)
        setupKeyboardHandling()

        // --- Listeners UI ---
        binding.ivLogo.setOnClickListener { pickImage() }
        binding.btnUploadLogo.setOnClickListener { pickImage() }

        binding.buttonSkip.setOnClickListener {
            AnalyticsEvents.logEvent("onboarding_asso_skip_clic")
            viewModel.quitNow()
        }

        binding.buttonFinish.setOnClickListener {
            AnalyticsEvents.logEvent("onboarding_asso_finish_clic")
            onFinishClicked()
        }

        // --- Chargement des données ---
        bindPresenter()
        loadMyAssociationIntoUi()
    }

    private fun pickImage() {
        pickLogoLauncher.launch(arrayOf("image/*"))
    }

    // --- CORRECTION ICI : On a enlevé le setPadding ---
    private fun setupKeyboardHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            // On vérifie juste si le clavier est visible pour scroller en bas
            // On ne touche plus au padding du scrollview
            if (insets.isVisible(WindowInsetsCompat.Type.ime()) && binding.etDescription.hasFocus()) {
                binding.scroll.postDelayed({
                    // On scroll tout en bas pour être sûr que le curseur est visible
                    binding.scroll.smoothScrollTo(0, binding.scroll.bottom)
                }, 100)
            }
            insets
        }
    }

    private fun bindPresenter() {
        // 1. Réception des infos de l'asso
        assoPresenter.partner.observe(viewLifecycleOwner) { partner ->
            if (partner == null) return@observe

            val desc = partner.description ?: ""
            initialDescription = desc

            // On remplit la description si le champ est vide (premier chargement)
            if (binding.etDescription.text.isNullOrBlank()) {
                binding.etDescription.setText(desc)
            }

            // On affiche l'image distante SEULEMENT si l'utilisateur n'en a pas déjà choisi une nouvelle localement
            if (newLogoUri == null) {
                // Priorité à imageUrl, puis fallback sur large/small
                val remoteUrl = partner.imageUrl ?: partner.largeLogoUrl ?: partner.smallLogoUrl

                if (!remoteUrl.isNullOrBlank()) {
                    // AJOUT ICI : .transform(CircleCrop()) pour arrondir l'image distante
                    Glide.with(this)
                        .load(remoteUrl)
                        .transform(CircleCrop())
                        .into(binding.ivLogo)
                }
                // Sinon, le placeholder du XML reste affiché par défaut
            }
        }

        // 2. Réception de l'URL pour uploader l'image S3
        assoPresenter.presignedUrl.observe(viewLifecycleOwner) { presigned ->
            val uriToUpload = newLogoUri

            // Cas d'erreur ou d'annulation
            if (presigned?.presignedUrl == null || uriToUpload == null) {
                isSaving = false
                // On tente quand même de sauvegarder la description s'il y a erreur image
                finalizeUpdate(null)
                return@observe
            }

            val bytes = readBytes(uriToUpload)
            if (bytes == null) {
                isSaving = false
                return@observe
            }

            // Upload effectif vers S3
            assoPresenter.uploadToPresignedUrl(presigned.presignedUrl, "image/jpeg", bytes) { success ->
                if (success) {
                    // Si succès, on a la clé (presigned.uploadKey) qu'on passera au backend
                    finalizeUpdate(presigned.uploadKey)
                } else {
                    isSaving = false
                    // Erreur upload : on sauvegarde au moins la description
                    finalizeUpdate(null)
                }
            }
        }

        // 3. Succès de la mise à jour finale
        assoPresenter.updatePartnerSuccess.observe(viewLifecycleOwner) { success ->
            isSaving = false
            if (success == true) {
                viewModel.quitNow()
            }
        }
    }

    private fun loadMyAssociationIntoUi() {
        val me = EntourageApplication.me(requireContext())
        val id = me?.partner?.id?.toInt()
        if (id != null && id > 0) {
            partnerId = id
            assoPresenter.getPartnerInfos(id)
        }
    }

    private fun onFinishClicked() {
        if (isSaving) return

        val currentDesc = binding.etDescription.text?.toString()?.trim().orEmpty()
        val descHasChanged = currentDesc != initialDescription
        val hasNewImage = newLogoUri != null

        // Si rien n'a changé, on sort direct
        if (!descHasChanged && !hasNewImage) {
            viewModel.quitNow()
            return
        }

        isSaving = true

        if (hasNewImage) {
            // S'il y a une image, on demande l'URL d'upload -> upload S3 -> updatePartner avec la clé
            assoPresenter.getPresignedUploadUrl("image/jpeg")
        } else {
            // Sinon on met juste à jour le texte
            finalizeUpdate(null)
        }
    }

    private fun finalizeUpdate(uploadedImageKey: String?) {
        val pid = partnerId ?: return
        val currentDesc = binding.etDescription.text?.toString()?.trim().orEmpty()

        // Le Presenter mappera 'uploadedImageKey' vers le champ "image_url" (qui attend la clé)
        assoPresenter.updatePartner(pid, currentDesc, uploadedImageKey)
    }

    private fun readBytes(uri: Uri): ByteArray? = runCatching {
        requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
    }.getOrNull()
}