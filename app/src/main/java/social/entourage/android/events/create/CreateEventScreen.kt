package social.entourage.android.events.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular

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
 * Assistant de création / d'édition : barre du haut, progression, libellé d'étape, contenu de
 * l'étape (ou aperçu) et pied de page Retour / Continuer.
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
    val stepCount = steps.size
    val isPreview = page >= viewModel.previewPageIndex
    val isEdition = viewModel.isEdition

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CeWhite)
                .semantics { testTagsAsResourceId = true }
        ) {
            TopBar(isEdition, actions)

            val progress by animateFloatAsState(
                targetValue = (page + 1).coerceAtMost(stepCount).toFloat() / stepCount,
                label = "create_event_progress"
            )
            CeProgressBar(progress)

            CeText(
                text = if (isPreview) {
                    stringResource(R.string.create_event_last_step)
                } else {
                    stringResource(R.string.create_event_step_progress, page + 1, stepCount)
                },
                style = ceStyle(NunitoSansRegular, 13f, CeGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 20.dp, end = 22.dp)
                    .testTag("create_event_step_label")
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        } else {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        }
                    },
                    label = "create_event_page"
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

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CeGreyContour)
            )
            Footer(
                modifier = Modifier.navigationBarsPadding(),
                page = page,
                nextLabel = stringResource(
                    when {
                        !isPreview -> R.string.create_event_continue
                        isEdition -> R.string.edit
                        else -> R.string.create_event_publish
                    }
                ),
                actions = actions
            )
        }
    }
}

@Composable
private fun TopBar(isEdition: Boolean, actions: CreateEventActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Zone sûre (barre d'état + encoche) puis marge de respiration sous elle.
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Top))
            .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CeRoundButton(
            size = 36.dp,
            icon = R.drawable.ic_create_event_back,
            description = stringResource(R.string.back),
            onClick = actions.onClose,
            iconPadding = 6.dp,
            modifier = Modifier.testTag("create_event_back")
        )
        CeText(
            text = stringResource(if (isEdition) R.string.edit_event else R.string.new_event),
            style = ceStyle(NunitoSansBold, 14f, CeBlack),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Enregistrement de brouillon masqué (actions.onSaveDraft conservé, non atteignable).
        // Espace de même largeur que le bouton retour pour garder le titre centré.
        Spacer(Modifier.size(36.dp))
    }
}

@Composable
private fun Footer(modifier: Modifier, page: Int, nextLabel: String, actions: CreateEventActions) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 22.dp, top = 11.dp, end = 22.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Invisible (mais encombrant) sur la première page : le CTA reste à droite.
        val first = page == 0
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .alpha(if (first) 0f else 1f)
                .let {
                    if (first) it else it.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        role = Role.Button,
                        onClick = actions.onPrevious
                    )
                }
                .padding(horizontal = 4.dp)
                .testTag("create_event_previous"),
            contentAlignment = Alignment.Center
        ) {
            CeText(stringResource(R.string.back), ceStyle(NunitoSansBold, 15f, CeBlack))
        }
        Spacer(Modifier.weight(1f))
        CeCta(
            label = nextLabel,
            onClick = actions.onNext,
            modifier = Modifier.testTag("create_event_next")
        )
    }
}
