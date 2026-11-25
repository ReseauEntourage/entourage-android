package social.entourage.android.home.compose

import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import social.entourage.android.R
import social.entourage.android.accessibility.a11yColor
import social.entourage.android.home.WelcomeJourneyState
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold

/** Les TextView ajoutent le « font padding » : on le conserve pour avoir la même hauteur de ligne. */
@Suppress("DEPRECATION")
private val TextViewPlatformStyle = PlatformTextStyle(includeFontPadding = true)

private fun journeyText(family: FontFamily, size: androidx.compose.ui.unit.TextUnit, color: Color) = TextStyle(
    fontFamily = family,
    fontSize = size,
    color = color,
    platformStyle = TextViewPlatformStyle,
)

/** Couleur du thème courant (mode contraste amélioré compris), comme `?attr/a11y...` dans le XML. */
@Composable
private fun a11yColorOf(@AttrRes attr: Int, @ColorRes default: Int): Color {
    val context = LocalContext.current
    return Color(context.a11yColor(attr, default))
}

private const val STEP_COUNT = 4

private val TodoIcons = listOf(
    R.drawable.ic_lucide_video,
    R.drawable.ic_group_welcome_home,
    R.drawable.ic_lucide_users,
    R.drawable.ic_lucide_messages,
)
private val DoneIcons = listOf(
    R.drawable.ic_lucide_check,
    R.drawable.ic_group_welcome_home,
    R.drawable.ic_lucide_check,
    R.drawable.ic_lucide_check,
)

/**
 * Parcours de bienvenue de la home (équivalent de `home_welcome_journey.xml` + `HomeWelcomeJourneyAdapter`).
 * Rend les [states] reçus : la visibilité globale, l'API (passer / masquer) et la confirmation de masquage
 * restent côté fragment. [onStepClick] et [onSkipClick] reçoivent l'index d'étape de 1 à 4.
 */
@Composable
fun HomeWelcomeJourney(
    states: List<WelcomeJourneyState>,
    onStepClick: (Int) -> Unit,
    onSkipClick: (Int) -> Unit,
    onHideRequested: () -> Unit,
) {
    // Encart de félicitations uniquement quand toutes les étapes sont réellement faites
    val isCompletedFully = states.all { it == WelcomeJourneyState.DONE }
    val completedCount = states.count { it == WelcomeJourneyState.DONE }
    // Seule la prochaine étape à faire a le bouton plein
    val nextTodo = states.indexOf(WelcomeJourneyState.TODO)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
    ) {
        JourneyHeaderRow(onHideRequested)

        // Une barre par étape, remplie seulement quand l'étape est faite (jamais quand elle est passée)
        val progressDescription = pluralStringResource(
            R.plurals.welcome_journey_progress_a11y, completedCount, completedCount, STEP_COUNT,
        )
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(6.dp)
                .semantics(mergeDescendants = true) { contentDescription = progressDescription },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(STEP_COUNT) { i ->
                val on = states.getOrNull(i) == WelcomeJourneyState.DONE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colorResource(if (on) R.color.orange else R.color.grey_middle)),
                )
            }
        }

        Text(
            text = stringResource(
                if (completedCount == 0) R.string.welcome_journey_subtitle_start
                else R.string.welcome_journey_subtitle_progress
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            style = journeyText(NunitoSansRegular, 13.sp, colorResource(R.color.grey)),
        )

        if (isCompletedFully) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .clip(JourneyCardShape)
                    .background(colorResource(R.color.green_light))
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.welcome_journey_complete),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = journeyText(QuicksandBold, 15.sp, colorResource(R.color.green)),
                )
            }
        } else {
            states.forEachIndexed { i, state ->
                val n = i + 1
                JourneyStepCard(
                    index = i,
                    state = state,
                    isPrimary = i == nextTodo,
                    topMargin = if (i == 0) 16.dp else 12.dp,
                    onStepClick = { onStepClick(n) },
                    onSkipClick = { onSkipClick(n) },
                )
            }
        }
    }
}

private val JourneyCardShape = RoundedCornerShape(12.dp)

