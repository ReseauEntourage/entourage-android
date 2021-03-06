package social.entourage.android.newsfeed.v2

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.layout_cell_action_more.view.*
import kotlinx.android.synthetic.main.layout_cell_event.view.*
import kotlinx.android.synthetic.main.layout_cell_headline_action.view.*
import kotlinx.android.synthetic.main.layout_cell_headline_announce.view.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.EntourageEvent
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.tools.EntBus


/****
 * Announce VH
 */
class AnnounceVH(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: Any?, listener: HomeViewHolderListener, position: Int) {
        itemView.setOnClickListener {
            data?.let { listener.onDetailClicked(data, position, true) }
        }

        itemView.ui_bg_trans_black?.visibility = View.INVISIBLE


        val announce = data as? Announcement
        announce?.let {
            itemView.ui_announce_title?.text = announce.title
            itemView.ui_announce_iv?.let { imageView ->
                Glide.with(imageView.context).clear(imageView)
                val imageUrl = announce.imageUrl

                if (imageUrl == null || imageUrl.trim { it <= ' ' }.isEmpty()) {
                    setCellPlaceholder(imageView)
                } else {
                    val requestListener = object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            itemView.ui_announce_title?.visibility = View.INVISIBLE
                            itemView.ui_view_show_more?.visibility = View.INVISIBLE
                            return false
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            setCellPlaceholder(imageView)
                            return false
                        }
                    }
                    Glide.with(imageView.context)
                            .load(Uri.parse(imageUrl))
                            .listener(requestListener)
                            .into(imageView)
                }
            }
        }
    }

    fun setCellPlaceholder(imageView: ImageView) {
        itemView.ui_announce_title?.visibility = View.VISIBLE
        itemView.ui_view_show_more?.visibility = View.VISIBLE
        AppCompatResources.getDrawable(itemView.context, R.drawable.bg_button_rounded_pre_onboard_orange_plain)?.let { itPlaceholder ->
            imageView.setImageDrawable(itPlaceholder)
        }
    }
}

/****
 * Action VH
 */
class ActionVH(view: View) : RecyclerView.ViewHolder(view) {
    private var isAction = true //Use for tracking Firebase click

    fun bind(data: Any?, listener: HomeViewHolderListener, position: Int, isFromHeadline: Boolean) {
        itemView.setOnClickListener {
            data?.let { listener.onDetailClicked(data, position, isFromHeadline, isAction) }
        }

        val feedItem = data as? FeedItem
        feedItem?.let { _feedItem ->
            val res = itemView.resources

            itemView.ui_action_title?.let { titleView ->
                titleView.text = String.format(res.getString(R.string.tour_cell_title), feedItem.getTitle())
            }

            //Icon
            itemView.ui_action_picto_type?.let { iconView ->
                Glide.with(iconView.context).clear(iconView)
                feedItem.getIconURL()?.let { iconURL ->
                    iconView.setImageDrawable(null)
                    Glide.with(iconView.context)
                            .load(iconURL)
                            .placeholder(R.drawable.ic_user_photo_small)
                            .circleCrop()
                            .into(iconView)
                } ?: run {
                    Glide.with(iconView.context)
                            .load(feedItem.getIconDrawable(itemView.context))
                            .into(iconView)
                }
            }

            val _type = (feedItem as BaseEntourage).getGroupType()
            if (_type == "outing") {
                isAction = false
                itemView.ui_action_tv_type?.text = itemView.context.resources.getString(R.string.entourage_type_outing)
                itemView.ui_action_tv_more?.text = itemView.context.resources.getString(R.string.show_more_event)
                itemView.ui_tv_info_by?.text = res.getString(R.string.cell_invit_from)
            } else {
                isAction = true
                itemView.ui_action_tv_more?.text = itemView.context.resources.getString(R.string.show_more)
                if (feedItem.actionGroupType == "ask_for_help") {
                    itemView.ui_action_tv_type?.text = itemView.context.resources.getString(R.string.entourage_type_demand)
                    itemView.ui_tv_info_by?.text = res.getString(R.string.cell_demand_from)
                } else {
                    itemView.ui_action_tv_type?.text = itemView.context.resources.getString(R.string.entourage_type_contribution)
                    itemView.ui_tv_info_by?.text = res.getString(R.string.cell_contrib_from)
                }
            }

            if (feedItem.getFeedTypeColor() != 0) {
                itemView.ui_action_tv_type?.setTextColor(ContextCompat.getColor(itemView.context, feedItem.getFeedTypeColor()))
            }

            //Author
            val author = feedItem.author
            if (author == null) {
                //author
                itemView.ui_action_tv_username?.text = "--"
                itemView.ui_action_iv_user?.setImageResource(R.drawable.ic_user_photo_small)
                itemView.ui_action_iv_user_check?.setImageDrawable(null)
            } else {
                //author photo
                itemView.ui_action_iv_user?.let {
                    author.avatarURLAsString?.let { avatarURLAsString ->
                        Glide.with(it.context)
                                .load(Uri.parse(avatarURLAsString))
                                .placeholder(R.drawable.ic_user_photo_small)
                                .circleCrop()
                                .into(it)
                    } ?: run {
                        Glide.with(it.context)
                                .load(R.drawable.ic_user_photo_small)
                                .into(it)
                    }
                }
                // Partner logo
                itemView.ui_action_iv_user_check?.let { logoView ->
                    author.partner?.smallLogoUrl?.let { smallLogoUrl ->
                        Glide.with(logoView.context)
                                .load(Uri.parse(smallLogoUrl))
                                .placeholder(R.drawable.partner_placeholder)
                                .circleCrop()
                                .into(logoView)
                    } ?: run {
                        logoView.setImageDrawable(null)
                    }
                }

                //author
                itemView.ui_action_tv_username?.text = String.format(res.getString(R.string.tour_cell_author), author.userName)
            }
            if (!feedItem.showAuthor()) {
                itemView.ui_action_tv_username?.text = ""
                itemView.ui_action_iv_user?.setImageDrawable(null)
                itemView.ui_action_iv_user_check?.setImageDrawable(null)
            }

            itemView.ui_action_iv_user?.setOnClickListener {
                feedItem.author?.let {
                    EntBus.post(Events.OnUserViewRequestedEvent(it.userID))
                }
            }

            //Location
            val distanceAsString = feedItem.getStartPoint()?.distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
                    ?: ""
            var distStr = if (distanceAsString.equals("", ignoreCase = true)) "" else String.format(res.getString(R.string.tour_cell_location), distanceAsString)

            feedItem.postal_code?.let { postalCode ->
                if (distStr.isNotEmpty() && postalCode.isNotEmpty()) {
                    distStr = "%s - %s".format(distStr, postalCode)
                } else if (postalCode.isNotEmpty()) {
                    distStr = postalCode
                }
            }

            itemView.ui_action_tv_location?.text = distStr

            //Nb people
            if (feedItem.numberOfPeople == 1) {
                itemView.ui_action_tv_nb_users?.text = res.getString(R.string.cell_numberOfPeople, feedItem.numberOfPeople)
            } else {
                itemView.ui_action_tv_nb_users?.text = res.getString(R.string.cell_numberOfPeoples, feedItem.numberOfPeople)
            }
        }
    }
}

