package social.entourage.android.base.newsfeed

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.layout_feed_action_card_renew.view.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.EntourageEvent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber


open class FeedItemViewHolderRenew(itemView: View) : BaseCardViewHolder(itemView) {

    private lateinit var feedItem: FeedItem

    override fun bindFields() {
        itemView.setOnClickListener { onClickMainView() }
        itemView.action_card_photo?.setOnClickListener { onClickCardPhoto() }
    }

    override fun populate(data: TimestampedObject) {
        this.feedItem = data as FeedItem

        //configure the cell fields
        val res = itemView.resources

        //title
        itemView.action_card_title?.text = String.format(res.getString(R.string.entourage_cell_title), feedItem.getTitle())

        //icon
        if (showCategoryIcon()) {
            // add the icon for entourages
            itemView.action_card_icon?.let { iconView ->
                Glide.with(iconView.context).clear(iconView)
                feedItem.getIconURL()?.let { iconURL ->
                    iconView.setImageDrawable(null)
                    Glide.with(iconView.context)
                            .load(iconURL)
                            .placeholder(R.drawable.ic_user_photo_small)
                            .circleCrop()
                            .listener(requestListener)
                            .into(iconView)
                } ?: run {
                    iconView.setImageDrawable(feedItem.getIconDrawable(itemView.context))
                }
            }
        }

        val author = feedItem.author
        if (author == null) {
            //author
            itemView.action_card_author?.text = "--"
            itemView.action_card_photo?.setImageResource(R.drawable.ic_user_photo_small)
        } else {
            //author photo
            itemView.action_card_photo?.let {
                author.avatarURLAsString?.let { avatarURLAsString ->
                    Glide.with(it.context)
                            .load(Uri.parse(avatarURLAsString))
                            .placeholder(R.drawable.ic_user_photo_small)
                            .circleCrop()
                            .listener(requestListener)
                            .into(it)
                } ?: run {
                    it.setImageResource(R.drawable.ic_user_photo_small)
                }
            }
            // Partner logo
            itemView.action_card_partner_logo?.let { logoView ->
                author.partner?.smallLogoUrl?.let { smallLogoUrl ->
                    Glide.with(logoView.context)
                            .load(Uri.parse(smallLogoUrl))
                            .placeholder(R.drawable.partner_placeholder)
                            .circleCrop()
                            .listener(requestListener)
                            .into(logoView)
                } ?: run {
                    logoView.setImageDrawable(null)
                }
            }
            //author
            itemView.action_card_author?.text = String.format(res.getString(R.string.entourage_cell_author), author.userName)
        }

        //Feed Item type
        if (feedItem is BaseEntourage) {
            if((feedItem as BaseEntourage).isEvent() ){
                if (feedItem is EntourageEvent) {
                    itemView.action_card_type?.text = String.format(res.getString(R.string.entourage_cell_title_event), (feedItem as EntourageEvent).getEventDateFormated(itemView.context))
                }
            }
            else {
                val cat = EntourageCategoryManager.findCategory(feedItem as BaseEntourage)
                itemView.action_card_type?.text = cat.title_list
            }
        }

        if ((feedItem as? BaseEntourage)?.isOnlineEvent == true) {
            itemView.ui_action_tv_location?.text = res.getString(R.string.info_event_online_feed)
        } else {
            val distanceAsString = feedItem.getStartPoint()?.distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
                    ?: ""
            var distStr = if (distanceAsString.equals("", ignoreCase = true)) "" else String.format(res.getString(R.string.entourage_cell_location), distanceAsString)

            feedItem.postal_code?.let { postalCode ->
                if (distStr.isNotEmpty() && postalCode.isNotEmpty()) {
                    distStr = "%s - %s".format(distStr, postalCode)
                } else if (postalCode.isNotEmpty()) {
                    distStr = postalCode
                }
            }
            itemView.ui_action_tv_location?.text = distStr
        }
    }

    protected open fun showCategoryIcon(): Boolean {
        return true
    }

    //--------------------------
    // GLIDE LOADING LISTENER
    //--------------------------
    private val requestListener = object : RequestListener<Drawable> {
        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            Timber.w(e)
            return false
        }
    }

    //--------------------------
    // INNER CLASSES
    //--------------------------
    private fun onClickMainView() {
        viewHolderListener?.onViewHolderDetailsClicked(0)
        // The server wants the position starting with 1
        EntBus.post(OnFeedItemInfoViewRequestedEvent(feedItem, adapterPosition + 1))
    }

    private fun onClickCardPhoto() {
        feedItem.author?.let {
            EntBus.post(OnUserViewRequestedEvent(it.userID))
        }
    }

    private fun onClickCardButton() {
        when (feedItem.joinStatus) {
            FeedItem.JOIN_STATUS_PENDING -> {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_PENDING_OVERLAY)
                EntBus.post(OnFeedItemCloseRequestEvent(feedItem))
            }
            FeedItem.JOIN_STATUS_ACCEPTED -> {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ACTIVE_OVERLAY)
                EntBus.post(OnFeedItemCloseRequestEvent(feedItem))
            }
            FeedItem.JOIN_STATUS_REJECTED -> {
                //TODO: What to do on rejected status ?
            }
            else -> {
                // The server wants the position starting with 1
                EntBus.post(OnFeedItemInfoViewRequestedEvent(feedItem, adapterPosition + 1))
            }
        }
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_feed_action_card_renew
    }
}