package social.entourage.android.home.compose

import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import social.entourage.android.R
import social.entourage.android.suggestion.HomeSuggestionSection
import social.entourage.android.ui.theme.QuicksandBold

/** Marge haute du bloc d'en-tête, dépliée / repliée (en pixels, comme `DEFAULT_MARGIN` / `NEW_MARGIN` de l'ancienne home). */
private const val HEADER_MARGIN_DEFAULT_PX = 80
private const val HEADER_MARGIN_COLLAPSED_PX = 10

/** Écran d'accueil : en-tête (fond, avatar, logo, titre, cloche) + liste au fond arrondi, rognée sur l'arrondi. */
@Composable
fun HomeScreen(
    ui: HomeUiState,
    actions: HomeActions,
    suggestionSection: HomeSuggestionSection?,
) {
    val listState = rememberLazyListState()

    HeaderCollapseEffect(ui, listState)
    LaunchedEffect(ui.scrollToJourneyRequest) {
        if (ui.scrollToJourneyRequest > 0) listState.scrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        // iv_fond_home : `header` en fitXY, largeur du parent, hauteur intrinsèque (400 dp)
        Image(
            painter = painterResource(R.drawable.header),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.TopStart),
        )

        Column(Modifier.fillMaxSize()) {
            HomeHeader(ui, actions)

            // rv_home : marginTop 10 dp, fond arrondi (35 dp en haut) qui rogne le contenu (clipToOutline)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.entourage_little_margin_top))
                    .clip(RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp))
                    .background(colorResource(R.color.very_very_clear_orange))
            ) {
                HomeList(ui, actions, suggestionSection, listState)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// En-tête
// ---------------------------------------------------------------------------------------------

/**
 * Replie / déplie le bloc d'en-tête au défilement, comme `setRecyclerViewScrollListener` : sous 50 px de défilement
 * le titre revient et la marge repasse à 80 px ; au-delà, en défilant vers le bas, le titre disparaît et la marge
 * passe à 10 px (animation de 100 ms). Tant que rien n'a défilé, la marge reste à 0 comme dans le XML.
 */
@Composable
private fun HeaderCollapseEffect(ui: HomeUiState, listState: LazyListState) {
    LaunchedEffect(listState) {
        var lastScrollY = 0
        snapshotFlow {
            if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else Int.MAX_VALUE / 2
        }.collect { scrollY ->
            val dy = scrollY - lastScrollY
            lastScrollY = scrollY
            if (dy == 0) return@collect
            when {
                scrollY == 0 -> {
                    if (ui.headerTopMarginPx != HEADER_MARGIN_DEFAULT_PX) ui.headerTopMarginPx = HEADER_MARGIN_DEFAULT_PX
                    if (!ui.titleVisible) ui.titleVisible = true
                }
                scrollY > 50 && dy > 0 && ui.titleVisible -> animateHeader(ui, collapse = true)
                scrollY <= 50 && dy < 0 && !ui.titleVisible -> animateHeader(ui, collapse = false)
            }
        }
    }
}

/** `ValueAnimator.ofInt` de 100 ms (interpolateur accélération/décélération par défaut) ; le titre bascule dès la première image. */
private suspend fun animateHeader(ui: HomeUiState, collapse: Boolean) {
    val from = ui.headerTopMarginPx.toFloat()
    val to = (if (collapse) HEADER_MARGIN_COLLAPSED_PX else HEADER_MARGIN_DEFAULT_PX).toFloat()
    ui.titleVisible = !collapse
    val progress = Animatable(0f)
    progress.animateTo(1f, tween(durationMillis = 100, easing = AccelerateDecelerate)) {
        ui.headerTopMarginPx = (from + (to - from) * value).roundToInt()
    }
    ui.headerTopMarginPx = to.roundToInt()
}

private val AccelerateDecelerate = Easing { t -> ((cos((t + 1) * PI) / 2.0) + 0.5).toFloat() }

@Composable
private fun HomeHeader(ui: HomeUiState, actions: HomeActions) {
    val density = LocalDensity.current
    val statusBarTop = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
    val marginStart = dimensionResource(R.dimen.entourage_marginstart)
    val marginEnd = dimensionResource(R.dimen.entourage_margin_end)
    val profileDescription = stringResource(R.string.a11y_my_profile)
    val notifDescription = stringResource(R.string.notifications)

    Column(Modifier.fillMaxWidth()) {
        // Marge animée lue dans la phase de layout : l'animation ne recompose pas l'en-tête à chaque image.
        Spacer(
            Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = 0))
                layout(placeable.width, ui.headerTopMarginPx.coerceAtLeast(0)) { }
            }
        )
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = statusBarTop)
        ) {
            // avatar : 48 dp, collé en bas du bloc, marge de début 16 dp
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = marginStart)
                    .size(48.dp)
                    .semantics { contentDescription = profileDescription }
                    .clickable(onClick = actions.onAvatarClick)
            ) {
                HomeGlideImage(
                    url = ui.avatarUrl,
                    modifier = Modifier.size(48.dp),
                    placeholder = R.drawable.placeholder_user,
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER,
                    circleCrop = true,
                )
            }

            // logo : centré dans l'espace à droite de la marge de début
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(start = marginStart),
                contentAlignment = Alignment.TopCenter,
            ) {
                val logoModifier = actions.onLogoClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
                Image(
                    painter = painterResource(R.drawable.logo_entourage),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = logoModifier,
                )
            }

            // titre : entre l'avatar (16 + 48) et la cloche (48 + 16), sous le logo (29 dp)
            if (ui.titleVisible) {
                Text(
                    text = stringResource(R.string.home_title),
                    color = Color.White,
                    fontFamily = QuicksandBold,
                    fontSize = spRes(R.dimen.entourage_font_very_small),
                    textAlign = TextAlign.Center,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(top = 29.dp, start = marginStart + 48.dp, end = marginEnd + 48.dp),
                )
            }

            // cloche : cible de 48 dp, marge haute 4 dp, marge de fin 16 dp
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = marginEnd)
                    .size(48.dp)
                    .drawableBackground(R.drawable.bg_partner_logo_inset6)
                    .semantics { contentDescription = notifDescription }
                    .clickable(onClick = actions.onNotifClick),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_notif_off),
                    contentDescription = null,
                    modifier = Modifier.size(width = 24.dp, height = 22.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // pastille de compteur : 18 dp, 11 dp du bord droit, 25 dp du bas (invisible = hors TalkBack)
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 11.dp, bottom = 25.dp)
                    .size(18.dp)
                    .then(
                        if (ui.notifBadgeVisible) Modifier.drawableBackground(R.drawable.bg_main_filter_item_number_shape)
                        else Modifier.clearAndSetSemantics { }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (ui.notifBadgeVisible) {
                    Text(
                        text = ui.notifBadgeText,
                        color = Color.White,
                        fontFamily = QuicksandBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true)),
                    )
                }
            }
        }
    }
}

