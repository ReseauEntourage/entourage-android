package social.entourage.android.enhanced_onboarding.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.R
import social.entourage.android.databinding.FragmentEnhancedOnboardingAssoBinding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * Fragment de fin d'onboarding spécifique pour les profils « association ».
 *
 * Cette étape permet à l'organisation de présenter brièvement sa structure
 * en ajoutant un logo et une description. Les deux champs sont optionnels.
 */
class EnhancedOnboardingAssoFragment : Fragment() {

    private lateinit var binding: FragmentEnhancedOnboardingAssoBinding
    private lateinit var viewModel: OnboardingViewModel
    private var logoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEnhancedOnboardingAssoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Définition des textes pour cette étape. Les chaînes sont codées en dur ici car
        // les ressources ne sont pas nécessairement disponibles dans ce module.
        binding.tvTitle.text = "Présentez votre association à la communauté"
        binding.tvSubtitle.text = "Ajoutez votre logo et une courte description pour que les membres de la communauté sachent qui vous êtes et ce que vous faites."
        binding.tvLabelDescription.text = "Description de votre association"
        binding.etDescription.hint = "Ex. : Association de quartier œuvrant pour le lien social via des événements ouverts à tous."

        // Gestion du clic sur le bouton d'upload du logo
        binding.btnUploadLogo.setOnClickListener {
            // Lancement d'une galerie pour sélectionner une image
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickIntent, PICK_IMAGE_REQUEST)
        }

        // Bouton "Plus tard" : aucune action supplémentaire, on termine l'onboarding
        binding.buttonSkip.setOnClickListener {
            // Signalement d'un clic sur "Plus tard" pour les statistiques
            AnalyticsEvents.logEvent("onboarding_asso_skip_clic")
            // Fin de l'onboarding sans enregistrer de données supplémentaires
            viewModel.registerAndQuit()
        }

        // Bouton "Terminer" : on sauvegarde les informations optionnelles et on enregistre
        binding.buttonFinish.setOnClickListener {
            // Signalement d'un clic sur "Terminer" pour les statistiques
            AnalyticsEvents.logEvent("onboarding_asso_finish_clic")
            // Stockage de la description dans le champ "about" de l'utilisateur (si renseignée)
            val description = binding.etDescription.text?.toString()?.trim()
            if (!description.isNullOrEmpty()) {
                viewModel.user?.about = description
            }
            // TODO : lors de l'intégration serveur, envoyer le logoUri si nécessaire
            viewModel.registerAndQuit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                logoUri = uri
                binding.ivLogo.setImageURI(uri)
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }
}