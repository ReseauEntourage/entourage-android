package social.entourage.android.newsfeed.v2

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_cell_entourage_search.view.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import java.util.ArrayList

/**
 * Created by Jr (MJ-DEVS) on 23/09/2021.
 */
class EntouragesSearchAdapter(var items: ArrayList<NewsfeedItem>, val listenerClick: (position:Int) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isAlreadySend = false
    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(items: ArrayList<NewsfeedItem>) {
        this.items = items
        notifyDataSetChanged()
        isAlreadySend = true
    }

    override fun getItemViewType(position: Int): Int {
        if (items.size == 0) return TYPE_EMPTY
        return TYPE_ENTOURAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_EMPTY -> VHEmpty(layoutInflater.inflate(R.layout.layout_search_poi_empty, parent, false))
            else -> EntourageSearchVH(layoutInflater.inflate(R.layout.layout_cell_entourage_search, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_EMPTY) return

        (holder as EntourageSearchVH).bind(items[position].data, listenerClick,position)
    }

    override fun getItemCount(): Int {
        if (items.size == 0 && isAlreadySend) return 1
        return items.size
    }

    inner class VHEmpty(view: View) : RecyclerView.ViewHolder(view)
    companion object {
        const val TYPE_ENTOURAGE = 1
        const val TYPE_EMPTY = 0
    }
}

/************************
 *   Search View holder
 ****/
class EntourageSearchVH(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(data: Any?, listener: (position:Int) -> Unit, position: Int) {
        itemView.setOnClickListener {
            data?.let { listener(position) }
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
                    iconView.setImageDrawable(feedItem.getIconDrawable(itemView.context))
                }
            }

            //Author
            val author = feedItem.author
            if (author == null) {
                //author
                itemView.ui_action_username?.text = ""
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
                itemView.ui_action_username?.text = String.format(res.getString(R.string.by_author_search), author.userName)
            }
            if (!feedItem.showAuthor()) {
                itemView.ui_action_username?.text = ""
                itemView.ui_action_iv_user?.setImageDrawable(null)
                itemView.ui_action_iv_user_check?.setImageDrawable(null)
            }

            itemView.ui_action_iv_user?.setOnClickListener {
                feedItem.author?.let {
                    AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEEDSEARCH_SHOW_PROFILE)
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

            if((feedItem as BaseEntourage).isOnlineEvent) {
                distStr = itemView.context.getString(R.string.detail_action_event_online)
            }

            itemView.ui_action_tv_location?.text = distStr
        }
    }
}