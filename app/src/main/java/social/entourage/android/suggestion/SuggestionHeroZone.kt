package social.entourage.android.suggestion

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import social.entourage.android.R
import social.entourage.android.ui.theme.NunitoSansBold

sealed interface HeroUi {
    data object Hidden : HeroUi
    data object Loading : HeroUi
    data class Ready(val suggestion: NextStepSuggestion) : HeroUi
}

fun SuggestionUiState.toHeroUi(): HeroUi {
    val hero = payload?.hero
    return when {
        hero != null -> HeroUi.Ready(hero)
        loading && payload == null -> HeroUi.Loading
        else -> HeroUi.Hidden
    }
}

/** Zone « Votre prochain pas » : titre + card, masqués ensemble quand il n'y a rien à proposer. */
@Composable
fun SuggestionHeroZone(
    ui: HeroUi,
    onCta: (NextStepSuggestion) -> Unit,
    onLater: (NextStepSuggestion) -> Unit,
    onBody: (NextStepSuggestion) -> Unit,
    onShown: (NextStepSuggestion) -> Unit,
    onWhyOpened: (NextStepSuggestion) -> Unit = {},
) {
    var whyFor by remember { mutableStateOf<NextStepSuggestion?>(null) }
    if (ui is HeroUi.Hidden) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            stringResource(R.string.home_suggestion_hero_title),
            modifier = Modifier
                .semantics { heading() }
                .padding(start = 2.dp, bottom = 8.dp),
            color = Color(0xFF1A1A1A),
            fontFamily = NunitoSansBold,
            fontSize = 18.sp,
        )
        when (ui) {
            is HeroUi.Loading -> Box(
                // Squelette de hauteur proche de la card : pas de saut de la liste à l'arrivée.
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF6E7E0)),
            )
            is HeroUi.Ready -> {
                val s = ui.suggestion
                LaunchedEffect(s.id) { onShown(s) }
                SuggestionCard(
                    suggestion = s,
                    onCta = { onCta(s) },
                    onLater = { onLater(s) },
                    onBody = { onBody(s) },
                    onWhy = if (s.displayableReasons().isEmpty()) null else {
                        { whyFor = s; onWhyOpened(s) }
                    },
                )
            }
            HeroUi.Hidden -> Unit
        }
    }

    whyFor?.let { WhySuggestionSheet(it, onDismiss = { whyFor = null }) }
}
