package social.entourage.android.actions.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import social.entourage.android.R
import social.entourage.android.ui.theme.EntourageComposeStyles
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold

@Composable
fun CreateActionCharterScreen(
    isDemand: Boolean,
    onBackClick: () -> Unit,
    onReadFullCharterClick: () -> Unit,
    onAcceptClick: () -> Unit,
) {
    val examples = if (isDemand) {
        listOf(
            "🧥" to stringResource(R.string.action_cgu_ask_example_1),
            "🧺" to stringResource(R.string.action_cgu_ask_example_2),
            "☕" to stringResource(R.string.action_cgu_ask_example_3),
        )
    } else {
        listOf(
            "🧺" to stringResource(R.string.action_cgu_give_example_1),
            "☕" to stringResource(R.string.action_cgu_give_example_2),
            "🗣️" to stringResource(R.string.action_cgu_give_example_3),
            "🧥" to stringResource(R.string.action_cgu_give_example_4),
        )
    }

    val limits = if (isDemand) {
        listOf(
            stringResource(R.string.action_cgu_ask_limit_1),
            stringResource(R.string.action_cgu_ask_limit_2),
            stringResource(R.string.action_cgu_ask_limit_3),
            stringResource(R.string.action_cgu_ask_limit_4),
        )
    } else {
        listOf(
            stringResource(R.string.action_cgu_give_limit_1),
            stringResource(R.string.action_cgu_give_limit_2),
            stringResource(R.string.action_cgu_give_limit_3),
            stringResource(R.string.action_cgu_give_limit_4),
        )
    }

    val limitsFootnote = stringResource(
        if (isDemand) R.string.action_cgu_ask_limit_footnote else R.string.action_cgu_give_limit_footnote
    )

    val consentItems = listOf(
        "🤲" to stringResource(R.string.action_cgu_consent_1),
        "🔒" to stringResource(R.string.action_cgu_consent_2),
        "🤝" to stringResource(R.string.action_cgu_consent_3),
    )

    Box(modifier = Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
        Image(
            painter = painterResource(R.drawable.header_profile_orange),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            CharterHeader(onBackClick)

            // EN-9620 : retour Gwen — le lien "Lire la charte complète" et le CTA ne sont plus
            // dans un footer sticky rapporté sous la feuille : ils terminent le contenu
            // scrollable (plus de couture visible, et il faut atteindre le bas pour valider).
            // Un fondu en haut/bas de la zone scrollable évite que le contenu soit coupé net.
            val sheetColor = colorResource(R.color.light_beige)
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(35.dp))
                    .background(sheetColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("charter_scroll")
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    CharterBanner(isDemand)

                    Spacer(Modifier.height(18.dp))
                    CharterDotHeader(
                        iconRes = R.drawable.ic_check_green,
                        title = stringResource(R.string.action_cgu_examples_header)
                    )
                    Spacer(Modifier.height(10.dp))
                    CharterBulletCard(examples)

                    Spacer(Modifier.height(18.dp))
                    CharterDotHeader(
                        symbol = "!",
                        dotBackgroundColor = colorResource(R.color.charter_dot_background),
                        dotTextColor = colorResource(R.color.custom_button_accent_pressed),
                        title = stringResource(R.string.action_cgu_limits_header)
                    )
                    Spacer(Modifier.height(10.dp))
                    CharterLimitsCard(limits, limitsFootnote)

                    if (isDemand) {
                        Spacer(Modifier.height(18.dp))
                        CharterDotHeader(
                            symbol = "♥",
                            dotBackgroundColor = colorResource(R.color.charter_dot_background),
                            dotTextColor = colorResource(R.color.custom_button_accent_pressed),
                            title = stringResource(R.string.action_cgu_consent_header)
                        )
                        Spacer(Modifier.height(10.dp))
                        CharterBulletCard(consentItems)
                    } else {
                        Spacer(Modifier.height(18.dp))
                        CharterSpiritCard()
                    }

                    Spacer(Modifier.height(24.dp))
                    CharterFooter(onReadFullCharterClick, onAcceptClick)
                }

                ScrollFade(
                    color = sheetColor,
                    fromTop = true,
                    visible = scrollState.canScrollBackward,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                ScrollFade(
                    color = sheetColor,
                    fromTop = false,
                    visible = scrollState.canScrollForward,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/** Masque dégradé (couleur de la feuille → transparent) posé sur un bord de la zone scrollable. */
@Composable
private fun ScrollFade(color: Color, fromTop: Boolean, visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    val colors = if (fromTop) listOf(color, Color.Transparent) else listOf(Color.Transparent, color)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(Brush.verticalGradient(colors))
    )
}

@Composable
private fun CharterHeader(onBackClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 15.dp, vertical = 8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_arrow_back_white),
            contentDescription = null,
            modifier = Modifier
                .testTag("icon_back")
                .size(40.dp)
                .clickable(onClick = onBackClick)
                .padding(10.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.action_cgu_header_title),
            style = EntourageComposeStyles.h1,
            modifier = Modifier.weight(1f)
        )
        // EN-9620 : retour Gwen — picto "mains dans le cœur" retiré du header.
    }
}

