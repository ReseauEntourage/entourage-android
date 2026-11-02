package social.entourage.android.suggestion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import social.entourage.android.R
import social.entourage.android.ui.theme.ColorOrange
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.NunitoSansSemiBold

private val CardBackground = Color(0xFFFEEAE3)
private val TitleColor = Color(0xFF1A1A1A)
private val MetaColor = Color(0xFF6D6C6C)
private val AuthorColor = Color(0xFF363636)
private val OutlineColor = Color(0xFFD9D9D9)

/** Couleur du badge et fond pastel du média, par type (maquette EN-9483). */
internal data class TypeStyle(val accent: Color, val mediaBackground: Color, val fallbackEmoji: String)

internal fun SuggestionType.style(): TypeStyle = when (this) {
    SuggestionType.ENTRAIDE -> TypeStyle(Color(0xFFD53F00), Color(0xFFFEEAE3), "🤝")
    SuggestionType.CONTENT -> TypeStyle(Color(0xFF2F6DB5), Color(0xFFEAF2FB), "📘")
    SuggestionType.EVENT -> TypeStyle(Color(0xFFFF9739), Color(0xFFFDDCD0), "📅")
    SuggestionType.GROUP -> TypeStyle(Color(0xFF1E7F51), Color(0xFFDEF1E8), "👥")
    SuggestionType.CONNECTION -> TypeStyle(Color(0xFF47A8B9), Color(0xFFECF8FB), "👋")
    SuggestionType.PLACE -> TypeStyle(Color(0xFFB84D0E), Color(0xFFEAF2FB), "📍")
}

@Composable
private fun SuggestionType.badgeText(tag: ContentTag?): String = when (this) {
    SuggestionType.ENTRAIDE -> "🤝 " + stringResource(R.string.home_suggestion_badge_entraide)
    SuggestionType.CONTENT -> when (tag) {
        ContentTag.ACT -> "🟢 " + stringResource(R.string.home_suggestion_badge_act)
        else -> "📘 " + stringResource(R.string.home_suggestion_badge_understand)
    }
    SuggestionType.EVENT -> "📅 " + stringResource(R.string.home_suggestion_badge_event)
    SuggestionType.GROUP -> "👥 " + stringResource(R.string.home_suggestion_badge_group)
    SuggestionType.CONNECTION -> "👋 " + stringResource(R.string.home_suggestion_badge_connection)
    SuggestionType.PLACE -> "📍 " + stringResource(R.string.home_suggestion_badge_place)
}

@Composable
internal fun SuggestionCta.label(): String = stringResource(
    when (this) {
        SuggestionCta.ANSWER -> R.string.home_suggestion_cta_answer
        SuggestionCta.READ -> R.string.home_suggestion_cta_read
        SuggestionCta.PARTICIPATE -> R.string.home_suggestion_cta_participate
        SuggestionCta.REGISTER -> R.string.home_suggestion_cta_register
        SuggestionCta.JOIN -> R.string.home_suggestion_cta_join
        SuggestionCta.SAY_HELLO -> R.string.home_suggestion_cta_say_hello
        SuggestionCta.SEE_PLACE -> R.string.home_suggestion_cta_see_place
    }
)

/** Puce secondaire selon le variant (visio, quartier, national) ; catégorie pour l'entraide. */
@Composable
private fun NextStepSuggestion.chipText(): String? = when {
    type == SuggestionType.ENTRAIDE -> categoryLabel
    variant == SuggestionVariant.REMOTE -> stringResource(R.string.home_suggestion_chip_remote)
    variant == SuggestionVariant.LOCAL -> stringResource(R.string.home_suggestion_chip_local)
    variant == SuggestionVariant.NATIONAL -> stringResource(R.string.home_suggestion_chip_national)
    else -> null
}

private fun initials(name: String?): String =
    name.orEmpty().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }

/**
 * Conteneur polymorphe unique : rend n'importe quel type de suggestion du moteur avec l'anatomie
 * commune (badge, ⓘ, média optionnel, titre, 1 à 3 métas optionnelles, CTA, action secondaire).
 *
 * @param onWhy ouvre « Pourquoi cette suggestion ? » ; le bouton ⓘ est masqué si null.
 */
