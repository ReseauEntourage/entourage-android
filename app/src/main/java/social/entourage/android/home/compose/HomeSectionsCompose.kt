package social.entourage.android.home.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.ConfigurationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import kotlinx.coroutines.delay
import social.entourage.android.R
import social.entourage.android.accessibility.a11yColor
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionUtils
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Status
import social.entourage.android.events.EventsFragment
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.tools.displayDistance
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold

// ---------------------------------------------------------------------------------------------
// Styles de texte : les TextView gardent le « font padding », on le conserve pour la même hauteur de ligne.
// ---------------------------------------------------------------------------------------------

@Suppress("DEPRECATION")
private val TextViewPlatformStyle = PlatformTextStyle(includeFontPadding = true)

private fun textStyle(family: FontFamily, size: androidx.compose.ui.unit.TextUnit, color: Color, align: TextAlign = TextAlign.Start) =
    TextStyle(fontFamily = family, fontSize = size, color = color, textAlign = align, platformStyle = TextViewPlatformStyle)

/** Couleur d'un TextView sans `android:textColor` (celle du thème, `textColorPrimary`). */
@Composable
private fun defaultTextViewColor(): Color {
    val context = LocalContext.current
    return remember(context) {
        val attrs = context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
        val color = attrs.getColorStateList(0)?.defaultColor ?: 0xDE000000.toInt()
        attrs.recycle()
        Color(color)
    }
}

/** Direction de texte appliquée par les adapters d'origine : RTL pour l'arabe, LTR sinon (jamais celle du parent). */
@Composable
private fun HomeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    forceDirection: Boolean = true,
) {
    val context = LocalContext.current
    val isArabic = ConfigurationCompat.getLocales(context.resources.configuration)[0]?.language == "ar"
    val text0 = @Composable {
        BasicText(
            text = text,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            minLines = minLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
    if (forceDirection) {
        CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
        ) { text0() }
    } else {
        text0()
    }
}

// ---------------------------------------------------------------------------------------------
// Image Glide avec les transformations exactes des anciens adapters
// ---------------------------------------------------------------------------------------------

@Composable
private fun HomeCardImage(
    model: Any?,
    modifier: Modifier,
    scaleType: ImageView.ScaleType,
    placeholder: Int? = null,
    error: Int? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    circleCrop: Boolean = false,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                this.scaleType = scaleType
                importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        },
        update = { view ->
            var request = Glide.with(view.context).load(model)
            if (placeholder != null) request = request.placeholder(placeholder)
            if (error != null) request = request.error(error)
            if (circleCrop) request = request.circleCrop()
            if (transformations.isNotEmpty()) {
                request = request.transform(*transformations.toTypedArray())
            }
            request.into(view)
        },
    )
}

// ---------------------------------------------------------------------------------------------
// En-tête et bouton de section
// ---------------------------------------------------------------------------------------------

/** `home_section_header.xml`. */
@Composable
fun HomeSectionHeader(title: String, subtitle: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 28.dp), // entourage_marginstart / 16dp / entourage_margin_section
    ) {
        HomeText(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = textStyle(QuicksandBold, spRes(R.dimen.entourage_font_large), colorResource(R.color.grey_dark)),
            forceDirection = false,
        )
        if (subtitle != null) {
            HomeText(
                text = subtitle,
                modifier = Modifier.fillMaxWidth(),
                style = textStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_small), defaultTextViewColor()),
                forceDirection = false,
            )
        }
    }
}

