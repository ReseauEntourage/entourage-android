package social.entourage.android.comment

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.ReactionType
import social.entourage.android.ui.theme.EntourageComposeStyles

/**
 * Tout ce qu'il faut pour ré-afficher la bulle telle qu'elle était et positionner le panneau
 * d'actions par-dessus, capturé au moment du long-clic/tap sur le bouton de réaction — cf.
 * [MessageBubbleItem]. Évite de recalculer le contenu (traduction, statut supprimé...) une
 * deuxième fois : on réutilise tel quel ce que [CommentsListAdapter.bindMessage] a déjà déduit.
 */
data class MessageActionsTarget(
    val comment: Post,
    val isMe: Boolean,
    val contentHtml: String,
    val isDeletedOrOffensive: Boolean,
    val deletedOrOffensiveLabel: String,
    val bounds: Rect,
)

private val PanelWidth = 220.dp

/**
 * Panneau d'actions superposé façon iMessage/Telegram : fond flouté, bulle du message reprise
 * nette à sa position d'origine, ligne de réactions et carte d'options ancrées juste à côté.
 * Remplace l'ancien `ActionSheetFragment` (SheetMode.MESSAGE_ACTIONS) qui ouvrait un bottom
 * sheet séparé — mêmes options, même comportement, présentation en place.
 */
@Composable
fun MessageActionsOverlay(
    target: MessageActionsTarget,
    backgroundBitmap: Bitmap?,
    canEdit: Boolean,
    allowsReactions: Boolean,
    reactionTypes: List<ReactionType>,
    myReactionId: Int,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onReactionPicked: (ReactionType) -> Unit,
) {
    val dismissInteraction = remember { MutableInteractionSource() }
    val swallowInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .let {
                if (backgroundBitmap != null) {
                    it.paint(
                        painter = BitmapPainter(backgroundBitmap.asImageBitmap()),
                        contentScale = ContentScale.FillBounds
                    )
                } else {
                    it.background(Color.Black.copy(alpha = 0.55f))
                }
            }
            .background(Color.Black.copy(alpha = if (backgroundBitmap != null) 0.12f else 0f))
            .clickable(interactionSource = dismissInteraction, indication = null, onClick = onDismiss)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val bubbleLeft = with(density) { target.bounds.left.toDp() }
            val bubbleTop = with(density) { target.bounds.top.toDp() }
            val bubbleWidth = with(density) { target.bounds.width.toDp() }
            val bubbleHeight = with(density) { target.bounds.height.toDp() }
            val bubbleBottom = bubbleTop + bubbleHeight

            // Estimation de la hauteur du panneau (réactions + carte d'options) pour décider de
            // le poser au-dessus ou en dessous de la bulle — mesurer réellement demanderait un
            // second passage de layout ; cette estimation reste correcte tant que le nombre
            // d'options ne change pas radicalement.
            val estimatedPanelHeight = (if (allowsReactions) 56.dp else 0.dp) + 8.dp + optionsCardHeight(
                canEdit = canEdit,
                showReport = !target.isMe,
                isMe = target.isMe
            )
            val spaceBelow = maxHeight - bubbleBottom
            val panelBelow = spaceBelow >= estimatedPanelHeight + 16.dp

            val panelX = when {
                target.isMe -> (bubbleLeft + bubbleWidth - PanelWidth).coerceIn(8.dp, maxWidth - PanelWidth - 8.dp)
                else -> bubbleLeft.coerceIn(8.dp, maxWidth - PanelWidth - 8.dp)
            }
            val panelY = if (panelBelow) bubbleBottom + 8.dp else (bubbleTop - estimatedPanelHeight - 8.dp).coerceAtLeast(8.dp)

            // Panneau (réactions + options), sous la bulle dans l'arbre pour qu'elle passe par-
            // dessus si jamais les deux devaient se chevaucher.
            Column(
                modifier = Modifier
                    .absoluteOffset(x = panelX, y = panelY)
                    .width(PanelWidth)
            ) {
                if (allowsReactions) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = if (target.isMe) Alignment.CenterEnd else Alignment.CenterStart) {
                        ReactionPickerRow(
                            types = reactionTypes,
                            selectedTypeId = myReactionId,
                            onPicked = onReactionPicked
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                MessageActionsCard(
                    isMe = target.isMe,
                    canEdit = canEdit,
                    showReport = !target.isMe,
                    onCopy = onCopy,
                    onEdit = onEdit,
                    onReport = onReport,
                    onDelete = onDelete,
                )
            }

            // La bulle reprise nette, à sa position exacte : un tap dessus ne doit pas fermer
            // le panneau (contrairement à un tap sur le fond flouté autour).
            Box(
                modifier = Modifier
                    .absoluteOffset(x = bubbleLeft, y = bubbleTop)
                    .width(bubbleWidth)
                    .clickable(interactionSource = swallowInteraction, indication = null) {}
            ) {
                BubbleContent(
                    comment = target.comment,
                    isMe = target.isMe,
                    isDeletedOrOffensive = target.isDeletedOrOffensive,
                    deletedOrOffensiveLabel = target.deletedOrOffensiveLabel,
                    contentHtml = target.contentHtml,
                    onLongPress = {},
                    onImageClick = {},
                    onLinkClick = {},
                )
            }
        }
    }
}

