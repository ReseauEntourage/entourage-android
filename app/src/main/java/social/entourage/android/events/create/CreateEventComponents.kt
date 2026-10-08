package social.entourage.android.events.create

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.NunitoSansSemiBold
import social.entourage.android.ui.theme.QuicksandBold

/*
 * Design system du parcours de création / d'édition d'événement, en Compose.
 *
 * Chaque valeur (dp, sp, couleur, rayon, épaisseur de trait) reprend 1:1 l'ancien rendu XML
 * (styles_create_event.xml, bg_create_event_*.xml, item_create_event_*.xml) :
 * - texte : Nunito Sans, Quicksand Bold pour les titres ;
 * - bordures : 1dp gris au repos, 2dp noir pour une sélection / le focus, 2dp rouge en erreur ;
 * - orange réservé au CTA, à la progression, à la bascule et aux petites icônes.
 */

// --- Couleurs (mêmes entrées de colors.xml que les anciens drawables) ------------------------

internal val CeBlack @Composable get() = colorResource(R.color.black)
internal val CeWhite @Composable get() = colorResource(R.color.white)
internal val CeGrey @Composable get() = colorResource(R.color.grey)
internal val CeGreyContour @Composable get() = colorResource(R.color.grey_contour)
internal val CeGreyMiddle @Composable get() = colorResource(R.color.grey_middle)
internal val CeOrange @Composable get() = colorResource(R.color.orange)
internal val CeBeige @Composable get() = colorResource(R.color.beige)
/** Encre (#222) du fond des pastilles sélectionnées. */
internal val CeInk = Color(0xFF222222)
/** Teinte claire des cartes sélectionnées. */
internal val CeSelectedTint = Color(0xFFEFEFEF)
internal val CeError @Composable get() = colorResource(R.color.create_event_error)
internal val CeErrorText @Composable get() = colorResource(R.color.create_event_error_text)

// --- Styles de texte (équivalents des styles create_event_* et des TextView des layouts) -----

/** Les TextView ajoutent le « font padding » : on le conserve pour avoir la même hauteur de ligne. */
@Suppress("DEPRECATION")
private val TextViewPlatformStyle = PlatformTextStyle(includeFontPadding = true)

internal fun ceStyle(family: FontFamily, size: Float, color: Color, lineHeight: Float? = null) = TextStyle(
    fontFamily = family,
    fontSize = size.sp,
    color = color,
    lineHeight = lineHeight?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
    platformStyle = TextViewPlatformStyle,
)

/** Équivalent de `lineSpacingMultiplier=1.1` : 1.1 x la hauteur de ligne naturelle de Nunito Sans (1.364 x la taille). */
internal fun nunitoLineHeight(sizeSp: Float): Float = sizeSp * 1.364f * 1.1f

internal val CeTitleStyle @Composable get() = ceStyle(QuicksandBold, 24f, CeBlack)
internal val CeLabelStyle @Composable get() = ceStyle(NunitoSansBold, 14f, CeBlack)
internal val CeRequiredStyle @Composable get() = ceStyle(NunitoSansRegular, 12f, CeGrey)
internal val CeHintStyle @Composable get() = ceStyle(NunitoSansRegular, 13f, CeGrey)
internal val CeInputStyle @Composable get() = ceStyle(NunitoSansRegular, 16f, CeBlack)
internal val CeErrorTextStyle @Composable get() = ceStyle(NunitoSansRegular, 13f, CeErrorText)

// --- Utilitaires ----------------------------------------------------------------------------

@Composable
internal fun rememberNoIndication() = remember { MutableInteractionSource() }

/** Fond blanc + bordure intérieure (comme une `<shape>` avec `<stroke>`). */
internal fun Modifier.ceOutlined(shape: Shape, width: Dp, color: Color, fill: Color) =
    this.background(fill, shape).border(width, color, shape)

/** Bordure en pointillés (6dp / 4dp) du cadre « Ajouter une photo ». */
private fun Modifier.ceDashedOutline(radius: Dp, width: Dp, color: Color, fill: Color) =
    this
        .background(fill, RoundedCornerShape(radius))
        .drawBehind {
            val stroke = width.toPx()
            val inset = stroke / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = CornerRadius(radius.toPx()),
                style = Stroke(
                    width = stroke,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                )
            )
        }

@Composable
internal fun CeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(textAlign = textAlign),
        maxLines = maxLines,
        overflow = overflow,
    )
}

// --- Structure des pages --------------------------------------------------------------------

/** Contenu défilant d'une étape : marges 22dp, 6dp en haut, 24dp en bas. */
@Composable
internal fun CeStepColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 24.dp),
        content = content
    )
}