/** `home_section_button.xml` : « Voir plus » aligné à droite, avec la flèche orange claire. */
@Composable
fun HomeSectionButton(text: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val orange = Color(context.a11yColor(R.attr.a11yTextLightOrange, R.color.light_orange))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Comme `itemView.setOnClickListener` : pas de ripple.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(top = 10.dp, end = 16.dp, bottom = 10.dp), // entourage_little_margin_top / entourage_margin_end
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top,
    ) {
        HomeText(
            text = text,
            modifier = Modifier.padding(end = 10.dp), // entourage_little_margin_end
            style = textStyle(QuicksandBold, spRes(R.dimen.entourage_font_small), orange),
            forceDirection = false,
        )
        Image(
            painter = painterResource(R.drawable.arrow_light_orange),
            contentDescription = null, // importantForAccessibility="no"
            modifier = Modifier.size(25.dp),
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Liste horizontale (home_horizontal_recycler_view.xml) et animation d'entrée des items
// ---------------------------------------------------------------------------------------------

/** Mémorise les items déjà animés (l'animation d'un RecyclerView ne rejoue pas au défilement). */
private class EntranceState {
    val animated = mutableSetOf<Int>()
}

private val LocalEntranceState = androidx.compose.runtime.compositionLocalOf<EntranceState?> { null }

/** `DecelerateInterpolator(1f)` : 1 - (1 - t)². */
private val DecelerateEasing = Easing { t -> 1f - (1f - t) * (1f - t) }

/**
 * Liste horizontale : `marginTop=20dp`, `paddingStart=16dp` sans rognage (`clipToPadding=false`).
 * Les items s'ajoutent avec [homeActionCards], [homeEventCards] ou [homeGroupCards] : chaque item entre
 * par `layout_animation_slide_from_right` (glissement de 20 % depuis la droite + fondu, 300 ms,
 * décalage de 15 % de la durée, soit 45 ms, entre deux items).
 */
@Composable
fun HomeHorizontalList(content: LazyListScope.() -> Unit) {
    val entrance = remember { EntranceState() }
    CompositionLocalProvider(LocalEntranceState provides entrance) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            contentPadding = PaddingValues(start = 16.dp),
            content = content,
        )
    }
}

@Composable
private fun SlideFromRight(index: Int, content: @Composable () -> Unit) {
    val entrance = LocalEntranceState.current
    val alreadyDone = entrance?.animated?.contains(index) == true
    val progress = remember { Animatable(if (alreadyDone || entrance == null) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (progress.value < 1f) {
            entrance?.animated?.add(index)
            delay(index * 45L)
            progress.animateTo(1f, tween(durationMillis = 300, easing = DecelerateEasing))
        }
    }
    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress.value
            translationX = size.width * 0.2f * (1f - progress.value)
        },
    ) { content() }
}

private fun <T> LazyListScope.homeCards(items: List<T>, itemContent: @Composable (T) -> Unit) {
    items(count = items.size, contentType = { "homeCard" }) { index ->
        SlideFromRight(index) { itemContent(items[index]) }
    }
}

fun LazyListScope.homeActionCards(actions: List<Action>, isContrib: Boolean) =
    homeCards(actions) { HomeActionCard(it, isContrib) }

fun LazyListScope.homeEventCards(events: List<Events>) = homeCards(events) { HomeEventCard(it) }

fun LazyListScope.homeGroupCards(groups: List<Group>) = homeCards(groups) { HomeGroupCard(it) }

// ---------------------------------------------------------------------------------------------
// Carte d'entraide / contribution (home_v2_action_item_layout.xml + HomeActionAdapter)
// ---------------------------------------------------------------------------------------------

/** Aligne un contenu plus haut que [reference] comme une contrainte « centré sur » : seule la moitié du débord compte dans la hauteur. */
private fun Modifier.centeredOn(reference: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0))
    val ref = reference.roundToPx()
    val diff = placeable.height - ref
    layout(placeable.width, ref + if (diff > 0) diff / 2 else 0) {
        placeable.placeRelative(0, -diff / 2)
    }
}

