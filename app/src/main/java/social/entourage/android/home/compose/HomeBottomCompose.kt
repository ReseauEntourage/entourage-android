package social.entourage.android.home.compose

import android.net.Uri
import android.widget.ImageView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import social.entourage.android.R
import social.entourage.android.accessibility.a11yColor
import social.entourage.android.api.model.Category
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Suggestion
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold

/*
 * Bas de la home en Compose : cartes pédago, outils solidaires + bandeau de don, squelette de chargement,
 * cartes de suggestion « connexion » et « prochain pas » (ancienne version, hors moteur SuggestionFeature).
 * Chaque composable porte les marges extérieures de son item XML d'origine.
 */

// --- Styles communs ---------------------------------------------------------------------------

/** Les TextView ajoutent le « font padding » : on le conserve pour garder la même hauteur de ligne. */
@Suppress("DEPRECATION")
private val TextViewPlatformStyle = PlatformTextStyle(includeFontPadding = true)

private fun tvStyle(family: FontFamily, size: TextUnit, color: Color) = TextStyle(
    fontFamily = family,
    fontSize = size,
    color = color,
    platformStyle = TextViewPlatformStyle,
)

/** Fond `home_v2_action_item_shape` : remplissage blanc à angles droits + bordure beige 1 dp arrondie à 5 dp. */
@Composable
private fun Modifier.actionItemShape(): Modifier =
    this
        .background(Color.White)
        .border(1.dp, colorResource(R.color.beige), RoundedCornerShape(5.dp))

@Composable
private fun rememberNoIndication() = remember { MutableInteractionSource() }

/** Clic sans effet visuel (les ConstraintLayout / TextView d'origine n'ont pas de ripple). */
@Composable
private fun Modifier.plainClickable(onClick: () -> Unit): Modifier =
    this.clickable(interactionSource = rememberNoIndication(), indication = null, onClick = onClick)

// --- Pédago -----------------------------------------------------------------------------------

private data class PedagoTag(val text: String, val textColor: Color, val background: Color)

@Composable
private fun Category?.tag(): PedagoTag {
    val context = LocalContext.current
    val lightOrange = Color(context.a11yColor(R.attr.a11yTextLightOrange, R.color.light_orange))
    val beigeClair = colorResource(R.color.beige_clair)
    return when (this) {
        Category.ALL -> PedagoTag(stringResource(R.string.home_v2_pedago_item_tag_all), lightOrange, beigeClair)
        Category.ACT -> PedagoTag(
            stringResource(R.string.home_v2_pedago_item_tag_act),
            colorResource(R.color.pedago_text_act), colorResource(R.color.pedago_bg_act)
        )
        Category.INSPIRE -> PedagoTag(
            stringResource(R.string.home_v2_pedago_item_tag_inspire),
            colorResource(R.color.pedago_text_inspire), colorResource(R.color.pedago_bg_inspire)
        )
        Category.UNDERSTAND -> PedagoTag(
            stringResource(R.string.home_v2_pedago_item_tag_understand),
            colorResource(R.color.pedago_text_understand), colorResource(R.color.pedago_bg_understand)
        )
        null -> PedagoTag("", lightOrange, beigeClair)
    }
}

/** Pastille de catégorie : `home_v2_tv_tag_item_pedago_shape` (rayon 12 dp) + padding 4/6 dp. */
@Composable
private fun PedagoTagPill(tag: PedagoTag, modifier: Modifier = Modifier) {
    Text(
        text = tag.text,
        style = tvStyle(QuicksandBold, spRes(R.dimen.entourage_font_very_small), tag.textColor),
        modifier = modifier
            .background(tag.background, RoundedCornerShape(12.dp))
            .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
    )
}

/** Vignette : fond blanc arrondi 15 dp + Glide centerCrop / coins 16 px (comme l'adapter d'origine). */
@Composable
private fun PedagoImage(imageUrl: String?, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundResource(R.drawable.home_v2_pedago_iv_item_shape)
                importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        },
        update = { view ->
            val key = imageUrl ?: ""
            if (view.tag != key) {
                view.tag = key
                val glide = Glide.with(view)
                if (imageUrl != null) {
                    glide.load(Uri.parse(imageUrl))
                        .placeholder(R.drawable.ic_event_placeholder)
                        .transform(CenterCrop(), GranularRoundedCorners(16F, 16F, 16F, 16F))
                        .error(R.drawable.ic_event_placeholder)
                        .into(view)
                } else {
                    glide.load(R.drawable.ic_placeholder_action)
                        .transform(CenterCrop(), GranularRoundedCorners(16F, 16F, 16F, 16F))
                        .into(view)
                }
            }
        },
    )
}

