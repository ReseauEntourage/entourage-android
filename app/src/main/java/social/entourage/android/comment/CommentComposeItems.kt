package social.entourage.android.comment

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Reaction
import social.entourage.android.api.model.ReactionType
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import social.entourage.android.ui.theme.ColorBlack
import social.entourage.android.ui.theme.ColorLightOrange
import kotlin.math.roundToInt
import social.entourage.android.ui.theme.EntourageComposeStyles

private val BubbleCornerShape = RoundedCornerShape(24.dp)

/**
 * Une bulle de message (texte et/ou image), à gauche ou à droite selon [isMe]. Remplace
 * layout_comment_item_left.xml / layout_comment_item_right.xml — les deux étaient une
 * quasi-duplication en ConstraintLayout XML, source des soucis d'alignement/de rendu.
 */
@Composable
fun MessageBubbleItem(
    comment: Post,
    isMe: Boolean,
    isConversation: Boolean,
    usesMessageOptionsMenu: Boolean,
    isHighlighted: Boolean = false,
    allowsReactions: Boolean,
    displayName: String,
    contentHtml: String,
    isDeletedOrOffensive: Boolean,
    deletedOrOffensiveLabel: String,
    dateText: String?,
    onAvatarClick: () -> Unit,
    onLongPress: (Rect) -> Unit,
    onImageClick: () -> Unit,
    onLinkClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onOptionsClick: (Rect) -> Unit,
    onReactionPicked: (ReactionType) -> Unit,
    reactions: List<Reaction>,
    reactionTypes: List<ReactionType>,
) {
    // Flash de mise en évidence pour le message ciblé par un deep link de notification :
    // apparaît immédiatement (pas de fade-in) puis s'estompe. key(comment.id) évite qu'une
    // ComposeView recyclée par le RecyclerView n'hérite de l'animation en cours de l'item
    // précédent qu'elle affichait.
    val highlightColor = colorResource(R.color.light_orange_opacity_50)
    val backgroundColor by key(comment.id) {
        animateColorAsState(
            targetValue = if (isHighlighted) highlightColor else Color.Transparent,
            animationSpec = tween(durationMillis = 600),
            label = "messageHighlight"
        )
    }

    // Long-clic et bouton de déclenchement (sous la bulle) ouvrent tous les deux le même
    // panneau superposé (cf. MessageActionsOverlay) — sur son propre message comme sur celui
    // d'un autre. La position de la bulle est capturée en continu via onGloballyPositioned
    // pour que le panneau puisse s'ancrer exactement dessus au moment du déclenchement.
    var bubbleBoundsInWindow by remember(comment.id) { mutableStateOf(Rect.Zero) }
    val hasReactions = reactions.any { it.reactionsCount > 0 }
    val handleLongPress: () -> Unit = { onLongPress(bubbleBoundsInWindow) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            GlideCircleAvatar(
                url = comment.user?.avatarURLAsString,
                size = 25.dp,
                onClick = onAvatarClick,
                modifier = Modifier.padding(top = 8.dp, end = 8.dp)
            )
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    bubbleBoundsInWindow = coordinates.boundsInWindow()
                }
            ) {
                BubbleContent(
                    comment = comment,
                    isMe = isMe,
                    isDeletedOrOffensive = isDeletedOrOffensive,
                    deletedOrOffensiveLabel = deletedOrOffensiveLabel,
                    contentHtml = contentHtml,
                    onLongPress = handleLongPress,
                    onImageClick = onImageClick,
                    onLinkClick = onLinkClick,
                )
            }

            if (dateText != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isMe && displayName.isNotEmpty()) {
                        Text(
                            text = displayName,
                            style = EntourageComposeStyles.groupMemberSubtitleBlack.copy(
                                color = if (isConversation) ColorLightOrange else ColorBlack
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = dateText,
                        style = EntourageComposeStyles.groupMemberSubtitleBlack.copy(
                            color = if (isConversation) ColorLightOrange else ColorBlack
                        )
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onRetryClick)
                ) {
                    Image(
                        painter = painterResource(R.drawable.new_retry),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(text = stringResource(R.string.retry_comment), style = EntourageComposeStyles.errorMsg)
                }
            }

            // Sous la bulle : pastilles des réactions déjà posées (affichage passif) suivies du
            // bouton déclencheur du panneau d'actions unifié (réactions + copier/modifier/
            // signaler/supprimer, cf. MessageActionsOverlay) — long-clic sur la bulle ouvre le
            // même panneau. Sur son propre message, pas de bouton visible : seul le long-clic
            // ouvre le panneau (les pastilles de réactions des autres restent affichées).
            val showBadges = allowsReactions && comment.id != null && hasReactions
            if (usesMessageOptionsMenu && (!isMe || showBadges)) {
                Spacer(Modifier.padding(top = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showBadges) {
                        ReactionsBadgeRow(reactions = reactions, reactionTypes = reactionTypes)
                        if (!isMe) Spacer(Modifier.padding(start = 6.dp))
                    }
                    if (!isMe) {
                        MessageActionsTriggerButton(
                            showLabel = allowsReactions && !hasReactions,
                            onClick = { onOptionsClick(bubbleBoundsInWindow) }
                        )
                    }
                }
            } else if (!usesMessageOptionsMenu && allowsReactions && comment.id != null) {
                Spacer(Modifier.padding(top = 2.dp))
                ReactionsBadgeRow(reactions = reactions, reactionTypes = reactionTypes)
            }
        }

        if (isMe) {
            GlideCircleAvatar(
                url = comment.user?.avatarURLAsString,
                size = 25.dp,
                onClick = onAvatarClick,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )
        }
    }
}

