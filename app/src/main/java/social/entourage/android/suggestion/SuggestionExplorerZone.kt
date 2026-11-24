package social.entourage.android.suggestion

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.colorResource
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
import social.entourage.android.ui.theme.QuicksandBold

/** Coins des lignes (16 dp) et épaisseur de leur liseré, le même trait de 1 dp que les cartes des outils solidaires. */
private val ExplorerRowShape = RoundedCornerShape(16.dp)
private val ExplorerRowBorderWidth = 1.dp

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

/** Zone « Pour vous aujourd'hui » : 2 à 4 accès personnalisés vers les onglets, lignes entièrement cliquables. */
@Composable
fun SuggestionExplorerZone(
    ui: ExplorerUi,
    onAccess: (ExplorerAccess) -> Unit,
    onShown: (List<ExplorerAccess>) -> Unit,
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
            // Titre : Quicksand Bold (police des titres de l'app), le reste de la suggestion est en Nunito.
            fontFamily = QuicksandBold,
            fontSize = 18.sp,
        )
        when (ui) {
            is ExplorerUi.Loading -> repeat(2) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .clip(ExplorerRowShape)
                        .background(Color(0xFFF3F0EE)),
                )
            }
            is ExplorerUi.Ready -> {
                LaunchedEffect(ui.accesses.map { it.id }) { onShown(ui.accesses) }
                ui.accesses.forEach { ExplorerRow(it, onClick = { onAccess(it) }) }
            }
            ExplorerUi.Hidden -> Unit
        }
    }
}

@Composable
private fun ExplorerRow(access: ExplorerAccess, onClick: () -> Unit) {
    val style = access.type?.style() ?: return
    val label = access.label.ifBlank { access.type?.defaultTitleRes()?.let { stringResource(it) }.orEmpty() }
    // Le tap ouvre la piste elle-même (et non un onglet) : le lecteur d'écran le dit comme sur iOS.
    val opens = stringResource(R.string.home_suggestion_explorer_opens)
    // Le type de la piste, comme le badge de la card (« 📅 Événement », « 🤝 Entraide »…) : sans lui, on ne voit pas
    // toujours de quoi il s'agit (une entraide, par exemple).
    val badge = access.suggestion?.badgeText() ?: access.type?.badgeText(null).orEmpty()
    val readout = listOfNotNull(badge.takeIf { it.isNotBlank() }, label, access.context, opens).joinToString(". ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            // Liseré des cartes des outils solidaires de la home (home_v2_action_item_shape : fond blanc,
            // trait de 1 dp en `beige`), sans ombre. `clip` arrondit aussi le fond et l'ondulation du tap.
            .clip(ExplorerRowShape)
            .background(Color.White)
            .border(ExplorerRowBorderWidth, colorResource(R.color.beige), ExplorerRowShape)
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
            if (badge.isNotBlank()) {
                Text(
                    text = badge,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(style.mediaBackground)
                        .padding(horizontal = 11.dp, vertical = 4.dp),
                    color = style.accent,
                    fontFamily = NunitoSansBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                label,
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