/**
 * Carte pédago de la liste verticale « Découvrir des contenus » (`home_v2_pedago_item_layout`) :
 * 90 dp de haut, marges 16 / 10 / 16 dp. Affichée directement dans la liste verticale (pas de liste horizontale).
 */
@Composable
fun HomePedagoCard(pedago: Pedago, onClick: (Pedago) -> Unit) {
    val tag = pedago.category.tag()
    val grey = colorResource(R.color.grey)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
            .height(90.dp)
            .actionItemShape()
            .plainClickable {
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Article)
                onClick(pedago)
            },
    ) {
        PedagoImage(pedago.imageUrl, Modifier.width(90.dp).fillMaxHeight())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 90.dp + 10.dp, end = 16.dp),
        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PedagoTagPill(tag)
                pedago.duration?.let { duration ->
                    Image(
                        painter = painterResource(R.drawable.ic_time_grey),
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp).size(10.dp),
                    )
                    Text(
                        text = stringResource(R.string.home_v2_pedago_item_length_title, duration),
                        style = tvStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_very_small), grey),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            Text(
                text = pedago.name ?: "",
                style = tvStyle(QuicksandBold, spRes(R.dimen.entourage_font_small), colorResource(R.color.black)),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * Carte pédago « sensibilisation » (`home_v2_initial_pedago_item_layout`) : 300 x 130 dp, marge droite 10 dp et haute 10 dp.
 * C'est un item de LISTE HORIZONTALE (ancien wrapper `HomeHorizontalWrapperAdapter`) : le conteneur est fourni par l'appelant.
 */
@Composable
fun HomeInitialPedagoCard(pedago: Pedago, onClick: (Pedago) -> Unit) {
    val tag = pedago.category.tag()
    Box(
        modifier = Modifier
            .padding(end = 10.dp, top = 10.dp)
            .width(300.dp)
            .height(130.dp)
            .actionItemShape()
            .plainClickable {
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Article)
                onClick(pedago)
            },
    ) {
        PedagoImage(pedago.imageUrl, Modifier.width(90.dp).fillMaxHeight())
        Column(modifier = Modifier.fillMaxWidth().padding(start = 90.dp + 10.dp, top = 10.dp)) {
            PedagoTagPill(tag)
            Text(
                text = pedago.name ?: "",
                style = tvStyle(QuicksandBold, spRes(R.dimen.entourage_font_small), colorResource(R.color.black)),
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
        pedago.duration?.let { duration ->
            Text(
                text = stringResource(R.string.home_v2_pedago_item_length_title, duration),
                style = tvStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_very_small), colorResource(R.color.grey)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 90.dp + 10.dp, bottom = 10.dp),
            )
        }
    }
}

// --- Outils solidaires + bandeau de don --------------------------------------------------------

@Composable
private fun ToolCard(
    iconRes: Int,
    labelRes: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .heightIn(min = 140.dp)
            .actionItemShape()
            .plainClickable(onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFEF5EB), CircleShape)
                .padding(10.dp),
        ) {
            Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(labelRes),
                style = tvStyle(QuicksandBold, 14.sp, colorResource(R.color.black)),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Bloc « Vos outils solidaires » (`layout_home_tools`) : titre, 3 cartes égales, bandeau de don. */
@Composable
fun HomeTools(
    onMapClick: () -> Unit,
    onPedagoClick: () -> Unit,
    onCharterClick: () -> Unit,
    onDonateClick: () -> Unit,
) {
    val black = colorResource(R.color.black)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.home_title_tools),
            style = tvStyle(QuicksandBold, 18.sp, black),
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            ToolCard(
                iconRes = R.drawable.ic_button_home_carte,
                labelRes = R.string.home_tools_map,
                modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 8.dp),
                onClick = onMapClick,
            )
            ToolCard(
                iconRes = R.drawable.ic_button_home_pedago,
                labelRes = R.string.home_tools_pedago,
                modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 8.dp),
                onClick = onPedagoClick,
            )
            ToolCard(
                iconRes = R.drawable.ic_button_home_charte,
                labelRes = R.string.home_tools_charter,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onCharterClick,
            )
        }
        DonationBanner(onDonateClick)
    }
}

