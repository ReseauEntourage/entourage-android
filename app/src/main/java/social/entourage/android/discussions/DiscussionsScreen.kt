package social.entourage.android.discussions

import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.HomeModerator
import social.entourage.android.ui.theme.NunitoSansBold
import social.entourage.android.ui.theme.NunitoSansRegular
import social.entourage.android.ui.theme.QuicksandBold

/**
 * EN-9489 : refonte de l'onglet Discussions d'après la maquette fournie
 * (maquette-discussions-versions_1.html) — header épuré, card "Bonnes ondes",
 * contact dédié épinglé, liste des conversations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionsScreen(
    conversations: List<Conversation>,
    dedicatedContact: HomeModerator?,
    isFilterActive: Boolean,
    notificationBannerVisible: Boolean,
    isRefreshing: Boolean,
    onEnableNotifications: () -> Unit,
    onSmallTalkCtaClick: () -> Unit,
    onFilterClick: () -> Unit,
    onDedicatedContactClick: () -> Unit,
    onConversationClick: (Conversation) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DiscussionsHeader(isFilterActive = isFilterActive, onFilterClick = onFilterClick)

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.light_beige))
        ) {
            val listState = rememberLazyListState()
            LoadMoreOnScrollEnd(listState = listState, onLoadMore = onLoadMore)

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (notificationBannerVisible) {
                    item(key = "notif_banner") {
                        NotificationBanner(onClick = onEnableNotifications)
                        Spacer(Modifier.height(16.dp))
                    }
                }
                item(key = "small_talk_card") {
                    SmallTalkFeatureCard(onClick = onSmallTalkCtaClick)
                }
                item(key = "section_label") {
                    Text(
                        text = stringResource(R.string.discussion_your_conversations_label),
                        fontFamily = QuicksandBold,
                        fontSize = 18.sp,
                        color = colorResource(R.color.grey),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                if (dedicatedContact != null) {
                    item(key = "dedicated_contact") {
                        DedicatedContactRow(moderator = dedicatedContact, onClick = onDedicatedContactClick)
                    }
                }
                items(conversations, key = { it.id ?: it.hashCode() }) { conversation ->
                    ConversationRow(conversation = conversation, onClick = { onConversationClick(conversation) })
                }
            }
        }
    }
}

@Composable
private fun LoadMoreOnScrollEnd(listState: LazyListState, onLoadMore: () -> Unit) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }
}

@Composable
private fun DiscussionsHeader(isFilterActive: Boolean, onFilterClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(R.drawable.header_profile_orange),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 12.dp, top = 24.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.discussion_main_title),
                fontFamily = QuicksandBold,
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ds_ic_filter),
                        contentDescription = stringResource(R.string.discussion_filter_title),
                        tint = colorResource(R.color.orange),
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .align(Alignment.TopEnd)
                            .background(Color(0xFFFE2929), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEE0C8))
            .clickable { onClick() }
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_notif_alert_discussion_list),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(13.dp))
        Text(
            text = stringResource(R.string.notifications_disabled_message),
            fontFamily = NunitoSansRegular,
            fontSize = 13.5.sp,
            color = Color(0xFF3A3A3A)
        )
    }
}

@Composable
private fun SmallTalkFeatureCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFDEF1E8), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🧩", fontSize = 22.sp)
        }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.discussion_small_talk_card_title),
                fontFamily = NunitoSansBold,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = stringResource(R.string.discussion_small_talk_card_subtitle),
                fontFamily = NunitoSansRegular,
                fontSize = 12.sp,
                color = colorResource(R.color.grey)
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(colorResource(R.color.orange))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = stringResource(R.string.discussion_small_talk_card_cta),
                fontFamily = NunitoSansBold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DedicatedContactRow(moderator: HomeModerator, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            url = moderator.imageURL,
            placeholder = R.drawable.placeholder_user,
            circle = true,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = moderator.displayName.orEmpty(),
                fontFamily = QuicksandBold,
                fontSize = 15.sp,
                color = Color(0xFF1A1A1A)
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFE7D6))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = stringResource(R.string.discussion_dedicated_contact_badge),
                    fontFamily = NunitoSansBold,
                    fontSize = 10.5.sp,
                    color = colorResource(R.color.orange_entourage)
                )
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    val context = LocalContext.current
    val isOuting = conversation.type == "outing"
    val unread = conversation.hasUnread()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isOuting) {
            // Glide affiche placeholder_my_event automatiquement tant que l'image n'est pas
            // chargée (ou en son absence) — pas besoin de gérer le cas nous-mêmes.
            GlideImage(
                url = conversation.imageUrl,
                placeholder = R.drawable.placeholder_my_event,
                circle = false,
                modifier = Modifier.size(56.dp)
            )
        } else {
            GlideImage(
                url = conversation.user?.imageUrl,
                placeholder = R.drawable.placeholder_user,
                circle = true,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            val nameToDisplay = if (conversation.memberCount > 2 && !isOuting) {
                "${conversation.title} et ${conversation.memberCount} membres"
            } else {
                conversation.title.orEmpty()
            }
            Text(
                text = nameToDisplay,
                fontFamily = QuicksandBold,
                fontSize = 15.sp,
                color = Color(0xFF1A1A1A),
                maxLines = 1
            )
            if (isOuting) {
                Text(
                    text = conversation.subname.orEmpty(),
                    fontFamily = NunitoSansRegular,
                    fontSize = 12.5.sp,
                    color = Color(0xFF7A7A7A)
                )
            } else {
                Text(
                    text = conversation.dateFormattedString(context),
                    fontFamily = NunitoSansRegular,
                    fontSize = 12.5.sp,
                    color = Color(0xFF7A7A7A)
                )
            }
            Text(
                text = conversation.getLastMessage(context = context).orEmpty(),
                fontFamily = if (unread) NunitoSansBold else NunitoSansRegular,
                fontSize = 13.sp,
                color = if (unread) Color(0xFF2A2A2A) else colorResource(R.color.dark_grey_opacity_40),
                maxLines = 1
            )
        }

        if (unread) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6A38))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.numberUnreadMessages.toString(),
                    fontFamily = NunitoSansBold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun GlideImage(
    url: String?,
    placeholder: Int,
    circle: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    AndroidView(
        modifier = modifier.clip(if (circle) CircleShape else RoundedCornerShape(14.dp)),
        factory = { ctx: Context ->
            ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        },
        update = { imageView ->
            val request = Glide.with(imageView).load(url).placeholder(placeholder).error(placeholder)
            if (circle) {
                request.transform(CenterCrop(), CircleCrop()).into(imageView)
            } else {
                val radiusPx = with(density) { 14.dp.roundToPx() }
                request.transform(CenterCrop(), RoundedCorners(radiusPx)).into(imageView)
            }
        }
    )
}
