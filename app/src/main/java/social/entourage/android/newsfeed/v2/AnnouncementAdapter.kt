package social.entourage.android.newsfeed.v2

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.layout_card_announcement.view.*
import social.entourage.android.R
import social.entourage.android.api.model.feed.Announcement
import timber.log.Timber
import java.util.ArrayList


class AnnouncementAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items: ArrayList<Announcement> = ArrayList()
    var viewHolderListener: AnnouncementViewHolderListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_card_announcement, parent, false)

        return AnnouncementViewHolder2(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AnnouncementViewHolder2).viewHolderListener = viewHolderListener
        holder.populate(items[position],position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateDatas(items:ArrayList<Announcement>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    /*******
     * Announcement Viewholder
     */
    inner class AnnouncementViewHolder2(view: View) : RecyclerView.ViewHolder(view), Target {
        private var positionSelected = 0
        var viewHolderListener: AnnouncementViewHolderListener? = null

        fun populate(announcement: Announcement,positionSelected:Int) {
            this.positionSelected = positionSelected
            //cancel previous net requests
            Picasso.get().cancelRequest(this)
            //title
            itemView.announcement_card_title?.text = announcement.title
            val iconUrl = announcement.iconUrl
            if (iconUrl != null) {
                Picasso.get()
                        .load(Uri.parse(iconUrl))
                        .placeholder(R.drawable.ic_broadcast)
                        .into(this)
            } else {
                itemView.announcement_card_title?.setCompoundDrawables(null, null, null, null)
            }
            //body
            itemView.announcement_card_body?.text = announcement.body
            //image
            //cancel previous net requests
            itemView.announcement_card_image?.let {imageView ->
                Picasso.get().cancelRequest(imageView)
                val imageUrl = announcement.imageUrl
                if (imageUrl == null || imageUrl.trim { it <= ' ' }.isEmpty()) {
                    imageView.visibility = View.GONE
                    itemView.announcement_card_divider_left?.visibility = View.VISIBLE
                    itemView.announcement_card_divider_right?.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.VISIBLE
                    itemView.announcement_card_divider_left?.visibility = View.GONE
                    itemView.announcement_card_divider_right?.visibility = View.GONE
                    Picasso.get().load(Uri.parse(imageUrl)).let {
                        AppCompatResources.getDrawable(itemView.context, R.drawable.ic_announcement_image_placeholder)?.let {
                            itPlaceholder -> it.placeholder(itPlaceholder)
                        }
                        it.into(imageView)
                    }
                }
            }
            //act button
            itemView.announcement_card_act_layout?.visibility = if (announcement.action != null) View.VISIBLE else View.GONE
            itemView.announcement_card_button_act?.text = announcement.action

            itemView.setOnClickListener {
                viewHolderListener?.onDetailClicked(positionSelected)
            }
            itemView.announcement_card_button_act?.setOnClickListener {
                viewHolderListener?.onDetailClicked(positionSelected)
            }
        }

        // ----------------------------------
        // Picasso Target implementation
        // ----------------------------------
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            val targetWidth = itemView.resources.getDimensionPixelOffset(R.dimen.announcement_icon_width)
            val targetHeight = itemView.resources.getDimensionPixelOffset(R.dimen.announcement_icon_height)
            val bitmapDrawable = BitmapDrawable(Resources.getSystem(), Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false))
            itemView.announcement_card_title?.setCompoundDrawablesWithIntrinsicBounds(bitmapDrawable, null, null, null)
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            Timber.w(e)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable) {
            itemView.announcement_card_title?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }
}

interface AnnouncementViewHolderListener {
    fun onDetailClicked(position: Int)
}