/** `create_event_title` : Quicksand Bold 24sp, 24dp d'espace dessous. */
@Composable
internal fun CeTitle(@StringRes text: Int, sizeSp: Float = 24f, modifier: Modifier = Modifier) {
    CeText(
        text = stringResource(text),
        style = ceStyle(QuicksandBold, sizeSp, CeBlack),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    )
}

/** Libellé de champ suivi, ou non, de « obligatoire ». */
@Composable
internal fun CeLabelRow(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        CeText(label, CeLabelStyle)
        if (required) {
            CeText(
                stringResource(R.string.create_event_required),
                CeRequiredStyle,
                Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
internal fun CeHint(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Unspecified) {
    CeText(text, CeHintStyle, modifier.fillMaxWidth(), textAlign)
}

/** Message d'erreur sous un champ (icône + texte rouge), absent quand il n'y a pas d'erreur. */
@Composable
internal fun CeError(@StringRes message: Int?, modifier: Modifier = Modifier, topMargin: Dp = 7.dp, tag: String? = null) {
    if (message == null) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topMargin)
            .let { if (tag != null) it.testTag(tag) else it },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_create_event_error),
            contentDescription = null,
        )
        Spacer(Modifier.width(6.dp))
        CeText(stringResource(message), CeErrorTextStyle)
    }
}

// --- Champs ---------------------------------------------------------------------------------

private val InputShape = RoundedCornerShape(12.dp)

@Composable
private fun ceInputBorder(hasError: Boolean, focused: Boolean): Pair<Dp, Color> = when {
    hasError -> 2.dp to CeError
    focused -> 2.dp to CeBlack
    else -> 1.dp to CeGreyContour
}

/** Champ de saisie (`create_event_input` : 54dp mini, rayon 12dp, marges 15dp / 13dp). */
@Composable
internal fun CeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    hasError: Boolean,
    modifier: Modifier = Modifier,
    minHeight: Dp = 54.dp,
    maxLines: Int = Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    topAligned: Boolean = false,
    maxLength: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
) {
    // État local en TextFieldValue : permet de tronquer la saisie (frappe, collage, clavier) à maxLength.
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    val shown = if (fieldValue.text == value) fieldValue else TextFieldValue(value, TextRange(value.length))
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val (borderWidth, borderColor) = ceInputBorder(hasError, focused)
    // Curseur et poignées : couleur d'accent (orange) comme le thème de l'ancien EditText.
    val accent = CeOrange
    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(accent, accent.copy(alpha = 0.4f))
    ) {
        BasicTextField(
            value = shown,
            onValueChange = { incoming ->
                val text = if (maxLength != null) incoming.text.take(maxLength) else incoming.text
                fieldValue = if (text.length != incoming.text.length) {
                    TextFieldValue(text, TextRange(minOf(incoming.selection.end, text.length)))
                } else incoming
                if (text != value) onValueChange(text)
            },
            modifier = modifier.fillMaxWidth(),
            textStyle = CeInputStyle,
            cursorBrush = SolidColor(accent),
            interactionSource = interaction,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
                keyboardType = keyboardType,
                autoCorrectEnabled = keyboardType == KeyboardType.Text,
            ),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .ceOutlined(InputShape, borderWidth, borderColor, CeWhite)
                        .padding(horizontal = 15.dp, vertical = 13.dp),
                    verticalAlignment = if (topAligned) Alignment.Top else Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            CeText(hint, ceStyle(NunitoSansRegular, 16f, CeGrey))
                        }
                        inner()
                    }
                    if (trailingIcon != null) {
                        Spacer(Modifier.width(12.dp))
                        Image(painter = painterResource(trailingIcon), contentDescription = null)
                    }
                }
            }
        )
    }
}

/** Champ non éditable qui ouvre un sélecteur (date, heure, adresse) : texte ou indication + icône à droite. */
@Composable
internal fun CeSelectField(
    text: String?,
    hint: String,
    @DrawableRes trailingIcon: Int,
    hasError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPadding: Dp = 0.dp,
) {
    val (borderWidth, borderColor) = ceInputBorder(hasError, focused = false)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .ceOutlined(InputShape, borderWidth, borderColor, CeWhite)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Le texte occupe la largeur restante : l'icône est collée au bord droit du champ.
        Box(Modifier.weight(1f)) {
            if (text.isNullOrEmpty()) {
                CeText(hint, ceStyle(NunitoSansRegular, 16f, CeGrey))
            } else {
                CeText(text, CeInputStyle)
            }
        }
        Spacer(Modifier.width(iconPadding))
        Image(painter = painterResource(trailingIcon), contentDescription = null)
    }
}