/** Hauteur approximative de [MessageActionsCard] pour le nombre de lignes visibles. */
private fun optionsCardHeight(canEdit: Boolean, showReport: Boolean, isMe: Boolean): Dp {
    var rows = 1 // Copier, toujours affiché
    if (canEdit) rows++
    if (showReport) rows++
    if (isMe) rows++ // Supprimer
    return (rows * 48).dp
}

/**
 * Carte d'options blanche arrondie, mêmes lignes/mêmes conditions que l'ancien
 * ActionSheetFragment (SheetMode.MESSAGE_ACTIONS) : Copier (toujours) → Modifier (mon message,
 * si éditable) → Signaler (message des autres) → Supprimer (mon message, destructif).
 */
@Composable
private fun MessageActionsCard(
    isMe: Boolean,
    canEdit: Boolean,
    showReport: Boolean,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.white))
    ) {
        MessageActionRow(
            iconRes = R.drawable.ic_copy_texte,
            label = stringResource(R.string.message_action_copy),
            onClick = onCopy
        )
        if (canEdit) {
            ActionRowDivider()
            MessageActionRow(
                iconRes = R.drawable.ic_edit_event,
                label = stringResource(R.string.message_action_edit),
                onClick = onEdit
            )
        }
        if (showReport) {
            ActionRowDivider()
            MessageActionRow(
                iconRes = R.drawable.ic_report_event_modal,
                label = stringResource(R.string.message_action_report),
                onClick = onReport
            )
        }
        if (isMe) {
            ActionRowDivider()
            MessageActionRow(
                iconRes = R.drawable.new_delete,
                label = stringResource(R.string.message_action_delete),
                onClick = onDelete,
                destructive = true
            )
        }
    }
}

@Composable
private fun ActionRowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colorResource(R.color.new_light_grey))
    )
}

@Composable
private fun MessageActionRow(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val contentColor = if (destructive) colorResource(R.color.orange) else colorResource(R.color.black)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(contentColor),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = EntourageComposeStyles.leftCourantBlack.copy(color = contentColor)
        )
    }
}

/**
 * Flou "maison" : réduit progressivement le bitmap par moitiés successives (moyennage façon
 * mipmap) puis le remet à l'échelle d'origine. Ne dépend d'aucune bibliothèque et donne un
 * rendu identique sur toutes les API supportées (contrairement à Modifier.blur, qui ne
 * fonctionne qu'à partir d'Android 12/API 31).
 */
fun Bitmap.softBlur(steps: Int = 4): Bitmap {
    if (width <= 1 || height <= 1) return this
    val originalWidth = width
    val originalHeight = height
    var current = this
    repeat(steps) {
        val w = (current.width / 2).coerceAtLeast(1)
        val h = (current.height / 2).coerceAtLeast(1)
        if (w == current.width && h == current.height) return@repeat
        val scaled = Bitmap.createScaledBitmap(current, w, h, true)
        if (current !== this) current.recycle()
        current = scaled
    }
    val blurred = Bitmap.createScaledBitmap(current, originalWidth, originalHeight, true)
    if (current !== this) current.recycle()
    return blurred
}
