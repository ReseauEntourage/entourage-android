package social.entourage.android.newsfeed

import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.layout_feed_action_card.view.*
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.BusProvider.instance
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.tools.Utils.formatLastUpdateDate

/**
 * Created by Mihai Ionescu on 24/03/2017.
 */
open class FeedItemViewHolder(itemView: View) : BaseCardViewHolder(itemView), Target {

    private lateinit var feedItem: FeedItem

    override fun bindFields() {
        itemView.setOnClickListener {onClickMainView()}
        itemView.tour_card_photo?.setOnClickListener {onClickCardPhoto()}
        itemView.tour_card_button_act?.setOnClickListener {onClickCardButton()}
    }

    override fun populate(data: TimestampedObject) {
        this.feedItem = data as FeedItem

        //configure the cell fields
        val res = itemView.resources

        //title
        itemView.tour_card_title?.let {titleView ->
            titleView.text = String.format(res.getString(R.string.tour_cell_title), feedItem.getTitle())
            titleView.setTypeface(null, if (feedItem.getUnreadMsgNb() == 0) Typeface.NORMAL else Typeface.BOLD)
        }
        //icon
        if (showCategoryIcon() ) {
            // add the icon for entourages
            itemView.tour_card_icon?.let {iconView ->
                Picasso.get().cancelRequest(iconView)
                val iconURL = feedItem.getIconURL()
                if (iconURL != null) {
                    iconView.setImageDrawable(null)
                    Picasso.get()
                            .load(iconURL)
                            .placeholder(R.drawable.ic_user_photo_small)
                            .transform(CropCircleTransformation())
                            .into(iconView)
                } else {
                    iconView.setImageDrawable(feedItem.getIconDrawable(itemView.context))
                }
            }
        }
        val author = feedItem.author
        if (author == null) {
            //author
            itemView.tour_card_author?.text = "--"
            itemView.tour_card_photo?.setImageResource(R.drawable.ic_user_photo_small)
        } else {
            //author photo
            itemView.tour_card_photo?.let {
                author.avatarURLAsString?.let {avatarURLAsString ->
                    Picasso.get()
                            .load(Uri.parse(avatarURLAsString))
                            .placeholder(R.drawable.ic_user_photo_small)
                            .transform(CropCircleTransformation())
                            .into(it)
                } ?: run {
                    it.setImageResource(R.drawable.ic_user_photo_small)
                }
            }
            // Partner logo
            itemView.tour_card_partner_logo?.let {logoView->
                author.partner?.smallLogoUrl?.let {smallLogoUrl->
                    Picasso.get()
                            .load(Uri.parse(smallLogoUrl))
                            .placeholder(R.drawable.partner_placeholder)
                            .transform(CropCircleTransformation())
                            .into(logoView)
                } ?: run {
                    logoView.setImageDrawable(null)
                }
            }

            //author
            itemView.tour_card_author?.text = String.format(res.getString(R.string.tour_cell_author), author.userName)
        }
        if (!feedItem.showAuthor()) {
            itemView.tour_card_author?.text = ""
            itemView.tour_card_photo?.setImageDrawable(null)
            itemView.tour_card_partner_logo?.setImageDrawable(null)
        }

        //Metadata
        //hide author for events
        if ((feedItem is BaseEntourage) && ((feedItem as BaseEntourage).metadata?.startDate != null))
            itemView.tour_card_author?.text = ""

        //Feed Item type
        itemView.tour_card_type?.text = feedItem.getFeedTypeLong(itemView.context)
        if (feedItem.getFeedTypeColor() != 0) {
            itemView.tour_card_type?.setTextColor(ContextCompat.getColor(itemView.context, feedItem.getFeedTypeColor()))
        }

        if (feedItem.isOnlineEvent) {
            itemView.tour_card_location?.text = res.getString(R.string.info_event_online_feed)
        }
        else {
            val distanceAsString = feedItem.getStartPoint()?.distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
                    ?: ""
            var distStr = if (distanceAsString.equals("", ignoreCase = true)) "" else String.format(res.getString(R.string.tour_cell_location), distanceAsString)

            if (distStr.length > 0 && !feedItem.postal_code.isNullOrEmpty()) {
                distStr = "%s - %s".format(distStr, feedItem.postal_code)
            } else if (!feedItem.postal_code.isNullOrEmpty()) {
                distStr = feedItem.postal_code!!
            }

            itemView.tour_card_location?.text = distStr
        }

        //tour members
        itemView.tour_card_people_count?.text = res.getString(R.string.tour_cell_numberOfPeople, feedItem.numberOfPeople)

        //badge count
        if (feedItem.getUnreadMsgNb() <= 0) {
            itemView.tour_card_badge_count?.visibility = View.GONE
        } else {
            itemView.tour_card_badge_count?.visibility = View.VISIBLE
            itemView.tour_card_badge_count?.text = res.getString(R.string.badge_count_format, feedItem.getUnreadMsgNb())
        }

        //act button
        if (itemView.tour_card_button_act != null) {
            //var dividerColor = R.color.accent
            var textColor = R.color.accent
            itemView.tour_card_button_act?.visibility = View.VISIBLE
            if (feedItem.isFreezed()) {
                itemView.tour_card_button_act?.setText(feedItem.getFreezedCTAText())
                //dividerColor = R.color.greyish
                textColor = feedItem.getFreezedCTAColor()
            } else {
                when(feedItem.joinStatus) {
                    FeedItem.JOIN_STATUS_PENDING -> {
                        itemView.tour_card_button_act?.setText(R.string.tour_cell_button_pending)
                    }
                    FeedItem.JOIN_STATUS_ACCEPTED -> {
                        if (feedItem.type == TimestampedObject.TOUR_CARD && feedItem.isOngoing()) {
                            itemView.tour_card_button_act?.setText(R.string.tour_cell_button_ongoing)
                        }
                        else {
                            EntourageApplication.get().me()?.let { currentUser ->
                                if (author?.userID == currentUser.id) {
                                    itemView.tour_card_button_act?.setText(R.string.tour_cell_button_accepted)
                                }
                                else {
                                    itemView.tour_card_button_act?.setText(R.string.tour_cell_button_accepted_other)
                                }
                            } ?: run {
                                itemView.tour_card_button_act?.setText(R.string.tour_cell_button_accepted)
                            }
                        }
                    }
                    FeedItem.JOIN_STATUS_REJECTED -> {
                        itemView.tour_card_button_act?.setText(R.string.tour_cell_button_rejected)
                        textColor = R.color.tomato
                    }
                    else -> {
                        itemView.tour_card_button_act?.setText(R.string.tour_cell_button_view)
                        //dividerColor = R.color.greyish
                        itemView.tour_card_button_act?.visibility = View.INVISIBLE
                    }
                }
            }
            itemView.tour_card_button_act?.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        //last message
        EntourageApplication.get().me()?.let { currentUser ->
            itemView.tour_card_last_message?.text = feedItem.lastMessage?.getText(currentUser.id) ?: ""
        } ?: kotlin.run { itemView.tour_card_last_message?.text = "" }

        itemView.tour_card_last_message?.visibility = if (itemView.tour_card_last_message?.text.isNullOrBlank()) View.GONE else View.VISIBLE
        itemView.tour_card_last_message?.setTypeface(null, if (feedItem.getUnreadMsgNb() == 0) Typeface.NORMAL else Typeface.BOLD)
        itemView.tour_card_last_message?.setTextColor(if (feedItem.getUnreadMsgNb() == 0) ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_normal) else ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_bold))

