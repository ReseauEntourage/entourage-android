package social.entourage.android.onboarding.onboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.FragmentOnboardingPhase3Binding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.tools.log.AnalyticsEvents

private const val ARG_ENTOUR = "entour"
private const val ARG_BEENTOUR = "beentour"
private const val ARG_ASSO = "asso"
private const val ARG_ADDRESS = "address" // gardé pour compat, non utilisé ici

class OnboardingPhase3Fragment : Fragment() {

    private lateinit var binding: FragmentOnboardingPhase3Binding
    private var callback: OnboardingStartCallback? = null

    // état initial passé par le parent
    private var isEntour = false
    private var isBeEntour = false
    private var isAsso = false

    // adapter
    private lateinit var adapter: ProfileChoiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEntour = it.getBoolean(ARG_ENTOUR)
            isBeEntour = it.getBoolean(ARG_BEENTOUR)
            isAsso = it.getBoolean(ARG_ASSO)
            // l’adresse est ignorée dorénavant, on la gèrera dans une étape dédiée
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingPhase3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingStartCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_profile)
        EnhancedOnboarding.shouldNotDisplayCampain = true

        setupRecycler()
        propagateSelectionToParent() // active/désactive "Suivant" au chargement
    }

    private fun setupRecycler() {
        val items = buildInitialItems()

        adapter = ProfileChoiceAdapter(
            items = items,
            onClick = { clicked ->
                // sélection exclusive : un seul choix à la fois
                isEntour = clicked.type == ProfileChoiceType.ENTOUR
                isBeEntour = clicked.type == ProfileChoiceType.BE_ENTOUR
                isAsso = clicked.type == ProfileChoiceType.ASSO

                adapter.updateSelection(isEntour, isBeEntour, isAsso)
                propagateSelectionToParent()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@OnboardingPhase3Fragment.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun buildInitialItems(): List<ProfileChoice> {
        return listOf(
            ProfileChoice(
                type = ProfileChoiceType.BE_ENTOUR,
                titleRes = R.string.option_supported,
                subtitleRes = R.string.option_supported_desc,
                iconRes = R.drawable.onboarding_been_entour,
                selected = isBeEntour
            ),
            ProfileChoice(
                type = ProfileChoiceType.ENTOUR,
                titleRes = R.string.option_surround,
                subtitleRes = R.string.option_surround_desc,
                iconRes = R.drawable.onboarding_entour,
                selected = isEntour
            ),
            ProfileChoice(
                type = ProfileChoiceType.ASSO,
                titleRes = R.string.onboard_phase3_asso_title,
                subtitleRes = R.string.onboard_phase3_asso_desc, // ajoute cette string si besoin
                iconRes = R.drawable.onboarding_asso,            // icône à fournir dans tes drawables
                selected = isAsso
            )
        )
    }

    private fun propagateSelectionToParent() {
        // On n’envoie plus d’adresse ici (NULL), elle sera gérée plus tard.
        callback?.updateUsertype(
            isEntour = isEntour,
            isBeEntour = isBeEntour,
            both = false,               // on supprime l’option “les deux” dans ce nouvel écran
            isAsso = isAsso
        )

        // On peut aussi piloter l’état du bouton "Suivant" directement :
        val hasAnySelection = isEntour || isBeEntour || isAsso
        callback?.updateButtonNext(hasAnySelection)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            isEntour: Boolean,
            isBeentour: Boolean,
            isAsso: Boolean,
            @Suppress("UNUSED_PARAMETER") address: User.Address?
        ) = OnboardingPhase3Fragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_ENTOUR, isEntour)
                putBoolean(ARG_BEENTOUR, isBeentour)
                putBoolean(ARG_ASSO, isAsso)
                // ARG_ADDRESS conservé pour compat, mais non utilisé
            }
        }
    }
}