@Composable
private fun DonationBanner(onDonateClick: () -> Unit) {
    val context = LocalContext.current
    val buttonFill = Color(context.a11yColor(R.attr.a11yButtonFill, R.color.orange))
    val bannerShape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(Color.White, bannerShape)
            .border(1.dp, Color(0xFFFEEAE3), bannerShape)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFFEEAE3), CircleShape)
                .padding(10.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_donation_heart), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = stringResource(R.string.home_donation_banner_title),
                style = tvStyle(QuicksandBold, 16.sp, colorResource(R.color.black)),
            )
            Text(
                text = stringResource(R.string.home_donation_banner_text),
                style = tvStyle(NunitoSansRegular, 13.sp, colorResource(R.color.black)),
                modifier = Modifier.padding(top = 2.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(min = 44.dp)
                    .background(buttonFill, RoundedCornerShape(22.dp))
                    .plainClickable(onDonateClick)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.home_donation_banner_button),
                    style = tvStyle(QuicksandBold, 14.sp, Color.White),
                )
            }
        }
    }
}

// --- Squelette de chargement -------------------------------------------------------------------

private val SkeletonGrey = Color(0xFFE0E0E0)

/**
 * Reproduit `ShimmerFrameLayout` (facebook/shimmer 0.5.0, config par défaut : alpha shimmer base 0,3 / reflet 1,0,
 * inclinaison 20°, 1 000 ms, dropoff 0,5, linéaire, répétition infinie) : un masque DST_IN en dégradé qui glisse de gauche à droite.
 */
private fun Modifier.facebookShimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "home_skeleton_shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1000, easing = LinearEasing), RepeatMode.Restart),
        label = "home_skeleton_shimmer_progress",
    )
    this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val w = size.width
            val h = size.height
            val base = Color.White.copy(alpha = 0.3f)
            val highlight = Color.White
            val dx = -w + 2f * w * progress
            rotate(degrees = 20f, pivot = Offset(w / 2f, h / 2f)) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.25f to base,
                            0.4995f to highlight,
                            0.5005f to highlight,
                            0.75f to base,
                        ),
                        startX = dx,
                        endX = dx + w,
                    ),
                    topLeft = Offset(-2f * w, -2f * h),
                    size = Size(5f * w, 5f * h),
                    blendMode = BlendMode.DstIn,
                )
            }
        }
}

@Composable
private fun SkeletonItem() {
    Column(
        modifier = Modifier
            .padding(end = 16.dp)
            .width(160.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
            .padding(8.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(90.dp).background(SkeletonGrey))
        Box(Modifier.padding(top = 12.dp).fillMaxWidth().height(16.dp).background(SkeletonGrey))
        Box(Modifier.padding(top = 8.dp).width(100.dp).height(12.dp).background(SkeletonGrey))
        Box(Modifier.padding(top = 8.dp, bottom = 8.dp).width(80.dp).height(12.dp).background(SkeletonGrey))
    }
}

@Composable
private fun SkeletonSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 16.dp, bottom = 20.dp),
    ) {
        Box(Modifier.width(150.dp).height(20.dp).background(SkeletonGrey))
        Box(Modifier.padding(top = 8.dp).width(200.dp).height(14.dp).background(SkeletonGrey))
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .clipToBounds()
                .facebookShimmer(),
        ) {
            Row(modifier = Modifier.wrapContentWidth(Alignment.Start, unbounded = true)) {
                SkeletonItem()
                SkeletonItem()
                SkeletonItem()
            }
        }
    }
}

/** Squelette de chargement initial de la home : les 4 blocs de `HomeSkeletonAdapter` (`home_skeleton_horizontal_list`). */
@Composable
fun HomeSkeleton() {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(4) { SkeletonSection() }
    }
}

// --- Cartes de suggestion « connexion » et « prochain pas » ----------------------------------

