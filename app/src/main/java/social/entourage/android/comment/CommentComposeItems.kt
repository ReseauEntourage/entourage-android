package social.entourage.android.comment

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.ReactionType
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import social.entourage.android.ui.theme.ColorBlack
import social.entourage.android.ui.theme.ColorLightOrange
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
    allowsReactions: Boolean,
    displayName: String,
    contentHtml: String,
    isDeletedOrOffensive: Boolean,
    deletedOrOffensiveLabel: String,
    dateText: String?,
    showReportIcon: Boolean,
    onAvatarClick: () -> Unit,
    onLongPress: () -> Unit,
    onImageClick: () -> Unit,
    onReportClick: () -> Unit,
    onLinkClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onQuickReact: () -> Unit,
    reactionTypes: List<ReactionType>,
    onReactionPicked: (ReactionType) -> Unit,
    myReactionId: Int,
    reactionsTotalCount: Int,
) {
    // Clé sur comment.id : réinitialise l'état quand cette ComposeView recyclée par le
    // RecyclerView reçoit un item différent (sinon le picker resterait ouvert par erreur).
    var pickerVisible by remember(comment.id) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            BubbleContent(
                comment = comment,
                isMe = isMe,
                isDeletedOrOffensive = isDeletedOrOffensive,
                deletedOrOffensiveLabel = deletedOrOffensiveLabel,
                contentHtml = contentHtml,
                onLongPress = onLongPress,
                onImageClick = onImageClick,
                onLinkClick = onLinkClick,
            )

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

            if (allowsReactions && !isMe && comment.id != null) {
                Spacer(Modifier.padding(top = 2.dp))
                if (pickerVisible) {
                    ReactionPickerRow(
                        types = reactionTypes,
                        onPicked = {
                            pickerVisible = false
                            onReactionPicked(it)
                        },
                    )
                }
                ReactionSummaryBubble(
                    myReactionId = myReactionId,
                    totalCount = reactionsTotalCount,
                    reactionTypes = reactionTypes,
                    onClick = onQuickReact,
                    onLongClick = { pickerVisible = !pickerVisible },
                )
            }
        }

        if (isMe) {
            GlideCircleAvatar(
                url = comment.user?.avatarURLAsString,
                size = 25.dp,
                onClick = onAvatarClick,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )
        } else if (showReportIcon) {
            ReportIcon(onReportClick, modifier = Modifier.padding(top = 4.dp, start = 8.dp))
        }
    }
}

@Composable
private fun BubbleContent(
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

@Composable
private fun ReportIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.new_report_comment),
        contentDescription = null,
        modifier = modifier
            .size(20.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ReactionSummaryBubble(
    myReactionId: Int,
    totalCount: Int,
    reactionTypes: List<ReactionType>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val defaultTypeId = reactionTypes.firstOrNull()?.id
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(colorResource(R.color.white))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        when {
            myReactionId == 0 -> Image(painterResource(R.drawable.ic_pouce_grey), null, modifier = Modifier.size(16.dp))
            myReactionId == defaultTypeId -> Image(painterResource(R.drawable.ic_pouce_orange), null, modifier = Modifier.size(16.dp))
            else -> {
                val chosen = reactionTypes.firstOrNull { it.id == myReactionId }
                if (chosen?.imageUrl != null) {
                    GlideIcon(url = chosen.imageUrl, size = 16.dp)
                } else {
                    Image(painterResource(R.drawable.ic_pouce_orange), null, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (totalCount > 0) {
            Text(
                text = totalCount.toString(),
                style = EntourageComposeStyles.groupMemberSubtitleBlack,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun ReactionPickerRow(types: List<ReactionType>, onPicked: (ReactionType) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.grey_deleted_cell))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        types.take(5).forEachIndexed { index, type ->
            if (index > 0) Spacer(Modifier.padding(start = 6.dp))
            GlideIcon(
                url = type.imageUrl,
                size = 22.dp,
                modifier = Modifier.clickable { onPicked(type) }
            )
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
            // HtmlCompat gère elle-même le fallback pré-API 24 (minSdk 23 ici), pas besoin
            // de brancher sur Build.VERSION.SDK_INT nous-même.
            val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tv.text = makeLinksClickable(spanned, onLinkClick)
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
 * Convertit les URLSpan d'un Spanned HTML en ClickableSpan appelant [onLinkClick], pour
 * gérer nous-même les clics (deeplink in-app) plutôt que de laisser le système ouvrir un
 * navigateur. Copie de la logique historique de CommentsListAdapter.makeLinksClickable.
 */
private fun makeLinksClickable(spanned: Spanned, onLinkClick: (String) -> Unit): Spannable {
    val urlSpans = spanned.getSpans(0, spanned.length, URLSpan::class.java)
    if (urlSpans.isEmpty()) return spanned as? Spannable ?: SpannableStringBuilder(spanned)
    val sb = SpannableStringBuilder(spanned)
    for (span in urlSpans) {
        val start = sb.getSpanStart(span)
        val end = sb.getSpanEnd(span)
        val flags = sb.getSpanFlags(span)
        val url = span.url
        sb.removeSpan(span)
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
