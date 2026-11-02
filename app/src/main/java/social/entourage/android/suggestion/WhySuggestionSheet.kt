package social.entourage.android.suggestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import social.entourage.android.R
import social.entourage.android.ui.theme.ColorOrange
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular

private val TitleColor = Color(0xFF1A1A1A)
private val BodyColor = Color(0xFF363636)
private val FooterColor = Color(0xFF6D6C6C)

@Composable
private fun SuggestionType?.whyTitle(): String = stringResource(
    when (this) {
        SuggestionType.ENTRAIDE -> R.string.home_suggestion_why_title_entraide
        SuggestionType.CONTENT -> R.string.home_suggestion_why_title_content
        SuggestionType.EVENT -> R.string.home_suggestion_why_title_event
        SuggestionType.GROUP -> R.string.home_suggestion_why_title_group
        SuggestionType.CONNECTION -> R.string.home_suggestion_why_title_connection
        SuggestionType.PLACE -> R.string.home_suggestion_why_title_place
        null -> R.string.home_suggestion_why_title_default
    }
)

private fun ReasonKind.icon(): String = when (this) {
    ReasonKind.PROXIMITY -> "📍"
    ReasonKind.INTERESTS -> "✨"
    ReasonKind.MOMENT -> "🗓️"
    ReasonKind.HISTORY -> "🔁"
    ReasonKind.NEED -> "🔎"
    ReasonKind.ACCESSIBILITY -> "✅"
    ReasonKind.SOCIAL_PROOF -> "👥"
}

@Composable
private fun SuggestionReason.text(): String {
    val p = param.orEmpty()
    return when (kind) {
        ReasonKind.PROXIMITY -> stringResource(R.string.home_suggestion_why_reason_proximity, p)
        ReasonKind.INTERESTS -> stringResource(R.string.home_suggestion_why_reason_interests, p)
        ReasonKind.MOMENT -> stringResource(R.string.home_suggestion_why_reason_moment, p)
        ReasonKind.HISTORY -> stringResource(R.string.home_suggestion_why_reason_history, p)
        ReasonKind.NEED -> stringResource(R.string.home_suggestion_why_reason_need, p)
        ReasonKind.ACCESSIBILITY -> stringResource(R.string.home_suggestion_why_reason_accessibility)
        ReasonKind.SOCIAL_PROOF -> stringResource(R.string.home_suggestion_why_reason_social_proof)
    }
}

/**
 * Bottom-sheet « Pourquoi cette suggestion ? » ouverte par le ⓘ d'une card. Les raisons viennent
 * du payload du moteur (2 à 3, une phrase chacune) ; aucun terme interne (segment, score) n'est affiché.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhySuggestionSheet(
    suggestion: NextStepSuggestion,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(
                suggestion.type.whyTitle(),
                modifier = Modifier
                    .semantics { heading() }
                    .padding(bottom = 16.dp),
                color = TitleColor,
                fontFamily = NunitoSansBold,
                fontSize = 20.sp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                suggestion.displayableReasons().forEach { reason ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEEAE3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(reason.kind.icon(), fontSize = 18.sp)
                        }
                        Text(
                            reason.text(),
                            modifier = Modifier.weight(1f),
                            color = BodyColor,
                            fontFamily = NunitoSansRegular,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.home_suggestion_why_footer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp),
                color = FooterColor,
                fontFamily = NunitoSansRegular,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ColorOrange)
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.home_suggestion_why_close),
                    color = Color.White,
                    fontFamily = NunitoSansBold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}