/** Bouton `MaterialButton` (thème MaterialComponents) : vue de 48 dp min, fond de 36 dp (inset 6 dp), texte en majuscules. */
@Composable
private fun MdcButton(
    text: String,
    textColor: Color,
    family: FontFamily,
    modifier: Modifier = Modifier,
    fill: Color? = null,
    horizontalPadding: Dp = 16.dp,
    onClick: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .then(if (fill != null) Modifier.shadow(2.dp, shape) else Modifier)
                .clip(shape)
                .then(if (fill != null) Modifier.background(fill) else Modifier)
                .indication(source, ripple())
                .heightIn(min = 36.dp)
                .padding(horizontal = horizontalPadding, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text.uppercase(),
                style = tvStyle(family, spRes(R.dimen.entourage_font_small), textColor)
                    .copy(letterSpacing = 0.0892857143.em),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/** Carte « Dire bonjour » (`home_suggestion_connection_item`) : marges 16 / 8 dp, padding 16 dp. */
@Composable
fun HomeSuggestionConnectionCard(
    suggestion: Suggestion,
    onAction: (Suggestion) -> Unit,
    onDismiss: (Suggestion) -> Unit,
) {
    val context = LocalContext.current
    val userInfo = suggestion.suggestedUserInfo
    val greyDark = colorResource(R.color.grey_dark)
    val buttonFill = Color(context.a11yColor(R.attr.a11yButtonFill, R.color.orange))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .actionItemShape()
            .padding(16.dp),
    ) {
        // Avatar (48 dp, liseré light_orange 1 dp, padding 2 dp), puis prénom et raison à droite.
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, colorResource(R.color.light_orange), CircleShape)
                .padding(2.dp),
        ) {
            HomeGlideImage(
                url = userInfo?.avatarUrl,
                modifier = Modifier.fillMaxSize(),
                placeholder = R.drawable.placeholder_user,
                circleCrop = true,
            )
        }
        Column(modifier = Modifier.fillMaxWidth().padding(start = 48.dp + 12.dp)) {
            Text(
                text = userInfo?.firstName ?: "",
                style = tvStyle(QuicksandBold, spRes(R.dimen.entourage_font_medium), colorResource(R.color.black)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = suggestion.reason ?: "",
                style = tvStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_small), greyDark),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        // Les boutons sont accrochés au bas de l'avatar (et non de la raison), comme dans le XML.
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp + 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MdcButton(
                text = stringResource(R.string.suggestion_connection_cta),
                textColor = Color.White,
                family = NunitoSansBold,
                fill = buttonFill,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                onClick = { onAction(suggestion) },
            )
            MdcButton(
                text = stringResource(R.string.suggestion_dismiss),
                textColor = greyDark,
                family = NunitoSansRegular,
                horizontalPadding = 8.dp,
                onClick = { onDismiss(suggestion) },
            )
        }
    }
}

@Composable
private fun nextStepCtaLabel(action: String): String = stringResource(
    when (action) {
        "join_event" -> R.string.suggestion_next_step_cta_join_event
        "join_group" -> R.string.suggestion_next_step_cta_join_group
        "write_group" -> R.string.suggestion_next_step_cta_write_group
        "say_hello" -> R.string.suggestion_next_step_cta_say_hello
        "create_action" -> R.string.suggestion_next_step_cta_create_action
        "welcome_member" -> R.string.suggestion_next_step_cta_welcome_member
        "create_event" -> R.string.suggestion_next_step_cta_create_event
        else -> R.string.suggestion_next_step_cta_join_event
    }
)

/** Carte « Ton prochain pas » (`home_suggestion_next_step_item`, ancienne version) : marges 16 / 8 dp, padding 16 dp. */
@Composable
fun HomeSuggestionNextStepCard(
    suggestion: Suggestion,
    onAction: (Suggestion) -> Unit,
    onDismiss: (Suggestion) -> Unit,
) {
    val context = LocalContext.current
    val greyDark = colorResource(R.color.grey_dark)
    val buttonFill = Color(context.a11yColor(R.attr.a11yButtonFill, R.color.orange))
    val headerColor = Color(context.a11yColor(R.attr.a11yTextOrange, R.color.orange))
    val entourageInfo = suggestion.suggestedEntourageInfo
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .actionItemShape()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.suggestion_next_step_title).uppercase(),
            style = tvStyle(NunitoSansBold, spRes(R.dimen.entourage_font_very_small), headerColor),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = entourageInfo?.title ?: suggestion.reason ?: "",
            style = tvStyle(QuicksandBold, spRes(R.dimen.entourage_font_medium), colorResource(R.color.black)),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            text = suggestion.reason ?: "",
            style = tvStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_small), greyDark),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            MdcButton(
                text = nextStepCtaLabel(suggestion.suggestedAction),
                textColor = Color.White,
                family = NunitoSansBold,
                fill = buttonFill,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                onClick = { onAction(suggestion) },
            )
            MdcButton(
                text = stringResource(R.string.suggestion_dismiss),
                textColor = greyDark,
                family = NunitoSansRegular,
                horizontalPadding = 8.dp,
                onClick = { onDismiss(suggestion) },
            )
        }
    }
}
