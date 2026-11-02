package social.entourage.android.suggestion

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import social.entourage.android.R
import social.entourage.android.ui.theme.ColorOrange
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.NunitoSansSemiBold

sealed interface ExplorerUi {
    data object Hidden : ExplorerUi
    data object Loading : ExplorerUi
    data class Ready(val accesses: List<ExplorerAccess>) : ExplorerUi
}

fun SuggestionUiState.toExplorerUi(): ExplorerUi {
    val accesses = payload?.explorer.orEmpty()
    return when {
        accesses.isNotEmpty() -> ExplorerUi.Ready(accesses)
        loading && payload == null -> ExplorerUi.Loading
        // Zéro accès (ou erreur sans donnée) : toute la zone, titre compris, est masquée.
        else -> ExplorerUi.Hidden
    }
}

@Composable
private fun ExplorerTab.targetLabel(): String = stringResource(
    when (this) {
        ExplorerTab.ENTRAIDE_DEMANDS -> R.string.home_suggestion_explorer_target_entraide
        ExplorerTab.EVENTS -> R.string.home_suggestion_explorer_target_events
        ExplorerTab.GROUPS -> R.string.home_suggestion_explorer_target_groups
        ExplorerTab.CONTENT -> R.string.home_suggestion_explorer_target_content
        ExplorerTab.PLACES -> R.string.home_suggestion_explorer_target_places
    }
)

/** Zone « Pour vous aujourd'hui » : 2 à 4 accès personnalisés vers les onglets, lignes entièrement cliquables. */
@Composable
fun SuggestionExplorerZone(
    ui: ExplorerUi,
    onAccess: (ExplorerAccess) -> Unit,
    onShown: () -> Unit,
) {
    if (ui is ExplorerUi.Hidden) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            stringResource(R.string.home_suggestion_explorer_title),
            modifier = Modifier
                .semantics { heading() }
                .padding(start = 2.dp),
            color = Color(0xFF1A1A1A),
            fontFamily = NunitoSansBold,
            fontSize = 18.sp,
        )
        when (ui) {
            is ExplorerUi.Loading -> repeat(2) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3F0EE)),
                )
            }
            is ExplorerUi.Ready -> {
                LaunchedEffect(ui.accesses.map { it.id }) { onShown() }
                ui.accesses.forEach { ExplorerRow(it, onClick = { onAccess(it) }) }
            }
            ExplorerUi.Hidden -> Unit
        }
    }
}

@Composable
private fun ExplorerRow(access: ExplorerAccess, onClick: () -> Unit) {
    val style = access.type?.style() ?: return
    val opens = stringResource(R.string.home_suggestion_explorer_opens, access.tab.targetLabel())
    val readout = listOfNotNull(access.label, access.context, opens).joinToString(". ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = readout
                role = Role.Button
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(style.mediaBackground),
            contentAlignment = Alignment.Center,
        ) {
            // Repli d'icône par type.
            Text(style.fallbackEmoji, fontSize = 19.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(
                access.label,
                color = Color(0xFF1A1A1A),
                fontFamily = NunitoSansSemiBold,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (!access.context.isNullOrBlank()) {
                Text(
                    access.context,
                    color = Color(0xFF6D6C6C),
                    fontFamily = NunitoSansRegular,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text("›", color = ColorOrange, fontFamily = NunitoSansBold, fontSize = 22.sp)
    }
}
