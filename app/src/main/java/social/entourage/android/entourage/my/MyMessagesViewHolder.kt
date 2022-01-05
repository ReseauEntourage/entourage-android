package social.entourage.android.entourage.my

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.layout_feed_action_card.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.tape.Events
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.Utils
import timber.log.Timber

/**
 * Created by Jerome on 23/12/2021.
 */
class MyMessagesViewHolder(view: View) : RecyclerView.ViewHolder(view){
    private lateinit var message: BaseEntourage

    fun populate(message: BaseEntourage, messagePosition:Int, listenerClick: (position:Int) -> Unit) {
        this.message = message
        //configure the cell fields
        val res = itemView.resources

        itemView.setOnClickListener {
            onClickMainView()
            listenerClick(messagePosition)
        }

        //title
        itemView.tour_card_title?.let { titleView ->
            titleView.text = String.format(res.getString(R.string.tour_cell_title), this.message.getTitle())
            titleView.setTypeface(null, if (this.message.getUnreadMsgNb() == 0) Typeface.NORMAL else Typeface.BOLD)
        }

        //icon
        itemView.tour_card_icon?.let { iconView ->
            Glide.with(iconView.context).clear(iconView)
            this.message.getIconURL()?.let { iconURL ->
                iconView.setImageDrawable(null)
                Glide.with(iconView.context)
                        .load(iconURL)
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .listener(requestListener)
                        .into(iconView)
            } ?: run {
                iconView.setImageDrawable(this.message.getIconDrawable(itemView.context))
            }
        }

        //last message
        EntourageApplication.get().me()?.let { currentUser ->
            itemView.tour_card_last_message?.text = this.message.lastMessage?.getText(currentUser.id)
                    ?: ""
        } ?: kotlin.run { itemView.tour_card_last_message?.text = "" }

        itemView.tour_card_last_message?.visibility = if (itemView.tour_card_last_message?.text.isNullOrBlank()) View.GONE else View.VISIBLE
        itemView.tour_card_last_message?.setTypeface(null, if (this.message.getUnreadMsgNb() == 0) Typeface.NORMAL else Typeface.BOLD)
        itemView.tour_card_last_message?.setTextColor(if (this.message.getUnreadMsgNb() == 0) ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_normal) else ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_bold))

        //last update date
        itemView.tour_card_last_update_date?.text = Utils.formatLastUpdateDate(this.message.updatedTime, itemView.context)
        itemView.tour_card_last_update_date?.setTypeface(null, if (this.message.getUnreadMsgNb() == 0) Typeface.NORMAL else Typeface.BOLD)
        itemView.tour_card_last_update_date?.setTextColor(if (this.message.getUnreadMsgNb() == 0) ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_normal) else ContextCompat.getColor(itemView.context, R.color.feeditem_card_details_bold))
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

    private fun onClickMainView() {
        // The server wants the position starting with 1
        EntBus.post(Events.OnFeedItemInfoViewRequestedEvent(this.message, bindingAdapterPosition + 1))
    }
}