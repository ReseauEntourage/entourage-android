package social.entourage.android.ui.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import social.entourage.android.R
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular

/**
 * Gabarit d'écran partagé par tous les parcours de création / d'édition (événement, groupe,
 * entraide) : barre du haut avec retour, barre de progression fine, libellé « Étape N sur X »,
 * contenu animé de l'étape (ou de l'aperçu) et pied de page Retour / Continuer.
 *
 * Le gabarit ne connaît rien du formulaire : l'appelant fournit la liste d'étapes (donc [stepCount]),
 * le contenu de chaque page et les actions. La page [stepCount] est l'aperçu, hors compte
 * « Étape N sur X ».
 *
 * @param tagPrefix préfixe des tags de test : `<prefix>_back`, `_step_label`, `_previous`, `_next`
 */
@Composable
internal fun CfWizardScaffold(
    headerTitle: String,
    page: Int,
    stepCount: Int,
    nextLabel: String,
    tagPrefix: String,
    onClose: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    content: @Composable (page: Int) -> Unit,
) {
    val isPreview = page >= stepCount

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CfWhite)
                .semantics { testTagsAsResourceId = true }
        ) {
            CfTopBar(headerTitle, tagPrefix, onClose)

            val progress by animateFloatAsState(
                targetValue = (page + 1).coerceAtMost(stepCount).toFloat() / stepCount.coerceAtLeast(1),
                label = "${tagPrefix}_progress"
            )
            CfProgressBar(progress)

            CfText(
                text = if (isPreview) {
                    stringResource(R.string.create_event_last_step)
                } else {
                    stringResource(R.string.create_event_step_progress, page + 1, stepCount)
                },
                style = cfStyle(NunitoSansRegular, 13f, CfGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 20.dp, end = 22.dp)
                    .testTag("${tagPrefix}_step_label")
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
                    label = "${tagPrefix}_page"
                ) { current -> content(current) }
            }

            CfFooter(
                modifier = Modifier.navigationBarsPadding(),
                page = page,
                nextLabel = nextLabel,
                tagPrefix = tagPrefix,
                onPrevious = onPrevious,
                onNext = onNext,
            )
        }
    }
}

@Composable
private fun CfTopBar(title: String, tagPrefix: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Zone sûre (barre d'état + encoche) puis marge de respiration sous elle.
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Top))
            .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CfRoundButton(
            size = 36.dp,
            icon = R.drawable.ic_create_event_back,
            description = stringResource(R.string.back),
            onClick = onClose,
            iconPadding = 6.dp,
            modifier = Modifier.testTag("${tagPrefix}_back")
        )
        CfText(
            text = title,
            style = cfStyle(NunitoSansBold, 14f, CfBlack),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Espace de même largeur que le bouton retour pour garder le titre centré.
        Spacer(Modifier.size(36.dp))
    }
}

@Composable
private fun CfFooter(
    modifier: Modifier,
    page: Int,
    nextLabel: String,
    tagPrefix: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    // Même barre que le bas des écrans d'onboarding enrichi : fond blanc, élévation 8dp,
    // padding 20dp, deux boutons de poids égal séparés par 2 x 4dp.
    Row(
        modifier = modifier
            .shadow(8.dp)
            .background(CfWhite)
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Invisible (mais encombrant) sur la première page : le bouton principal reste à droite.
        val first = page == 0
        CfFooterButton(
            label = stringResource(R.string.back),
            primary = false,
            onClick = { if (!first) onPrevious() },
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .alpha(if (first) 0f else 1f)
                .testTag("${tagPrefix}_previous")
        )
        CfFooterButton(
            label = nextLabel,
            primary = true,
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
                .testTag("${tagPrefix}_next")
        )
    }
}