/** Fond défini par un drawable (shape, inset, attributs de thème `?attr/a11y...`), dessiné tel quel comme un `android:background`. */
@Composable
internal fun Modifier.drawableBackground(@DrawableRes id: Int): Modifier {
    val context = LocalContext.current
    val drawable = remember(id, context) { AppCompatResources.getDrawable(context, id)?.mutate() }
    return drawBehind {
        drawable?.let {
            it.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
            drawIntoCanvas { canvas -> it.draw(canvas.nativeCanvas) }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Liste
// ---------------------------------------------------------------------------------------------

@Composable
private fun HomeList(
    ui: HomeUiState,
    actions: HomeActions,
    suggestionSection: HomeSuggestionSection?,
    listState: LazyListState,
) {
    // Les items présents au moment où le contenu remplace le squelette jouent l'animation d'entrée
    // (`layoutAnimation` de la RecyclerView : une seule fois, par position) ; ceux qui apparaissent ensuite non.
    var revealing by remember { mutableStateOf(true) }
    LaunchedEffect(ui.contentShown) {
        if (ui.contentShown) {
            withFrameNanos { }
            withFrameNanos { }
            revealing = false
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = dimensionResource(R.dimen.entourage_margin_top), bottom = 50.dp),
    ) {
        if (!ui.contentShown) {
            item(key = "skeleton", contentType = "skeleton") { HomeSkeleton() }
            return@LazyColumn
        }

        var order = 0

        // Parcours de bienvenue
        if (ui.journeyVisible) {
            val position = order++
            item(key = "journey", contentType = "journey") {
                Box(Modifier.fallDownIn(position, revealing)) {
                    HomeWelcomeJourney(
                        states = ui.journeyStates,
                        onStepClick = actions.onJourneyStep,
                        onSkipClick = actions.onJourneySkip,
                        onHideRequested = actions.onJourneyHide,
                    )
                }
            }
        }

        if (suggestionSection != null) {
            // Moteur de suggestions : le parcours de bienvenue OU les suggestions (jamais ensemble)
            if (suggestionSection.heroVisibleInCompose) {
                val position = order++
                item(key = "hero", contentType = "hero") { Box(Modifier.fallDownIn(position, revealing)) { suggestionSection.HeroItem() } }
            }
            if (suggestionSection.explorerVisibleInCompose) {
                val position = order++
                item(key = "explorer", contentType = "explorer") { Box(Modifier.fallDownIn(position, revealing)) { suggestionSection.ExplorerItem() } }
            }
        } else {
            // Sensibilisation
            if (ui.initialPedagoVisible) {
                val headerPosition = order++
                item(key = "initialPedagoHeader", contentType = "initialPedagoHeader") {
                    Box(Modifier.fallDownIn(headerPosition, revealing)) {
                        HomeSectionHeader(
                            title = stringResource(R.string.home_title_sensibilisation),
                            subtitle = stringResource(R.string.home_subtitle_sensibilisation),
                        )
                    }
                }
                val listPosition = order++
                item(key = "initialPedago", contentType = "initialPedago") {
                    Box(Modifier.fallDownIn(listPosition, revealing)) {
                        HomeHorizontalList {
                            items(ui.initialPedagos, contentType = { "initialPedago" }) { HomeInitialPedagoCard(it, actions.onPedagoClick) }
                        }
                    }
                }
            }

            // Entraides / contributions
            if (ui.actionsVisible) {
                val headerPosition = order++
                item(key = "actionHeader", contentType = "actionHeader") {
                    Box(Modifier.fallDownIn(headerPosition, revealing)) {
                        HomeSectionHeader(title = ui.actionTitle, subtitle = ui.actionSubtitle)
                    }
                }
                val listPosition = order++
                item(key = "actions", contentType = "actions") {
                    Box(Modifier.fallDownIn(listPosition, revealing)) {
                        HomeHorizontalList { homeActionCards(ui.actions, ui.isContribution) }
                    }
                }
                val buttonPosition = order++
                item(key = "actionButton", contentType = "actionButton") {
                    Box(Modifier.fallDownIn(buttonPosition, revealing)) {
                        HomeSectionButton(text = ui.actionButtonText, onClick = ui.onActionButtonClick)
                    }
                }
            }

            // Événements
            if (ui.eventsVisible) {
                val headerPosition = order++
                item(key = "eventHeader", contentType = "eventHeader") {
                    Box(Modifier.fallDownIn(headerPosition, revealing)) {
                        HomeSectionHeader(
                            title = stringResource(R.string.home_title_event),
                            subtitle = stringResource(R.string.home_subtitle_event),
                        )
                    }
                }
                val listPosition = order++
                item(key = "events", contentType = "events") {
                    Box(Modifier.fallDownIn(listPosition, revealing)) {
                        HomeHorizontalList { homeEventCards(ui.events) }
                    }
                }
                val buttonPosition = order++
                item(key = "eventButton", contentType = "eventButton") {
                    Box(Modifier.fallDownIn(buttonPosition, revealing)) {
                        HomeSectionButton(text = stringResource(R.string.home_btn_more_event), onClick = actions.onEventsAll)
                    }
                }
            }

            // Suggestions du back (hors moteur)
            ui.connectionSuggestion?.let { suggestion ->
                val position = order++
                item(key = "connection", contentType = "connection") {
                    Box(Modifier.fallDownIn(position, revealing)) {
                        HomeSuggestionConnectionCard(suggestion, actions.onConnectionAction, actions.onConnectionDismiss)
                    }
                }
            }
            ui.nextStepSuggestion?.let { suggestion ->
                val position = order++
                item(key = "nextStep", contentType = "nextStep") {
                    Box(Modifier.fallDownIn(position, revealing)) {
                        HomeSuggestionNextStepCard(suggestion, actions.onNextStepAction, actions.onNextStepDismiss)
                    }
                }
            }
            // Les groupes ne sont jamais affichés (section toujours masquée dans l'ancienne home).
        }

        // Outils solidaires
        val toolsPosition = order++
        item(key = "tools", contentType = "tools") {
            Box(Modifier.fallDownIn(toolsPosition, revealing)) {
                HomeTools(
                    onMapClick = actions.onToolsMap,
                    onPedagoClick = actions.onToolsPedago,
                    onCharterClick = actions.onToolsCharter,
                    onDonateClick = actions.onToolsDonate,
                )
            }
        }

        // Contenus à découvrir
        if (ui.pedagoTitleVisible) {
            val position = order++
            item(key = "pedagoHeader", contentType = "pedagoHeader") {
                Box(Modifier.fallDownIn(position, revealing)) {
                    HomeSectionHeader(title = stringResource(R.string.home_title_discover_contents), subtitle = null)
                }
            }
        }
        val firstPedagoPosition = order
        items(ui.pedagos.size, contentType = { "pedago" }) { index ->
            Box(Modifier.fallDownIn(firstPedagoPosition + index, revealing)) {
                HomePedagoCard(ui.pedagos[index], actions.onPedagoClick)
            }
        }
    }
}

private val Decelerate = Easing { t -> 1f - (1f - t) * (1f - t) }

/**
 * `layout_animation_fall_down` : chaque item descend de 20 % de sa hauteur en fondu (500 ms, décélération),
 * décalé de 15 % de la durée (75 ms) par position. Invisible tant que son tour n'est pas venu.
 */
@Composable
private fun Modifier.fallDownIn(position: Int, revealing: Boolean): Modifier {
    val play = remember { revealing }
    if (!play) return this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(position * 75L)
        progress.animateTo(1f, tween(durationMillis = 500, easing = Decelerate))
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = -0.2f * size.height * (1f - progress.value)
    }
}