/** `internal` (pas `private`) : réutilisé tel quel par [MessageActionsOverlay] pour ré-afficher
 * la bulle nette par-dessus le fond flouté, à sa position d'origine. */
@Composable
internal fun BubbleContent(
    comment: Post,
    isMe: Boolean,
    isDeletedOrOffensive: Boolean,
    deletedOrOffensiveLabel: String,
    contentHtml: String,
    onLongPress: () -> Unit,
    onImageClick: () -> Unit,
    onLinkClick: (String) -> Unit,
) {
    val hasImage = !comment.imageUrl.isNullOrEmpty() && !isDeletedOrOffensive
    val hasOnlyImage = hasImage && contentHtml.isBlank()
    val bubbleColor = when {
        isDeletedOrOffensive -> colorResource(R.color.grey_deleted_cell)
        comment.messageType == "auto" -> colorResource(R.color.blue_message_auto)
        isMe -> colorResource(R.color.orange_opacity_50)
        else -> colorResource(R.color.beige)
    }
    // Le long-press est posé directement sur les vraies Views (TextView/ImageView) plutôt
    // que sur un Modifier Compose ambiant : un geste Compose posé sur un ancêtre d'un
    // AndroidView peut intercepter le tap avant qu'il n'atteigne le ClickableSpan du
    // TextView, ce qui casse le clic sur les liens/mentions (le lien retombe alors sur le
    // comportement par défaut d'URLSpan, qui ouvre un navigateur externe).
    val longClick: (() -> Unit)? = if (isDeletedOrOffensive) null else onLongPress

    Column(
        modifier = Modifier
            .let { if (hasOnlyImage) it else it.clip(BubbleCornerShape).background(bubbleColor) }
    ) {
        if (hasImage) {
            GlideMessageImage(url = comment.imageUrl, onClick = onImageClick, onLongClick = longClick)
        }
        if (!hasOnlyImage) {
            if (isDeletedOrOffensive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_comment_deleted),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorResource(R.color.grey_deleted_icon)),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = deletedOrOffensiveLabel,
                        style = EntourageComposeStyles.leftCourantBlack.copy(color = colorResource(R.color.grey_deleted_icon))
                    )
                }
            } else {
                HtmlMessageText(
                    html = contentHtml,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    onLinkClick = onLinkClick,
                    onLongClick = longClick,
                )
            }
        }
    }
}

/**
 * Bouton déclencheur du panneau d'actions unifié (réactions + copier/modifier/signaler/
 * supprimer, cf. [MessageActionsOverlay]) — remplace l'ancien icône "3 points". Pilule avec
 * icône + libellé "Réagir" quand le message n'a encore aucune réaction et qu'on peut y réagir
 * ([showLabel]) ; icône seule sinon (message déjà réagi, où les pastilles de
 * [ReactionsBadgeRow] jouent déjà ce rôle, ou son propre message, où réagir ne s'applique pas).
 */
@Composable
private fun MessageActionsTriggerButton(showLabel: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colorResource(R.color.white))
            .border(1.dp, colorResource(R.color.new_light_grey), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = if (showLabel) 12.dp else 6.dp, vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_reaction_smiley),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorResource(R.color.orange)),
            modifier = Modifier.size(16.dp)
        )
        if (showLabel) {
            Spacer(Modifier.padding(start = 4.dp))
            Text(
                text = stringResource(R.string.message_action_react),
                style = EntourageComposeStyles.groupMemberSubtitleBlack
            )
        }
    }
}

