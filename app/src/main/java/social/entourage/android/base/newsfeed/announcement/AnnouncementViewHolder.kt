package social.entourage.android.base.newsfeed.announcement

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_card_announcement.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.view.EntSnackbar
import timber.log.Timber

/**
 * View Holder for the announcement card
 * Created by Mihai Ionescu on 02/11/2017.
 */
class AnnouncementViewHolder(view: View) : BaseCardViewHolder(view) {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    //Announcement related attributes
    private var actUrl: String? = null

    // ----------------------------------
    // BaseViewHolder implementation
    // ----------------------------------
    override fun bindFields() {
        val onClickListener = View.OnClickListener {
            if (actUrl == null) return@OnClickListener
            val actIntent = Intent(Intent.ACTION_VIEW, Uri.parse(actUrl))
            try {
                itemView.context.startActivity(actIntent)
            } catch (ex: Exception) {
                EntSnackbar.make(itemView, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()
            }
        }
        itemView.setOnClickListener(onClickListener)
        itemView.announcement_card_image?.setOnClickListener(onClickListener)
        itemView.announcement_card_button_act?.setOnClickListener(onClickListener)
    }

    override fun populate(data: TimestampedObject) {
        val announcement: Announcement = data as Announcement
        //cancel previous net requests
        Glide.with(itemView.context).clear(itemView)
        //title
        itemView.announcement_card_title?.text = announcement.title
        announcement.iconUrl?.let { iconUrl ->
            Glide.with(itemView.context)
                    .load(Uri.parse(iconUrl))
                    .placeholder(R.drawable.ic_broadcast)
                    .listener(requestListener)
                    .into(itemView.announcement_card_image)
        } ?: run {
            itemView.announcement_card_title?.setCompoundDrawables(null, null, null, null)
        }
        //body
        itemView.announcement_card_body?.text = announcement.body
        //image
        //cancel previous net requests
        itemView.announcement_card_image?.let {imageView ->
            Glide.with(imageView.context).clear(imageView)
            val imageUrl = announcement.imageUrl
            if (imageUrl == null || imageUrl.trim { it <= ' ' }.isEmpty()) {
                imageView.visibility = View.GONE
                itemView.announcement_card_divider_left?.visibility = View.VISIBLE
                itemView.announcement_card_divider_right?.visibility = View.VISIBLE
            } else {
                imageView.visibility = View.VISIBLE
                itemView.announcement_card_divider_left?.visibility = View.GONE
                itemView.announcement_card_divider_right?.visibility = View.GONE
                Glide.with(imageView.context)
                        .load(Uri.parse(imageUrl))
                        .placeholder(R.drawable.ic_announcement_image_placeholder)
                        .listener(requestListener)
                        .into(imageView)
            }
        }
        //act button
        itemView.announcement_card_act_layout?.visibility = if (announcement.action != null) View.VISIBLE else View.GONE
        itemView.announcement_card_button_act?.text = announcement.action
        actUrl = announcement.url
    }

    //--------------------------
    // GLIDE LOADING LISTENER
    //--------------------------
    private val requestListener = object : RequestListener<Drawable> {
        override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
            Timber.w(e)
            return false
        }
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_card_announcement
    }
}