package social.entourage.android.suggestions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.enhanced_onboarding.OnboardingViewModel

/**
 * Fragment d'onboarding — première étape de personnalisation pour les suggestions.
 * Collecte les préférences initiales de l'utilisateur (centre d'intérêt, distance).
 *
 * INJECTION DANS LE FLUX ONBOARDING :
 * Pour intégrer ce fragment dans OnboardingStartActivity :
 * 1. L'ajouter comme étape supplémentaire dans changeFragment() après OnboardingTypeFragment
 * 2. Récupérer les préférences via le callback OnboardingStartCallback (ajouter la méthode)
 * 3. Les transmettre au backend via callSignup() ou un appel PATCH /users/me séparé
 *
 * Exemple :
 *   5 -> showFragment(OnboardingFirstStepFragment.newInstance())
 *
 * Le UserRequest.updateUser() existant (PATCH users/{userId}) peut être réutilisé
 * pour envoyer les préférences après inscription.
 */
class OnboardingFirstStepFragment : Fragment() {

    interface OnFirstStepCompleteListener {
        fun onFirstStepComplete(interests: List<String>, maxDistanceKm: Int)
    }

    private var listener: OnFirstStepCompleteListener? = null

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        listener = context as? OnFirstStepCompleteListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        // Affiche le bouton retour : l'utilisateur peut revenir à l'étape précédente
        val vm = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        vm.toggleBtnBack(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dp = resources.displayMetrics.density
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val p = (24 * dp).toInt()
            setPadding(p, p, p, p)
            setBackgroundColor(android.graphics.Color.parseColor("#FFF8F6"))
        }

        val title = TextView(requireContext()).apply {
            text = "Vos centres d'intérêt"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (8 * dp).toInt()
            layoutParams = lp
        }
        root.addView(title)

        val subtitle = TextView(requireContext()).apply {
            text = "Sélectionnez ce qui vous correspond pour recevoir des suggestions adaptées."
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (24 * dp).toInt()
            layoutParams = lp
        }
        root.addView(subtitle)

        val selectedInterests = mutableSetOf<String>()
        val interestOptions = listOf(
            "sport" to "Sport",
            "culture" to "Culture",
            "cuisine" to "Cuisine",
            "nature" to "Nature",
            "bien-etre" to "Bien-être",
            "animaux" to "Animaux"
        )

        interestOptions.forEach { (key, label) ->
            val btn = Button(requireContext()).apply {
                text = label
                tag = key
                setBackgroundColor(android.graphics.Color.parseColor("#FEEAE3"))
                setTextColor(android.graphics.Color.parseColor("#FF9739"))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (44 * dp).toInt()
                )
                lp.bottomMargin = (8 * dp).toInt()
                layoutParams = lp
                setOnClickListener {
                    if (selectedInterests.contains(key)) {
                        selectedInterests.remove(key)
                        setBackgroundColor(android.graphics.Color.parseColor("#FEEAE3"))
                        setTextColor(android.graphics.Color.parseColor("#FF9739"))
                    } else {
                        selectedInterests.add(key)
                        setBackgroundColor(android.graphics.Color.parseColor("#FF9739"))
                        setTextColor(android.graphics.Color.WHITE)
                    }
                }
            }
            root.addView(btn)
        }

        val distanceLabel = TextView(requireContext()).apply {
            text = "Distance maximale (km)"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = (16 * dp).toInt()
            lp.bottomMargin = (8 * dp).toInt()
            layoutParams = lp
        }
        root.addView(distanceLabel)

        val distanceInput = EditText(requireContext()).apply {
            hint = "Ex: 5"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("5")
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (24 * dp).toInt()
            layoutParams = lp
        }
        root.addView(distanceInput)

        val btnContinue = Button(requireContext()).apply {
            text = "Continuer"
            setBackgroundColor(android.graphics.Color.parseColor("#FF9739"))
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (48 * dp).toInt()
            )
            setOnClickListener {
                val distance = distanceInput.text.toString().toIntOrNull() ?: 5
                listener?.onFirstStepComplete(selectedInterests.toList(), distance)
            }
        }
        root.addView(btnContinue)

        return root
    }

    companion object {
        fun newInstance() = OnboardingFirstStepFragment()
    }
}
