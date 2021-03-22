package social.entourage.android.newsfeed.v2

import android.net.Uri
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
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
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.tools.EntBus


/****
 * Announce VH
 */
class AnnounceVH(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: Any?, listener: HomeViewHolderListener) {
        itemView.setOnClickListener {
            data?.let {  listener.onDetailClicked(data) }
        }

        val announce = data as? Announcement
        announce?.let {
            itemView.ui_announce_title?.text = announce.title
            itemView.ui_announce_iv?.let { imageView ->
                Picasso.get().cancelRequest(imageView)
                val imageUrl = announce.imageUrl

                if (imageUrl == null || imageUrl.trim { it <= ' ' }.isEmpty()) {
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_announcement_image_placeholder)?.let { itPlaceholder ->
                        imageView.setImageDrawable(itPlaceholder)
                    }
                } else {
                    Picasso.get().load(Uri.parse(imageUrl)).let {
                        AppCompatResources.getDrawable(itemView.context, R.drawable.ic_announcement_image_placeholder)?.let { itPlaceholder ->
                            it.placeholder(itPlaceholder)
                        }
                        it.into(imageView)
                    }
                }
            }
        }
    }
}

/****
 * Action VH
 */
class ActionVH(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: Any?, listener: HomeViewHolderListener) {
        itemView.setOnClickListener {
            data?.let { listener.onDetailClicked(data)  }
        }

        val feedItem = data as? FeedItem
        feedItem?.let { _feedItem ->
            val res = itemView.resources

            itemView.ui_action_title?.let {titleView ->
                titleView.text = String.format(res.getString(R.string.tour_cell_title), feedItem.getTitle())
            }

            //Icon
            itemView.ui_action_picto_type?.let {iconView ->
                Picasso.get().cancelRequest(iconView)
                feedItem.getIconURL()?.let { iconURL ->
                    iconView.setImageDrawable(null)
                    Picasso.get()
                            .load(iconURL)
                            .placeholder(R.drawable.ic_user_photo_small)
                            .transform(CropCircleTransformation())
                            .into(iconView)
                } ?: run {
                    iconView.setImageDrawable(feedItem.getIconDrawable(itemView.context))
                }
            }

            val _type = (feedItem as BaseEntourage).getGroupType()
            if (_type.equals("outing")) {
                itemView.ui_action_tv_type?.text = itemView.context.resources.getString(R.string.entourage_type_outing)
                itemView.ui_action_tv_more?.text = itemView.context.resources.getString(R.string.show_more_event)
            }
            else {
                itemView.ui_action_tv_more?.text =  itemView.context.resources.getString(R.string.show_more)
                if (feedItem.actionGroupType.equals("ask_for_help")) {
                    itemView.ui_action_tv_type?.text = itemView.context.resources.getString(R.string.entourage_type_demand)
                }
                else {
                    itemView.ui_action_tv_type?.text = itemView.context.resources.getString(R.string.entourage_type_contribution)
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
                itemView.ui_action_iv_user_check?.let {logoView->
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

            if (distStr.isNotEmpty() && !feedItem.postal_code.isNullOrEmpty()) {
                distStr = "%s - %s".format(distStr, feedItem.postal_code)
            } else if (!feedItem.postal_code.isNullOrEmpty()) {
                distStr = feedItem.postal_code!!
            }

            itemView.ui_action_tv_location?.text = distStr

            //Nb people
            if (feedItem.numberOfPeople == 1) {
                itemView.ui_action_tv_nb_users?.text = res.getString(R.string.cell_numberOfPeople, feedItem.numberOfPeople)
            }
            else {
                itemView.ui_action_tv_nb_users?.text = res.getString(R.string.cell_numberOfPeoples, feedItem.numberOfPeople)
            }
        }
    }
}

/****
 * Event VH
 */
class EventVH(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: Any?, listener: HomeViewHolderListener) {
        itemView.setOnClickListener {
            data?.let { listener.onDetailClicked(data)  }
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

            if (distStr.isNotEmpty() && !feedItem.postal_code.isNullOrEmpty()) {
                distStr = "%s - %s".format(distStr, feedItem.postal_code)
            } else if (!feedItem.postal_code.isNullOrEmpty()) {
                distStr = feedItem.postal_code!!
            }
            itemView.ui_event_tv_location?.text = distStr

            //Nb people
            if (feedItem.numberOfPeople == 1) {
                itemView.ui_event_tv_users?.text = res.getString(R.string.cell_numberOfPeople, feedItem.numberOfPeople)
            }
            else {
                itemView.ui_event_tv_users?.text = res.getString(R.string.cell_numberOfPeoples, feedItem.numberOfPeople)
            }
        }
    }
}

class ShowMoreVH(view: View,val type:HomeCardType) : RecyclerView.ViewHolder(view) {
    fun bind(listener: HomeViewHolderListener) {
        itemView.setOnClickListener {
            listener.onShowDetail(type)
        }

        val titleId = if (type == HomeCardType.ACTIONS) R.string.show_more_actions else R.string.show_more_events
        itemView.ui_tv_title_more?.text = itemView.resources.getString(titleId)
    }
}