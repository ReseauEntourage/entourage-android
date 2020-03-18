package social.entourage.android.announcement

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.Announcement
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.CropCircleTransformation
import timber.log.Timber
import kotlinx.android.synthetic.main.layout_card_announcement.view.*
import social.entourage.android.view.EntourageSnackbar

/**
 * View Holder for the announcement card
 * Created by Mihai Ionescu on 02/11/2017.
 */
class AnnouncementViewHolder(view: View?) : BaseCardViewHolder(view), Target {
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
                EntourageSnackbar.make(itemView, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()
            }
        }
        itemView.setOnClickListener(onClickListener)
        itemView.announcement_card_image?.setOnClickListener(onClickListener)
        itemView.announcement_card_button_act?.setOnClickListener(onClickListener)
    }

    override fun populate(data: TimestampedObject) {
        populateAnnouncement(data as Announcement)
    }

    private fun populateAnnouncement(announcement: Announcement?) {
        if (announcement == null) return
        //cancel previous net requests
        Picasso.get().cancelRequest(this)
        Picasso.get().cancelRequest(itemView.announcement_card_photo)
        Picasso.get().cancelRequest(itemView.announcement_card_partner_logo)
        Picasso.get().cancelRequest(itemView.announcement_card_image)
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
        //author
        val author = announcement.author
        if (author == null) {
            itemView.announcement_card_photo?.setImageResource(R.drawable.ic_user_photo_small)
            itemView.announcement_card_partner_logo?.setImageDrawable(null)
        } else { //author photo
            if (itemView.announcement_card_photo != null) {
                val avatarURLAsString = author.avatarURLAsString
                if (avatarURLAsString != null) {
                    Picasso.get()
                            .load(Uri.parse(avatarURLAsString))
                            .placeholder(R.drawable.ic_user_photo_small)
                            .transform(CropCircleTransformation())
                            .into(itemView.announcement_card_photo!!)
                } else {
                    itemView.announcement_card_photo!!.setImageResource(R.drawable.ic_user_photo_small)
                }
            }
            // Partner logo
            if (itemView.announcement_card_partner_logo != null) {
                val partnerLogoURL = author.partner?.smallLogoUrl
                if (partnerLogoURL != null) {
                    Picasso.get()
                            .load(Uri.parse(partnerLogoURL))
                            .placeholder(R.drawable.partner_placeholder)
                            .transform(CropCircleTransformation())
                            .into(itemView.announcement_card_partner_logo!!)
                } else {
                    itemView.announcement_card_partner_logo!!.setImageDrawable(null)
                }
            }
        }
        //body
        itemView.announcement_card_body?.text = announcement.body
        //image
        val imageUrl = announcement.imageUrl
        if (imageUrl == null || imageUrl.trim { it <= ' ' }.length == 0) {
            itemView.announcement_card_image?.visibility = View.GONE
            itemView.announcement_card_divider_left?.visibility = View.VISIBLE
            itemView.announcement_card_divider_right?.visibility = View.VISIBLE
        } else {
            itemView.announcement_card_image?.visibility = View.VISIBLE
            itemView.announcement_card_divider_left?.visibility = View.GONE
            itemView.announcement_card_divider_right?.visibility = View.GONE
            Picasso.get()
                    .load(Uri.parse(imageUrl))
                    .placeholder(R.drawable.ic_announcement_image_placeholder)
                    .into(itemView.announcement_card_image)
        }
        //act button
        itemView.announcement_card_act_layout?.visibility = if (announcement.action != null) View.VISIBLE else View.GONE
        itemView.announcement_card_button_act?.text = announcement.action
        actUrl = announcement.url
    }

    // ----------------------------------
    // Picasso Target implementation
    // ----------------------------------
    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
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

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.layout_card_announcement
    }
}