// --- Cartes, pastilles, cases -----------------------------------------------------------------

/** Pastille ronde « blanc + trait 1dp gris » des boutons − / + et du retour. */
@Composable
internal fun CeRoundButton(
    size: Dp,
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPadding: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .ceOutlined(CircleShape, 1.dp, CeGreyContour, CeWhite)
            .clickable(
                interactionSource = rememberNoIndication(),
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = description }
            .padding(iconPadding),
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(icon), contentDescription = null)
    }
}

/** Tuile beige à coins arrondis (12dp) contenant une icône. */
@Composable
internal fun CeIconTile(size: Dp, padding: Dp, @DrawableRes icon: Int) {
    Box(
        modifier = Modifier
            .size(size)
            .background(CeBeige, RoundedCornerShape(12.dp))
            .padding(padding)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

/** Case ronde de sélection des cartes : cercle noir + coche, sinon cercle blanc à trait gris 2dp. */
@Composable
internal fun CeCheckCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .let {
                if (selected) it.background(CeBlack, CircleShape)
                else it.ceOutlined(CircleShape, 2.dp, CeGreyContour, CeWhite)
            }
    ) {
        if (selected) {
            Image(painterResource(R.drawable.ic_create_event_tick), contentDescription = null)
        }
    }
}

/** Case carrée (rayon 7dp) de sélection des groupes. */
@Composable
internal fun CeCheckSquare(selected: Boolean) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .size(24.dp)
            .let {
                if (selected) it.background(CeBlack, shape)
                else it.ceOutlined(shape, 2.dp, CeGreyContour, CeWhite)
            }
    ) {
        if (selected) {
            Image(painterResource(R.drawable.ic_create_event_tick), contentDescription = null)
        }
    }
}

/** Carte sélectionnable (`item_create_event_option_card`). */
@Composable
internal fun CeOptionCard(
    @DrawableRes icon: Int,
    title: String,
    description: String?,
    selected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp)
            // Sélection : bordure épaisse + fond teinté, nettement distincts de l'état repos.
            .ceOutlined(
                shape,
                if (selected) 2.dp else 1.dp,
                if (selected) CeBlack else CeGreyContour,
                if (selected) CeSelectedTint else CeWhite
            )
            .toggleable(
                value = selected,
                interactionSource = rememberNoIndication(),
                indication = null,
                role = Role.Checkbox,
                onValueChange = onToggle
            )
            .heightIn(min = 72.dp - 11.dp)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CeIconTile(size = 42.dp, padding = 9.dp, icon = icon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 13.dp)
        ) {
            CeText(title, ceStyle(NunitoSansBold, 15f, CeBlack))
            if (description != null) {
                CeText(
                    description,
                    ceStyle(NunitoSansRegular, 12f, CeGrey),
                    Modifier.padding(top = 2.dp)
                )
            }
        }
        CeCheckCircle(selected)
    }
}

/** Pastille de catégorie sélectionnable (étape 4) : sélectionnée = fond encre #222 + texte blanc. */
@Composable
internal fun CeInterestChip(
    label: String,
    selected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .ceOutlined(shape, 1.dp, if (selected) CeInk else CeGreyContour, if (selected) CeInk else CeWhite)
            .toggleable(
                value = selected,
                interactionSource = rememberNoIndication(),
                indication = null,
                role = Role.Checkbox,
                onValueChange = onToggle
            )
            .padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CeText(label, ceStyle(NunitoSansSemiBold, 14f, if (selected) CeWhite else CeBlack))
    }
}

/** Pastille d'aperçu non interactive (`bg_create_event_chip_static`). */
@Composable
internal fun CePreviewChip(label: String) {
    CeText(
        label,
        ceStyle(NunitoSansSemiBold, 12f, CeBlack),
        Modifier
            .ceOutlined(RoundedCornerShape(20.dp), 1.dp, CeGreyContour, CeWhite)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    )
}

/** Bascule présentiel / en ligne : deux moitiés, la sélection est pleine orange (rayon 10dp). */
@Composable
internal fun CeModeToggle(
    online: Boolean,
    onSelect: (online: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .ceOutlined(RoundedCornerShape(14.dp), 1.dp, CeGreyContour, CeWhite)
            .padding(4.dp)
    ) {
        ToggleSegment(
            label = stringResource(R.string.face_to_face),
            selected = !online,
            onClick = { onSelect(false) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 2.dp)
                .testTag("create_event_face_to_face")
        )
        ToggleSegment(
            label = stringResource(R.string.online),
            selected = online,
            onClick = { onSelect(true) },
            modifier = Modifier
                .weight(1f)
                .padding(start = 2.dp)
                .testTag("create_event_online")
        )
    }
}