@Composable
fun HomeActionCard(action: Action, isContrib: Boolean) {
    val context = LocalContext.current
    val orangeText = Color(context.a11yColor(R.attr.a11yTextOrange, R.color.orange))
    val black = colorResource(R.color.black)
    val beige = colorResource(R.color.beige)
    val cardShape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .padding(start = 4.dp, top = 10.dp) // entourage_little_margin_five / entourage_little_margin_top
            .width(280.dp) // home_action_card_width
            // home_v2_action_item_shape : fond blanc carré (rayon 0) + bordure beige 1dp à coins de 5dp.
            .background(Color.White)
            .border(1.dp, beige, cardShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (isContrib) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Contrib_Detail)
                } else {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Demand_Detail)
                }
                context.startActivity(
                    Intent(context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE, action.title)
                        .putExtra(Const.IS_ACTION_DEMAND, !isContrib)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine()),
                )
            }
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Avatar + nom + catégorie. Hauteur fixée à celle de l'avatar : le titre se place sous l'avatar.
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                val avatarUrl = action.author?.avatarURLAsString
                HomeCardImage(
                    model = avatarUrl?.let { Uri.parse(it) } ?: R.drawable.placeholder_user,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, colorResource(R.color.light_orange), CircleShape) // home_user_avatar_border
                        .padding(2.dp),
                    scaleType = ImageView.ScaleType.CENTER_CROP,
                    placeholder = if (avatarUrl != null) R.drawable.placeholder_user else null,
                    error = if (avatarUrl != null) R.drawable.placeholder_user else null,
                    transformations = listOf(CircleCrop()),
                )
                Column(
                    modifier = Modifier
                        .padding(start = 64.dp) // 48dp d'avatar + entourage_little_margin_start (16dp)
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.Top, unbounded = true),
                ) {
                    HomeText(
                        text = action.author?.userName.orEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        style = textStyle(QuicksandBold, spRes(R.dimen.entourage_font_medium), black),
                        maxLines = 1,
                    )
                    val category = action.sectionName?.let { ActionUtils.showTagTranslated(context, it) }.orEmpty()
                    // La pastille reste présente (vide) quand la catégorie manque, comme le TextView d'origine.
                    HomeText(
                        text = category,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(beige, RoundedCornerShape(14.dp)) // bg_home_action_category
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        style = textStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_very_small), orangeText),
                    )
                }
            }
            HomeText(
                text = action.title.orEmpty(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                style = textStyle(QuicksandBold, spRes(R.dimen.entourage_font_medium), black),
                minLines = 2,
                maxLines = 2,
            )
            HomeText(
                text = action.description?.replaceFirstChar { it.uppercase() }.orEmpty(),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                style = textStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_small), colorResource(R.color.grey_dark)),
                minLines = 2,
                maxLines = 2,
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_location_gds_detail),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.orange)),
                )
                HomeText(
                    text = if (action.distance != null) action.displayDistance(context) else "",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(1f)
                        .centeredOn(14.dp),
                    style = textStyle(NunitoSansRegular, spRes(R.dimen.entourage_font_very_small), orangeText),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Carte d'événement (home_v2_event_item_layout.xml + HomeEventAdapter)
// ---------------------------------------------------------------------------------------------

private val UrgencyColor = Color(0xFFD53F00)
private val TagFemaleColor = Color(0xFF7C3AED)
private val SeparatorColor = Color(0xFFF5F5F5)

@Composable
fun HomeEventCard(event: Events) {
    val context = LocalContext.current
    val black = colorResource(R.color.black)
    val grey = colorResource(R.color.grey)
    val shape = RoundedCornerShape(16.dp)

    val isCanceled = event.status == Status.CLOSED || event.status == Status.CANCELLED
    val isReservedFemale = event.metadata?.reserved_female == true
    val isEntourageEvent = event.author?.communityRoles?.let {
        it.contains("Équipe Entourage") || it.contains("Animateur Entourage")
    } == true

    Box(
        modifier = Modifier
            .padding(start = 5.dp, top = 4.dp, bottom = 4.dp)
            .width(290.dp)
            // CardView : élévation 2dp, coins 16dp, fond blanc, contenu rogné sur l'arrondi.
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                EventsFragment.isFromDetails = true
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Event_Detail)
                EventFeedActivity.isFromMyEvent = true
                context.startActivity(
                    Intent(context, EventFeedActivity::class.java).putExtra(Const.EVENT_ID, event.id),
                )
            },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- Image 150dp + calques ---
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val landscape = event.metadata?.landscapeUrl
                HomeCardImage(
                    model = landscape?.let { Uri.parse(it) } ?: R.drawable.ic_event_placeholder,
                    modifier = Modifier.fillMaxSize(),
                    scaleType = ImageView.ScaleType.CENTER_CROP,
                    placeholder = if (landscape != null) R.drawable.ic_event_placeholder else null,
                    error = if (landscape != null) R.drawable.ic_event_placeholder else null,
                    transformations = listOf(CenterCrop()),
                )
                if (isCanceled) {
                    Box(Modifier.fillMaxSize().background(colorResource(R.color.background_transparent)))
                    // EN-9334 : badge « Annulé »
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .background(colorResource(R.color.onboard_black_36), RoundedCornerShape(20.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_event_canceled),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp).size(10.dp),
                            colorFilter = ColorFilter.tint(Color.White),
                        )
                        HomeText(
                            text = stringResource(R.string.event_canceled_badge),
                            style = textStyle(QuicksandBold, 10.sp, Color.White),
                            forceDirection = false,
                        )
                    }
                }
                Row(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                    if (isReservedFemale) {
                        // Le tag « femmes » garde sa marge de 6dp même quand le tag Entourage est absent.
                        HomeText(
                            text = "Réservé aux femmes",
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .background(TagFemaleColor, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = textStyle(QuicksandBold, 10.sp, Color.White),
                            forceDirection = false,
                        )
                    } else if (isEntourageEvent) {
                        val buttonFill = Color(context.a11yColor(R.attr.a11yButtonFill, R.color.orange))
                        HomeText(
                            text = "Événement Entourage",
                            modifier = Modifier
                                .background(buttonFill, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = textStyle(QuicksandBold, 10.sp, Color.White),
                            forceDirection = false,
                        )
                    }
                }
            }

            // --- Infos ---
            Column(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)) {
                val placeLimit = event.metadata?.placeLimit
                val membersCount = event.membersCount
                if (placeLimit != null && placeLimit > 0 && membersCount != null) {
                    val remaining = placeLimit - membersCount
                    if (remaining in 1..5) {
                        HomeText(
                            text = if (remaining == 1) "Plus qu'une place" else "Plus que $remaining places",
                            modifier = Modifier.padding(bottom = 6.dp),
                            style = textStyle(QuicksandBold, 11.sp, UrgencyColor),
                            forceDirection = false,
                        )
                    }
                }
                HomeText(
                    text = event.title.orEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    style = textStyle(QuicksandBold, 15.sp, if (isCanceled) grey else black),
                    minLines = 2,
                    maxLines = 2,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_date_grey),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp).size(15.dp),
                    )
                    HomeText(
                        text = event.metadata?.startsAt?.let { Utils.formatEventDateWithTime(it, context) }.orEmpty(),
                        style = textStyle(NunitoSansRegular, 14.sp, grey),
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_location_grey),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp).size(15.dp),
                    )
                    HomeText(
                        text = event.metadata?.displayAddress?.let { it.split(",").lastOrNull()?.trim() ?: it }.orEmpty(),
                        modifier = Modifier.weight(1f, fill = false),
                        style = textStyle(NunitoSansRegular, 14.sp, grey),
                        maxLines = 1,
                    )
                }
                // Séparateur de la ligne sociale
                Box(Modifier.fillMaxWidth().padding(top = 10.dp).height(1.dp).background(SeparatorColor))
                val totalCount = event.membersCount ?: 0
                if (totalCount > 0) {
                    EventSocialRow(event, totalCount, grey)
                }
            }
        }
    }
}

