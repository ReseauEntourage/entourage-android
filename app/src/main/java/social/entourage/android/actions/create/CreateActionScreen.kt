package social.entourage.android.actions.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import social.entourage.android.R
import social.entourage.android.ui.create.CfWizardScaffold

/**
 * Pont entre le formulaire du [CreateActionViewModel] (champs mutables simples, non observables) et
 * Compose : [changed] prévient le ViewModel (validation des champs déjà signalés) et relance le
 * rendu des composables qui lisent [form] ou [steps].
 */
@Stable
internal class CreateActionUiState(val viewModel: CreateActionViewModel) {
    private var version by mutableIntStateOf(0)

    val form: CreateActionForm get() = version.let { viewModel.form }

    /** Liste d'étapes courante (l'étape de partage peut apparaître ou disparaître). */
    val steps: List<CreateActionStep> get() = version.let { viewModel.steps }

    /** À appeler après chaque modification d'un champ du formulaire. */
    fun changed() {
        viewModel.onFormChanged()
        version++
    }

    /** Relance le rendu sans passer par la validation (retour de photo, de groupe, etc.). */
    fun refresh() {
        version++
    }
}

/** Actions que l'écran délègue au fragment (navigation, sélecteurs système, envoi). */
internal class CreateActionActions(
    val onClose: () -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onChoosePhoto: () -> Unit,
    val onPickAddress: () -> Unit,
    val onOpenAboutCategories: () -> Unit,
)

/** Assistant de création / d'édition d'entraide : le gabarit partagé autour de l'étape ou de l'aperçu. */
@Composable
internal fun CreateActionScreen(
    state: CreateActionUiState,
    page: Int,
    actions: CreateActionActions,
) {
    val viewModel = state.viewModel
    val errors by viewModel.errors.observeAsState(emptyMap())
    val steps = state.steps
    val isPreview = page >= steps.size
    val isEdition = viewModel.isEdition
    val isDemand = viewModel.isDemand

    CfWizardScaffold(
        headerTitle = stringResource(
            when {
                isEdition && isDemand -> R.string.action_edit_demand_title
                isEdition -> R.string.action_edit_contrib_title
                isDemand -> R.string.action_create_demand_title
                else -> R.string.action_create_contrib_title
            }
        ),
        page = page,
        stepCount = steps.size,
        nextLabel = stringResource(
            when {
                !isPreview -> R.string.create_event_continue
                isEdition -> R.string.edit
                else -> R.string.create_event_publish
            }
        ),
        tagPrefix = "create_action",
        onClose = actions.onClose,
        onPrevious = actions.onPrevious,
        onNext = actions.onNext,
    ) { current ->
        val currentSteps = state.steps
        if (current >= currentSteps.size) {
            CreateActionPreview(state)
        } else when (currentSteps[current]) {
            CreateActionStep.CATEGORY -> CreateActionStepCategory(state, errors, actions.onOpenAboutCategories)
            CreateActionStep.DESCRIPTION -> CreateActionStepDescription(state, errors, actions.onChoosePhoto)
            CreateActionStep.LOCATION -> CreateActionStepLocation(state, errors, actions.onPickAddress)
            CreateActionStep.SHARING -> CreateActionStepSharing(state, errors)
        }
    }
}