/** Titre (largeur : tout sauf le bouton « ⋯ » de 40 dp) et bouton « ⋯ » centré sur la hauteur du titre, qui peut déborder. */
@Composable
private fun JourneyHeaderRow(onHideRequested: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val optionsDescription = stringResource(R.string.welcome_journey_options)
    val greyDark = colorResource(R.color.grey_dark)
    Layout(
        content = {
            Text(
                text = "Votre parcours de bienvenue",
                style = journeyText(QuicksandBold, social.entourage.android.home.compose.spRes(R.dimen.entourage_font_large), greyDark),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = { menuOpen = true },
                    )
                    .semantics { contentDescription = optionsDescription },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⋯",
                    style = journeyText(QuicksandBold, 20.sp, greyDark),
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.welcome_journey_hide_menu)) },
                        onClick = {
                            menuOpen = false
                            onHideRequested()
                        },
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val buttonSize = 40.dp.roundToPx()
        val titlePlaceable = measurables[0].measure(
            Constraints(maxWidth = (constraints.maxWidth - buttonSize).coerceAtLeast(0))
        )
        val buttonPlaceable = measurables[1].measure(Constraints.fixed(buttonSize, buttonSize))
        val height = titlePlaceable.height
        layout(constraints.maxWidth, height) {
            titlePlaceable.placeRelative(0, 0)
            buttonPlaceable.placeRelative(constraints.maxWidth - buttonSize, (height - buttonSize) / 2)
        }
    }
}

@Composable
private fun JourneyStepCard(
    index: Int,
    state: WelcomeJourneyState,
    isPrimary: Boolean,
    topMargin: androidx.compose.ui.unit.Dp,
    onStepClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    val n = index + 1
    val grey = colorResource(R.color.grey)
    val green = colorResource(R.color.green)
    val black = colorResource(R.color.black)
    val orangeText = a11yColorOf(R.attr.a11yTextOrange, R.color.orange)
    val buttonFill = a11yColorOf(R.attr.a11yButtonFill, R.color.orange)

    // (les titres des étapes 1 à 3 sont des littéraux du XML ; l'étape 4 passe par une ressource)
    val titleText = when (n) {
        1 -> "1 min pour tout comprendre"
        2 -> "Rejoignez un groupe qui vous ressemble"
        3 -> "Rencontrez l'équipe en visio"
        else -> stringResource(R.string.welcome_step4_title)
    }
    val descText = when (n) {
        1 -> "Qui sommes-nous, comment ça marche, et comment vous pouvez agir en 1 minute"
        2 -> "Des membres partout en France partagent vos centres d'intérêt. Rejoignez-les."
        3 -> "Une session d'accueil en petit groupe pour poser vos questions à l'équipe avec plusieurs créneaux disponibles."
        else -> stringResource(R.string.welcome_step4_desc)
    }
    val todoButtonLabel = when (n) {
        1 -> stringResource(R.string.welcome_video_cta)
        2 -> "Voir les groupes"
        3 -> "Voir les prochaines dates"
        else -> stringResource(R.string.welcome_step4_cta)
    }

    val cardBackground: Color
    val cardBorder: Color
    val titleColor: Color
    val descColor: Color
    val badgeText: String
    val badgeColor: Color
    val badgeBackground: Color
    val badgeBorder: Color?
    val iconRes: Int
    val iconBackground: Color
    when (state) {
        WelcomeJourneyState.DONE -> {
            cardBackground = colorResource(R.color.green_light)
            cardBorder = green
            titleColor = green
            descColor = green
            badgeText = "Terminé"
            badgeColor = green
            badgeBackground = Color.Transparent
            badgeBorder = green
            iconRes = DoneIcons[index]
            iconBackground = green
        }
        WelcomeJourneyState.SKIPPED -> {
            cardBackground = colorResource(R.color.white)
            cardBorder = colorResource(R.color.grey_onboarding_item_border)
            titleColor = grey
            descColor = grey
            badgeText = stringResource(R.string.welcome_journey_badge_skipped)
            badgeColor = grey
            badgeBackground = colorResource(R.color.white_f5_f5_f6)
            badgeBorder = null
            iconRes = TodoIcons[index]
            iconBackground = buttonFill
        }
        WelcomeJourneyState.TODO -> {
            cardBackground = colorResource(R.color.white)
            cardBorder = colorResource(R.color.orange_light_separator_event)
            titleColor = black
            descColor = grey
            badgeText = "À faire"
            badgeColor = orangeText
            badgeBackground = Color(0xFFFFE8D9)
            badgeBorder = null
            iconRes = TodoIcons[index]
            iconBackground = buttonFill
        }
    }

    val noRipple = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .padding(top = topMargin)
            .fillMaxWidth()
            // Étape faite : toute la carte à 50 %
            .alpha(if (state == WelcomeJourneyState.DONE) 0.5f else 1.0f)
            .clip(JourneyCardShape)
            .background(cardBackground)
            .border(1.dp, cardBorder, JourneyCardShape)
            // Toute la carte est cliquable, sans ondulation (le fond est un simple shape dans le XML)
            .clickable(interactionSource = noRipple, indication = null, onClick = onStepClick)
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackground, CircleShape)
                    .padding(8.dp),
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = titleText,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        style = journeyText(QuicksandBold, 15.sp, titleColor),
                    )
                    JourneyBadge(badgeText, badgeColor, badgeBackground, badgeBorder)
                }
                Text(
                    text = descText,
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                    style = journeyText(NunitoSansRegular, 15.sp, descColor),
                )
                if (state == WelcomeJourneyState.SKIPPED) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clickable(onClick = onStepClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.welcome_journey_redo_step),
                            style = journeyText(QuicksandBold, 13.sp, colorResource(R.color.orange_entourage)),
                        )
                    }
                }
            }
        }

        when (state) {
            WelcomeJourneyState.TODO -> {
                JourneyButton(
                    text = todoButtonLabel,
                    isPrimary = isPrimary,
                    fill = buttonFill,
                    outlinedTextColor = orangeText,
                    onClick = onStepClick,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .defaultMinSize(minHeight = 40.dp)
                        .clickable(onClick = onSkipClick)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.welcome_journey_skip_step),
                        textAlign = TextAlign.Center,
                        style = journeyText(QuicksandBold, 13.sp, grey),
                    )
                }
            }
            // Seule l'étape 1 garde un bouton vert (revoir la vidéo)
            WelcomeJourneyState.DONE -> if (n == 1) {
                JourneyButton(
                    text = "Revoir la vidéo",
                    isPrimary = true,
                    fill = green,
                    outlinedTextColor = green,
                    onClick = onStepClick,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            WelcomeJourneyState.SKIPPED -> Unit
        }
    }
}