/** Avatars qui se chevauchent (décalage 17dp, 24dp de diamètre) + nombre de participants. */
@Composable
private fun EventSocialRow(event: Events, totalCount: Int, grey: Color) {
    val members = event.members ?: emptyList()
    val visible = (0..2).count { members.getOrNull(it) != null }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Largeur du FrameLayout : celle de l'avatar le plus à droite encore visible.
        Box(modifier = Modifier.padding(end = 8.dp).height(24.dp).width(if (visible == 0) 0.dp else (17 * (visible - 1) + 24).dp)) {
            // Ordre de dessin d'origine : membre 3 (dessous), 2, puis 1 (dessus).
            for (i in listOf(2, 1, 0)) {
                val member = members.getOrNull(i) ?: continue
                HomeCardImage(
                    model = member.avatarUrl,
                    modifier = Modifier.padding(start = (17 * i).dp).size(24.dp),
                    scaleType = ImageView.ScaleType.FIT_CENTER,
                    placeholder = R.drawable.placeholder_user,
                    error = R.drawable.placeholder_user,
                    circleCrop = true,
                )
            }
        }
        HomeText(
            text = pluralStringResource(R.plurals.number_of_people, totalCount, totalCount),
            style = textStyle(NunitoSansRegular, 13.sp, grey),
            forceDirection = false,
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Carte de groupe (home_v2_group_item_layout.xml + HomeGroupAdapter)
// ---------------------------------------------------------------------------------------------

@Composable
fun HomeGroupCard(group: Group) {
    val context = LocalContext.current
    val beige = colorResource(R.color.beige)
    val cardShape = RoundedCornerShape(15.dp)
    // Le clic masque la pastille (INVISIBLE) et remet le compteur à zéro.
    var badgeHidden by remember(group) { mutableStateOf(false) }
    val unread = group.unreadPostsCount
    // Sans compteur, la pastille garde sa visibilité et son texte par défaut du XML (« 1 »).
    val badgeVisible = !badgeHidden && (unread == null || unread > 0)

    // Conteneur : paddingTop 8dp, paddingEnd 2dp ; la pastille déborde de 6dp à droite et 10dp en haut de la carte.
    Box(modifier = Modifier.padding(top = 8.dp, end = 2.dp)) {
        Box(
            modifier = Modifier
                .padding(start = 4.dp, top = 10.dp)
                .size(width = 160.dp, height = 130.dp)
                // home_v2_item_group_shape : fond blanc + bordure beige 1dp, coins de 15dp.
                .background(Color.White, cardShape)
                .border(1.dp, beige, cardShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Group_Detail)
                    group.unreadPostsCount = 0
                    badgeHidden = true
                    context.startActivity(Intent(context, GroupFeedActivity::class.java).putExtra(Const.GROUP_ID, group.id))
                },
        ) {
            val imageUrl = group.imageUrl
            HomeCardImage(
                model = imageUrl?.let { Uri.parse(it) } ?: R.drawable.ic_event_placeholder,
                modifier = Modifier.fillMaxWidth().height(70.dp),
                scaleType = ImageView.ScaleType.CENTER_CROP,
                placeholder = if (imageUrl != null) R.drawable.ic_event_placeholder else null,
                error = if (imageUrl != null) R.drawable.ic_event_placeholder else null,
                // Rayons en pixels (15F), comme GranularRoundedCorners dans l'adapter ; le repli a des coins inversés.
                transformations = listOf(
                    CenterCrop(),
                    if (imageUrl != null) GranularRoundedCorners(15f, 15f, 0f, 0f) else GranularRoundedCorners(15f, 0f, 0f, 15f),
                ),
            )
            // Titre centré dans la zone sous l'image (60dp), 2 lignes réservées.
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 70.dp).height(60.dp),
                contentAlignment = Alignment.Center,
            ) {
                HomeText(
                    text = group.name.orEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    style = textStyle(QuicksandBold, spRes(R.dimen.entourage_font_small), colorResource(R.color.black), TextAlign.Center),
                    minLines = 2,
                    maxLines = 2,
                )
            }
        }
        if (badgeVisible) {
            val badgeText = when {
                unread == null -> "1"
                unread > 9 -> "9+"
                else -> unread.toString()
            }
            Box(
                modifier = Modifier
                    // Coin haut-droit de la carte (4dp + 160dp), décalé de +6dp à droite et -10dp en haut.
                    .padding(start = 4.dp + 160.dp + 6.dp - 24.dp, top = 10.dp - 10.dp)
                    .size(24.dp)
                    .background(Color(context.a11yColor(R.attr.a11yBadgeFill, R.color.tomato)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                HomeText(
                    text = badgeText,
                    modifier = Modifier.padding(2.dp).wrapContentSize(),
                    style = textStyle(QuicksandBold, 12.sp, Color.White, TextAlign.Center),
                    forceDirection = false,
                )
            }
        }
    }
}