/**
 * Une pastille par type de réaction déjà posé sur le message (icône + nombre), lecture
 * seule — pour réagir, voir [ReactionPickerRow] (appui long sur la bulle).
 *
 * Rendu en Views Android classiques plutôt qu'en Compose pur : la ComposeView réutilisée par
 * le RecyclerView (un item par message) ne recomposait pas de façon fiable cette pastille après
 * plusieurs changements de réaction rapprochés sur le même message — le panneau de sélection
 * (une composition à part, reconstruite à chaque appui long) reflétait toujours la bonne
 * réaction, mais la pastille sous la bulle restait bloquée sur un état intermédiaire jusqu'au
 * rechargement complet du fil. Ni une nouvelle instance de liste, ni notifyDataSetChanged(), ni
 * ComposeView.disposeComposition() avant chaque recomposition n'ont résolu ce blocage : un
 * ImageView/TextView mis à jour de façon impérative dans `update`, lui, se redessine de façon
 * fiable à chaque appel.
 */
@Composable
private fun ReactionsBadgeRow(reactions: List<Reaction>, reactionTypes: List<ReactionType>) {
    val buckets = reactions.filter { it.reactionsCount > 0 }
    if (buckets.isEmpty()) return
    AndroidView(
        factory = { ctx ->
            LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
        },
        update = { container ->
            val ctx = container.context
            val density = ctx.resources.displayMetrics.density
            fun dp(value: Float) = (value * density).roundToInt()

            container.removeAllViews()
            buckets.forEachIndexed { index, bucket ->
                val type = reactionTypes.firstOrNull { it.id == bucket.reactionId }
                val pill = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = dp(15f).toFloat()
                        setColor(ContextCompat.getColor(ctx, R.color.white))
                    }
                    setPadding(dp(6f), dp(3f), dp(6f), dp(3f))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) marginStart = dp(4f)
                    }
                }
                val icon = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(16f), dp(16f))
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                if (type?.imageUrl != null) {
                    Glide.with(icon).load(type.imageUrl).into(icon)
                } else {
                    icon.setImageResource(R.drawable.ic_pouce_orange)
                }
                pill.addView(icon)
                val countText = TextView(ctx).apply {
                    text = bucket.reactionsCount.toString()
                    setTextColor(ContextCompat.getColor(ctx, R.color.black))
                    textSize = 13f
                    typeface = ResourcesCompat.getFont(ctx, R.font.nunitosans_regular)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = dp(4f) }
                }
                pill.addView(countText)
                container.addView(pill)
            }
        }
    )
}

/**
 * Barre de sélection d'une réaction, façon Messenger/WhatsApp, affichée dans le panneau
 * d'actions superposé (cf. [MessageActionsOverlay]), quel que soit l'écran (conversation,
 * commentaires de groupe ou de sortie). [selectedTypeId] (la réaction actuelle de
 * l'utilisateur, 0 si aucune) est mis en évidence — la retaper l'enlève (même toggle que
 * [social.entourage.android.comment.CommentActivity.onMessageReactionClicked]). Fond blanc
 * opaque (pas gris) pour bien se détacher du fond flouté du panneau.
 */
@Composable
fun ReactionPickerRow(types: List<ReactionType>, selectedTypeId: Int, onPicked: (ReactionType) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(R.color.white))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        types.take(5).forEachIndexed { index, type ->
            if (index > 0) Spacer(Modifier.padding(start = 10.dp))
            val isSelected = selectedTypeId != 0 && type.id == selectedTypeId
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) colorResource(R.color.light_orange_opacity_50) else Color.Transparent)
                    .clickable { onPicked(type) }
                    .padding(6.dp)
            ) {
                GlideIcon(url = type.imageUrl, size = 26.dp)
            }
        }
    }
}

/** Séparateur de jour ("Aujourd'hui", "12 janvier 2026"...). Remplace layout_comment_item_date.xml. */
@Composable
fun DateSeparatorItem(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = EntourageComposeStyles.groupMemberSubtitleBlack)
    }
}

