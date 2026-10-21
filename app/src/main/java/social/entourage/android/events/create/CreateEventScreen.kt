package social.entourage.android.events.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.ui.create.CfWizardScaffold

/**
 * Pont entre le formulaire du [CreateEventViewModel] (champs mutables simples, non observables) et
 * Compose : [changed] prévient le ViewModel (validation des champs déjà signalés) et relance le
 * rendu des composables qui lisent [form].
 */
@Stable
internal class CreateEventUiState(val viewModel: CreateEventViewModel) {
    private var version by mutableIntStateOf(0)

    val form: CreateEventForm get() = version.let { viewModel.form }

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
internal class CreateEventActions(
    val onClose: () -> Unit,
    val onSaveDraft: () -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onChoosePhoto: () -> Unit,
    val onPickAddress: () -> Unit,
    val onGroupsShown: () -> Unit,
    val onLoadMoreGroups: () -> Unit,
)

/**
 * Assistant de création / d'édition : le gabarit partagé ([CfWizardScaffold]) autour du contenu de
 * l'étape (ou de l'aperçu).
 */
@Composable
internal fun CreateEventScreen(
    state: CreateEventUiState,
    page: Int,
    groups: List<Group>,
    actions: CreateEventActions,
) {
    val viewModel = state.viewModel
    val errors by viewModel.errors.observeAsState(emptyMap())
    val steps = viewModel.steps
    val isPreview = page >= viewModel.previewPageIndex
    val isEdition = viewModel.isEdition

    // Enregistrement de brouillon masqué (actions.onSaveDraft conservé, non atteignable).
    CfWizardScaffold(
        headerTitle = stringResource(if (isEdition) R.string.edit_event else R.string.new_event),
        page = page,
        stepCount = steps.size,
        nextLabel = stringResource(
            when {
                !isPreview -> R.string.create_event_continue
                isEdition -> R.string.edit
                else -> R.string.create_event_publish
            }
        ),
        tagPrefix = "create_event",
        onClose = actions.onClose,
        onPrevious = actions.onPrevious,
        onNext = actions.onNext,
    ) { current ->
        if (current >= viewModel.previewPageIndex) {
            CreateEventPreview(state)
        } else when (steps[current]) {
            CreateEventStep.PRESENTATION ->
                CreateEventStepPresentation(state, errors, actions.onChoosePhoto)
            CreateEventStep.WHEN ->
                CreateEventStepWhen(state, errors)
            CreateEventStep.WHERE_AND_FOR_WHOM ->
                CreateEventStepWhere(state, errors, actions.onPickAddress)
            CreateEventStep.CATEGORIES ->
                CreateEventStepCategories(state, errors)
            CreateEventStep.SHARING ->
                CreateEventStepSharing(state, groups, actions.onGroupsShown, actions.onLoadMoreGroups)
        }
    }
}
