package social.entourage.android.suggestions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.R
import social.entourage.android.enhanced_onboarding.OnboardingViewModel

/**
 * Fragment d'onboarding permettant à l'utilisateur de choisir son type de profil
 * en lien avec la feature Suggestions.
 *
 * INJECTION DANS LE FLUX ONBOARDING :
 * Pour intégrer ce fragment dans le flux existant (OnboardingStartActivity),
 * il faudrait :
 * 1. Incrémenter `numberOfSteps` dans OnboardingStartActivity (de 3 à 4 ou 5)
 * 2. Ajouter un case dans `changeFragment()` pour afficher ce fragment à l'étape souhaitée
 * 3. Implémenter l'interface OnboardingStartCallback ou en créer une dédiée
 * 4. Stocker le type choisi dans temporaryUser ou un champ dédié
 * 5. L'envoyer via callSignup() ou un appel API séparé
 *
 * Exemple de modification dans changeFragment() :
 *   4 -> showFragment(OnboardingTypeFragment.newInstance())
 */
class OnboardingTypeFragment : Fragment() {

    interface OnTypeSelectedListener {
        fun onTypeSelected(type: String)
    }

    private var listener: OnTypeSelectedListener? = null

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        listener = context as? OnTypeSelectedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        // Masque le bouton retour : c'est la première étape du flux
        val vm = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        vm.toggleBtnBack(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val padding = (24 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(android.graphics.Color.parseColor("#FFF8F6"))
        }

        val title = TextView(requireContext()).apply {
            text = "Qui êtes-vous ?"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = (24 * resources.displayMetrics.density).toInt()
            layoutParams = params
        }
        root.addView(title)

        val types = listOf(
            "neighbour" to "Je cherche du lien dans mon quartier",
            "volunteer" to "Je veux aider",
            "association" to "Je représente une association"
        )

        types.forEach { (key, label) ->
            val btn = Button(requireContext()).apply {
                text = label
                setBackgroundColor(android.graphics.Color.parseColor("#FF9739"))
                setTextColor(android.graphics.Color.WHITE)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (48 * resources.displayMetrics.density).toInt()
                )
                params.bottomMargin = (12 * resources.displayMetrics.density).toInt()
                layoutParams = params
                setOnClickListener { listener?.onTypeSelected(key) }
            }
            root.addView(btn)
        }

        return root
    }

    companion object {
        fun newInstance() = OnboardingTypeFragment()
    }
}