@Composable
private fun JourneyBadge(text: String, color: Color, background: Color, border: Color?) {
    val shape = RoundedCornerShape(16.dp)
    Text(
        text = text,
        modifier = Modifier
            .background(background, shape)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = journeyText(NunitoSansBold, 13.sp, color),
    )
}

/**
 * `MaterialButton` de l'ancien XML : hauteur de vue 48 dp (minHeight AppCompat), fond de 36 dp (insets de 6 dp),
 * rayon 32 dp, padding 24 x 10 dp, 14 sp Quicksand Bold, interlettrage 0,07. Bouton « secondaire » : fond blanc,
 * texte orange, contour orange de 1,5 dp.
 */
@Composable
private fun JourneyButton(
    text: String,
    isPrimary: Boolean,
    fill: Color,
    outlinedTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape: Shape = RoundedCornerShape(32.dp)
    val background = if (isPrimary) fill else colorResource(R.color.white)
    val textColor = if (isPrimary) colorResource(R.color.white) else outlinedTextColor
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics { role = Role.Button }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = 6.dp)
                .clip(shape)
                .background(background)
                .then(
                    if (isPrimary) Modifier
                    else Modifier.border(1.5.dp, colorResource(R.color.orange), shape)
                ),
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            textAlign = TextAlign.Center,
            style = journeyText(QuicksandBold, 14.sp, textColor).copy(letterSpacing = 0.07.em),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF4EC)
@Composable
private fun HomeWelcomeJourneyPreview() {
    HomeWelcomeJourney(
        states = listOf(
            WelcomeJourneyState.DONE,
            WelcomeJourneyState.TODO,
            WelcomeJourneyState.SKIPPED,
            WelcomeJourneyState.TODO,
        ),
        onStepClick = {},
        onSkipClick = {},
        onHideRequested = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF4EC)
@Composable
private fun HomeWelcomeJourneyCompletePreview() {
    HomeWelcomeJourney(
        states = List(STEP_COUNT) { WelcomeJourneyState.DONE },
        onStepClick = {},
        onSkipClick = {},
        onHideRequested = {},
    )
}