@Composable
private fun ToggleSegment(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .let { if (selected) it.background(CeOrange, RoundedCornerShape(10.dp)) else it }
            .selectable(
                selected = selected,
                interactionSource = rememberNoIndication(),
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CeText(
            label,
            ceStyle(NunitoSansBold, 15f, if (selected) CeWhite else CeGrey),
            textAlign = TextAlign.Center
        )
    }
}

/** Ligne de récurrence : rond noir, texte 15sp, 48dp de haut au minimum. */
@Composable
internal fun CeRadioRow(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val black = CeBlack
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                interactionSource = rememberNoIndication(),
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Case d'un RadioButton AppCompat : gabarit de 32dp, cercle de 20dp (trait 2dp), point de 10dp.
        Canvas(Modifier.size(32.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(black, radius = 9.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
            if (selected) drawCircle(black, radius = 5.dp.toPx(), center = center)
        }
        Spacer(Modifier.width(10.dp))
        CeText(label, ceStyle(NunitoSansRegular, 15f, CeBlack))
    }
}

// --- Boutons --------------------------------------------------------------------------------

/** CTA orange (`bg_create_event_cta`) : 52dp, 140dp mini, rayon 12dp, ripple blanc translucide. */
@Composable
internal fun CeCta(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(52.dp)
            .defaultMinSize(minWidth = 140.dp)
            .clip(shape)
            .background(CeOrange)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0x33FFFFFF)),
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        CeText(label, ceStyle(NunitoSansBold, 16f, CeWhite), textAlign = TextAlign.Center)
    }
}

/** Barre de progression de 4dp (même barre que le parcours de bienvenue : fond gris, remplissage orange). */
@Composable
internal fun CeProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(CeGreyMiddle, shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(4.dp)
                .background(CeOrange, shape)
        )
    }
}

// --- Images ---------------------------------------------------------------------------------

/**
 * Image Glide (CenterCrop + coins arrondis de [Const.ROUNDED_CORNERS_IMAGES]) : même rendu qu'avant,
 * en réutilisant le pipeline Glide de l'application plutôt qu'un nouveau chargeur d'images.
 */
@Composable
internal fun CeGlideImage(source: Any, contentDescription: String?, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
        update = { view ->
            if (view.tag != source) {
                view.tag = source
                Glide.with(view)
                    .load(source)
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .into(view)
            }
            view.contentDescription = contentDescription
        },
        modifier = modifier
    )
}

/** Cadre « Ajouter une photo » (pointillés 6dp / 4dp, rayon 14dp ; rouge 2dp en erreur). */
@Composable
internal fun CePhotoEmpty(
    title: String,
    hint: String?,
    hasError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .ceDashedOutline(
                radius = 14.dp,
                width = if (hasError) 2.dp else 1.dp,
                color = if (hasError) CeError else CeGreyContour,
                fill = CeWhite
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CeIconTile(size = 46.dp, padding = 11.dp, icon = R.drawable.ic_create_event_image)
        CeText(
            title,
            ceStyle(NunitoSansBold, 14f, CeBlack),
            Modifier.padding(top = 7.dp),
        )
        if (hint != null) {
            CeText(
                hint,
                ceStyle(NunitoSansRegular, 12f, CeGrey),
                Modifier.padding(top = 2.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

internal fun Modifier.ceDim(visible: Boolean): Modifier = if (visible) this else this.alpha(0.4f)

// --- Aperçus Android Studio (composants seuls : les écrans lisent l'application et le ViewModel) ---

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 360, name = "Champs et cartes")
@Composable
private fun CeComponentsPreview() {
    Column(Modifier.padding(22.dp)) {
        CeTitle(R.string.create_event_step_three_title)
        CeModeToggle(online = false, onSelect = {})
        CeLabelRow("Adresse")
        Spacer(Modifier.height(8.dp))
        CeTextField(value = "", onValueChange = {}, hint = "Ex : Discussion entre voisins", hasError = true)
        CeError(R.string.create_event_error_name)
        Spacer(Modifier.height(16.dp))
        CeOptionCard(R.drawable.ic_create_event_family, "En famille", "Les enfants sont les bienvenus.", true, {})
        CeOptionCard(R.drawable.ic_create_event_female, "Réservé aux femmes", null, false, {})
        Spacer(Modifier.height(8.dp))
        CeProgressBar(0.4f)
        Spacer(Modifier.height(16.dp))
        CeCta("Continuer", {})
    }
}