        //last update date
        itemView.tour_card_last_update_date?.text = formatLastUpdateDate(feedItem.updatedTime, itemView.context)
        itemView.tour_card_last_update_date?.setTypeface(null, if (feedItem.getUnreadMsgNb() == 0) Typeface.NORMAL else Typeface.BOLD)
        itemView.tour_card_last_update_date?.setTextColor(if (feedItem.getUnreadMsgNb() == 0) ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_normal) else ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_bold))
    }

    protected open fun showCategoryIcon(): Boolean {
        return true
    }

    //--------------------------
    // PICASSO TARGET IMPLEMENTATION
    //--------------------------
    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
        val targetWidth = itemView.resources.getDimensionPixelOffset(R.dimen.feeditem_icon_width)
        val targetHeight = itemView.resources.getDimensionPixelOffset(R.dimen.feeditem_icon_height)
        val drawable = BitmapDrawable(itemView.resources, Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false))
        itemView.tour_card_title?.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
        itemView.tour_card_title?.setCompoundDrawablesWithIntrinsicBounds(errorDrawable, null, null, null)
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable) {}

    //--------------------------
    // INNER CLASSES
    //--------------------------
    private fun onClickMainView(){
        viewHolderListener?.onViewHolderDetailsClicked(0)
        // The server wants the position starting with 1
        instance.post(OnFeedItemInfoViewRequestedEvent(feedItem, adapterPosition + 1))
    }

    private fun onClickCardPhoto(){
        feedItem.author?.let {
            instance.post(OnUserViewRequestedEvent(it.userID))
        }

    }
    private fun onClickCardButton(){
        when (feedItem.joinStatus) {
            FeedItem.JOIN_STATUS_PENDING -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_PENDING_OVERLAY)
                instance.post(OnFeedItemCloseRequestEvent(feedItem))
            }
            FeedItem.JOIN_STATUS_ACCEPTED -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ACTIVE_OVERLAY)
                instance.post(OnFeedItemCloseRequestEvent(feedItem))
            }
            FeedItem.JOIN_STATUS_REJECTED -> {
                //TODO: What to do on rejected status ?
            }
            else -> {
                // The server wants the position starting with 1
                instance.post(OnFeedItemInfoViewRequestedEvent(feedItem, adapterPosition + 1))
            }
        }
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_feed_action_card
    }
}