/****
 * Event VH
 */
class EventVH(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: Any?, listener: HomeViewHolderListener, position: Int, isFromHeadline: Boolean) {
        itemView.setOnClickListener {
            data?.let { listener.onDetailClicked(data, position, isFromHeadline) }
        }

        val feedItem = data as? FeedItem
        feedItem?.let { _feedItem ->
            val res = itemView.resources

            itemView.ui_event_title?.let { titleView ->
                titleView.text = String.format(res.getString(R.string.tour_cell_title), feedItem.getTitle())
            }

            itemView.ui_event_tv_date?.let { titleView ->
                if (feedItem is EntourageEvent) {
                    titleView.text = String.format(res.getString(R.string.tour_cell_title), feedItem.getEventDateFormated(itemView.context))
                }
            }

            //Location
            val distanceAsString = feedItem.getStartPoint()?.distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
                    ?: ""
            var distStr = if (distanceAsString.equals("", ignoreCase = true)) "" else String.format(res.getString(R.string.tour_cell_location), distanceAsString)

            feedItem.postal_code?.let { postalCode ->
                if (distStr.isNotEmpty() && postalCode.isNotEmpty()) {
                    distStr = "%s - %s".format(distStr, postalCode)
                } else if (postalCode.isNotEmpty()) {
                    distStr = postalCode
                }
            }
            itemView.ui_event_tv_location?.text = distStr

            //Nb people
            if (feedItem.numberOfPeople == 1) {
                itemView.ui_event_tv_users?.text = res.getString(R.string.cell_numberOfPeople, feedItem.numberOfPeople)
            } else {
                itemView.ui_event_tv_users?.text = res.getString(R.string.cell_numberOfPeoples, feedItem.numberOfPeople)
            }

            itemView.ui_event_iv?.let { eventView ->
                if (feedItem is EntourageEvent) {
                    feedItem.metadata?.portrait_url?.let {
                        Glide.with(eventView.context)
                                .load(Uri.parse(it))
                                .placeholder(R.drawable.partner_placeholder)
                                .into(eventView)
                    } ?: run {
                        Glide.with(eventView.context)
                                .load(R.drawable.ic_placeholder_event_feed)
                                .into(eventView)
                    }
                }
            }
        }
    }
}

class ShowMoreVH(view: View, val type: HomeCardType) : RecyclerView.ViewHolder(view) {
    fun bind(listener: HomeViewHolderListener) {
        itemView.setOnClickListener {
            listener.onShowDetail(type, false)
        }

        val titleId = if (type == HomeCardType.ACTIONS) R.string.show_more_actions else R.string.show_more_events
        itemView.ui_tv_title_more?.text = itemView.resources.getString(titleId)
    }
}

class OtherVH(view: View, val type: HomeCardType) : RecyclerView.ViewHolder(view) {
    fun bind(listener: HomeViewHolderListener) {
        itemView.setOnClickListener {
            if (type == HomeCardType.ACTIONS) {
                listener.onShowEntourageHelp()
            } else {
                listener.onShowChangeZone()
            }
        }
    }
}