@Composable
private fun CharterBanner(isDemand: Boolean) {
    val icon = if (isDemand) "🙋" else "🤝"
    val eyebrow = stringResource(if (isDemand) R.string.action_cgu_ask_eyebrow else R.string.action_cgu_give_eyebrow)
    val title = stringResource(if (isDemand) R.string.action_cgu_ask_title else R.string.action_cgu_give_title)
    val subtitle = stringResource(if (isDemand) R.string.action_cgu_ask_subtitle else R.string.action_cgu_give_subtitle)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.beige), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(colorResource(R.color.orange), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 22.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow,
                style = TextStyle(
                    fontFamily = QuicksandBold,
                    fontSize = 11.sp,
                    color = colorResource(R.color.custom_button_accent_pressed),
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                style = TextStyle(fontFamily = QuicksandBold, fontSize = 17.sp, color = colorResource(R.color.pre_onboard_black)),
                modifier = Modifier.testTag("banner_title")
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = NunitoSansRegular,
                    fontSize = 13.sp,
                    color = colorResource(R.color.grey),
                    lineHeight = 17.sp
                )
            )
        }
    }
}

@Composable
private fun CharterDotHeader(
    title: String,
    iconRes: Int? = null,
    symbol: String? = null,
    dotBackgroundColor: Color? = null,
    dotTextColor: Color = Color.Unspecified,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            iconRes != null -> Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            symbol != null && dotBackgroundColor != null -> Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(dotBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = symbol, style = TextStyle(fontFamily = QuicksandBold, fontSize = 13.sp, color = dotTextColor))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = TextStyle(fontFamily = QuicksandBold, fontSize = 14.5.sp, color = colorResource(R.color.pre_onboard_black))
        )
    }
}

@Composable
private fun CharterBulletCard(items: List<Pair<String, String>>) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            items.forEachIndexed { index, (emoji, text) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Text(text = emoji, fontSize = 17.sp, modifier = Modifier.width(26.dp))
                    Text(
                        text = text,
                        style = TextStyle(
                            fontFamily = NunitoSansRegular,
                            fontSize = 13.5.sp,
                            color = colorResource(R.color.pre_onboard_black),
                            lineHeight = 17.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(color = colorResource(R.color.grey_thin), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun CharterLimitsCard(items: List<String>, footnote: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.beige), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        items.forEach { text ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Text(
                    text = "✕",
                    style = TextStyle(fontFamily = QuicksandBold, fontSize = 14.sp, color = colorResource(R.color.custom_button_accent_pressed)),
                    modifier = Modifier.width(20.dp)
                )
                Text(
                    text = text,
                    style = TextStyle(
                        fontFamily = NunitoSansRegular,
                        fontSize = 13.sp,
                        color = colorResource(R.color.grey),
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        HorizontalDivider(
            color = colorResource(R.color.grey_thin),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = footnote,
            style = TextStyle(
                fontFamily = NunitoSansRegular,
                fontSize = 12.sp,
                color = colorResource(R.color.grey),
                lineHeight = 15.sp
            )
        )
    }
}

@Composable
private fun CharterSpiritCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.green_light), RoundedCornerShape(16.dp))
            .padding(15.dp)
    ) {
        Text(text = "✨", fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.action_cgu_spirit_text),
            style = TextStyle(
                fontFamily = NunitoSansBold,
                fontSize = 13.sp,
                color = colorResource(R.color.green),
                lineHeight = 17.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CharterFooter(onReadFullCharterClick: () -> Unit, onAcceptClick: () -> Unit) {
    val prefix = stringResource(R.string.action_cgu_read_more_prefix)
    val link = stringResource(R.string.action_cgu_read_more_link)
    val linkColor = colorResource(R.color.custom_button_accent_pressed)

    val hint = buildAnnotatedString {
        append(prefix)
        append(" ")
        withLink(LinkAnnotation.Clickable(tag = "read_more", linkInteractionListener = { onReadFullCharterClick() })) {
            withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                append(link)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = hint,
            style = TextStyle(fontFamily = NunitoSansRegular, fontSize = 12.sp, color = colorResource(R.color.grey), textAlign = TextAlign.Center),
            modifier = Modifier.testTag("read_full_charter").padding(bottom = 9.dp)
        )
        Button(
            onClick = onAcceptClick,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange)),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.testTag("accept").heightIn(min = 48.dp)
        ) {
            Text(text = stringResource(R.string.action_cgu_accept_button), style = EntourageComposeStyles.h2White)
        }
    }
}