@Composable
fun SuggestionCard(
    suggestion: NextStepSuggestion,
    onCta: () -> Unit,
    onLater: () -> Unit,
    onBody: () -> Unit,
    modifier: Modifier = Modifier,
    onWhy: (() -> Unit)? = null,
) {
    val type = suggestion.type ?: return
    val style = type.style()
    val badge = type.badgeText(suggestion.contentTag)
    val chip = suggestion.chipText()
    val ctaLabel = suggestion.cta.label()
    val laterLabel = stringResource(R.string.home_suggestion_later)
    val whyLabel = stringResource(R.string.home_suggestion_why_button_description)
    val openDetails = stringResource(R.string.home_suggestion_open_details)

    // Un seul élément de lecture, dans l'ordre : type, titre, méta, actions.
    val readout = remember(suggestion, badge, chip) {
        listOfNotNull(
            badge, chip, suggestion.personName, suggestion.title, suggestion.body,
        ).plus(suggestion.metas.map { it.text }).joinToString(". ")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable(onClickLabel = openDetails, onClick = onBody)
            .semantics(mergeDescendants = true) {
                contentDescription = readout
                customActions = buildList {
                    add(CustomAccessibilityAction(ctaLabel) { onCta(); true })
                    add(CustomAccessibilityAction(laterLabel) { onLater(); true })
                    if (onWhy != null) add(CustomAccessibilityAction(whyLabel) { onWhy(); true })
                }
            }
            .padding(start = 18.dp, top = 6.dp, end = 6.dp, bottom = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = badge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 11.dp, vertical = 4.dp),
                    color = style.accent,
                    fontFamily = NunitoSansBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (chip != null) {
                    Text(
                        text = chip,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MetaColor,
                        fontFamily = NunitoSansSemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (onWhy != null) {
                // Zone tactile de 48 dp pour un disque visuel de 30 dp.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClickLabel = whyLabel, role = Role.Button, onClick = onWhy),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(ColorOrange.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("ⓘ", color = Color(0xFFD53F00), fontSize = 15.sp)
                    }
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        Row(
            modifier = Modifier.padding(end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Media(type, style, suggestion.personName)
            Column(Modifier.weight(1f)) {
                // Champs libres : affichés bruts, avec un nombre de lignes borné pour la police agrandie.
                if (type == SuggestionType.ENTRAIDE && suggestion.personName != null) {
                    Text(
                        suggestion.personName,
                        color = AuthorColor,
                        fontFamily = NunitoSansSemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    suggestion.title,
                    color = TitleColor,
                    fontFamily = NunitoSansBold,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                if (!suggestion.body.isNullOrBlank()) {
                    Text(
                        suggestion.body,
                        color = MetaColor,
                        fontFamily = NunitoSansRegular,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                // Chaque méta est optionnelle : pas d'emplacement fixe pour la distance.
                suggestion.metas.take(3).forEach { meta ->
                    Text(
                        text = if (meta.icon != null) "${meta.icon} ${meta.text}" else meta.text,
                        color = MetaColor,
                        fontFamily = NunitoSansRegular,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }

        val stackActions = LocalDensity.current.fontScale > 1.3f
        val actionsModifier = Modifier.padding(top = 16.dp, end = 12.dp)
        if (stackActions) {
            Column(actionsModifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton(ctaLabel, onCta, Modifier.fillMaxWidth())
                LaterButton(laterLabel, onLater, Modifier.fillMaxWidth())
            }
        } else {
            Row(actionsModifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton(ctaLabel, onCta, Modifier.weight(1f))
                LaterButton(laterLabel, onLater)
            }
        }
    }
}

@Composable
private fun Media(type: SuggestionType, style: TypeStyle, personName: String?) {
    val isPerson = type == SuggestionType.ENTRAIDE || type == SuggestionType.CONNECTION
    val letters = initials(personName)
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(if (isPerson) CircleShape else RoundedCornerShape(12.dp))
            .background(style.mediaBackground),
        contentAlignment = Alignment.Center,
    ) {
        // Repli par type quand l'avatar ou la vignette réels sont absents.
        if (isPerson && letters.isNotEmpty()) {
            Text(letters, color = style.accent, fontFamily = NunitoSansBold, fontSize = 16.sp)
        } else {
            Text(style.fallbackEmoji, fontSize = 24.sp)
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(ColorOrange)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = Color.White,
            fontFamily = NunitoSansBold,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LaterButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(32.dp))
            .border(1.5.dp, OutlineColor, RoundedCornerShape(32.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = AuthorColor,
            fontFamily = NunitoSansSemiBold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