/** En-tête "post parent" au-dessus des commentaires. Remplace layout_comment_detail_post_top.xml. */
@Composable
fun ParentPostHeaderItem(
    comment: Post,
    isDeleted: Boolean,
    deletedLabel: String,
    dateText: String?,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlideCircleAvatar(url = comment.user?.avatarURLAsString, size = 36.dp, onClick = {})
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = comment.user?.displayName ?: "-", style = EntourageComposeStyles.leftCourantBlack)
                if (dateText != null) {
                    Text(text = dateText, style = EntourageComposeStyles.groupMemberSubtitle)
                }
            }
        }
        if (!comment.imageUrl.isNullOrEmpty()) {
            GlideMessageImage(url = comment.imageUrl, onClick = {}, modifier = Modifier.padding(top = 8.dp))
        }
        Text(
            text = if (isDeleted) deletedLabel else (comment.content ?: ""),
            style = EntourageComposeStyles.leftCourantBlack,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// ==================================================================================
// Petits composants d'interop Android View (HTML cliquable, images Glide) — on garde
// le rendu HTML/Glide existant (déjà correct) et on ne change que la mise en page
// autour, qui est là où étaient les soucis d'alignement en ConstraintLayout XML.
// ==================================================================================

@Composable
private fun HtmlMessageText(
    html: String,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                linksClickable = true
                setTextColor(android.graphics.Color.BLACK)
                setLinkTextColor(android.graphics.Color.parseColor("#007AFF"))
                textSize = 15f
                typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nunitosans_regular)
            }
        },
        update = { tv ->
            tv.text = buildMessageSpannable(html, onLinkClick)
            // Long-press posé nativement sur la View (pas via un Modifier Compose ambiant)
            // pour ne pas interférer avec le clic sur les ClickableSpan (liens/mentions).
            tv.setOnLongClickListener {
                onLongClick?.invoke()
                onLongClick != null
            }
            tv.isLongClickable = onLongClick != null
        }
    )
}

@Composable
private fun GlideCircleAvatar(url: String?, size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        factory = { ImageView(it) },
        update = { iv ->
            Glide.with(iv)
                .load(url)
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .circleCrop()
                .into(iv)
        }
    )
}

@Composable
private fun GlideIcon(url: String?, size: Dp, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.size(size),
        factory = { ImageView(it) },
        update = { iv -> Glide.with(iv).load(url).into(iv) }
    )
}

@Composable
private fun GlideMessageImage(
    url: String?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val maxWidth = with(density) { (context.resources.displayMetrics.widthPixels / 2).toDp() }
    AndroidView(
        modifier = modifier
            .widthIn(max = maxWidth)
            .clip(RoundedCornerShape(12.dp)),
        factory = { ctx ->
            ImageView(ctx).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                isClickable = true
            }
        },
        update = { iv ->
            Glide.with(iv)
                .load(url)
                .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                .into(iv)
            iv.setOnClickListener { onClick() }
            iv.setOnLongClickListener {
                onLongClick?.invoke()
                onLongClick != null
            }
            iv.isLongClickable = onLongClick != null
        }
    )
}

/**
 * Rend cliquable tout ce qui doit l'être dans le corps d'un message : les balises <a> du
 * HTML (dont les mentions @) et les URL écrites en texte brut.
 *
 * L'ordre compte : [Linkify.addLinks] commence par supprimer tous les URLSpan présents, donc
 * on convertit d'abord les liens du HTML en ClickableSpan (que Linkify ne touche pas), et on
 * ne linkifie le texte brut qu'ensuite. C'est l'équivalent de l'ancien
 * android:autoLink="web" des layouts XML de commentaires, disparu avec le passage en Compose.
 */
private fun buildMessageSpannable(html: String, onLinkClick: (String) -> Unit): Spannable {
    // HtmlCompat gère elle-même le fallback pré-API 24 (minSdk 23 ici).
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    val sb = makeLinksClickable(spanned, onLinkClick)
    Linkify.addLinks(sb, Linkify.WEB_URLS)
    return makeLinksClickable(sb, onLinkClick)
}

/**
 * Convertit les URLSpan d'un Spanned en ClickableSpan appelant [onLinkClick], pour gérer
 * nous-même les clics (deeplink in-app) plutôt que de laisser le système ouvrir un
 * navigateur. Copie de la logique historique de CommentsListAdapter.makeLinksClickable.
 */
private fun makeLinksClickable(spanned: Spanned, onLinkClick: (String) -> Unit): SpannableStringBuilder {
    val sb = SpannableStringBuilder(spanned)
    for (span in sb.getSpans(0, sb.length, URLSpan::class.java)) {
        val start = sb.getSpanStart(span)
        val end = sb.getSpanEnd(span)
        val flags = sb.getSpanFlags(span)
        val url = span.url
        sb.removeSpan(span)
        // Un ClickableSpan déjà posé sur la même portion vient du HTML : c'est la cible
        // explicite (une mention, par ex.), qu'une détection Linkify ne doit pas écraser.
        if (sb.getSpans(start, end, ClickableSpan::class.java).isNotEmpty()) continue
        sb.setSpan(object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                onLinkClick(url)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }, start, end, flags)
    }
    return sb
}
