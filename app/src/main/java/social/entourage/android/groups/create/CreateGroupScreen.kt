package social.entourage.android.groups.create

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
 * Pont entre le formulaire du [CreateGroupViewModel] (champs mutables simples, non observables) et
 * Compose : [changed] prévient le ViewModel (validation des champs déjà signalés) et relance le
 * rendu des composables qui lisent [form].
 */
@Stable
internal class CreateGroupUiState(val viewModel: CreateGroupViewModel) {
    private var version by mutableIntStateOf(0)

    val form: CreateGroupForm get() = version.let { viewModel.form }

    /** À appeler après chaque modification d'un champ du formulaire. */
    fun changed() {
        viewModel.onFormChanged()
        version++
    }

    /** Relance le rendu sans passer par la validation (retour de photo, etc.). */
    fun refresh() {
        version++
    }
}

/** Actions que l'écran délègue au fragment (navigation, sélecteurs système, envoi). */
internal class CreateGroupActions(
    val onClose: () -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onChoosePhoto: () -> Unit,
    val onPickAddress: () -> Unit,
)

/** Assistant de création / d'édition de groupe : le gabarit partagé autour de l'étape ou de l'aperçu. */
@Composable
internal fun CreateGroupScreen(
    state: CreateGroupUiState,
    page: Int,
    actions: CreateGroupActions,
) {
    val viewModel = state.viewModel
    val errors by viewModel.errors.observeAsState(emptyMap())
    val steps = viewModel.steps
    val isPreview = page >= viewModel.previewPageIndex
    val isEdition = viewModel.isEdition

    CfWizardScaffold(
        headerTitle = stringResource(if (isEdition) R.string.edit_group else R.string.new_group),
        page = page,
        stepCount = steps.size,
        nextLabel = stringResource(
            when {
                !isPreview -> R.string.create_event_continue
                isEdition -> R.string.edit
                else -> R.string.create
            }
        ),
        tagPrefix = "create_group",
        onClose = actions.onClose,
        onPrevious = actions.onPrevious,
        onNext = actions.onNext,
    ) { current ->
        if (current >= viewModel.previewPageIndex) {
            CreateGroupPreview(state)
        } else when (steps[current]) {
            CreateGroupStep.INFO -> CreateGroupStepInfo(state, errors, actions.onPickAddress)
            CreateGroupStep.INTERESTS -> CreateGroupStepInterests(state, errors)
            CreateGroupStep.PHOTO -> CreateGroupStepPhoto(state, errors, actions.onChoosePhoto)
        }
    }
}
