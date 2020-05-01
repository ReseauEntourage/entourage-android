package social.entourage.android.entourage.information.discussion

import android.net.Uri
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.entourage_information_user_join_card_view.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.map.EntourageUser
import social.entourage.android.api.tape.Events.OnUserJoinRequestUpdateEvent
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.tools.Utils

/**
 * User Card View in tour information screen
 */
class TourUserJoinCardViewHolder(view: View) : BaseCardViewHolder(view) {
    private var userId = 0
    private var feedItem: FeedItem? = null

    override fun bindFields() {
        itemView.tic_photo?.setOnClickListener {onClick()}
        itemView.tic_partner_logo?.setOnClickListener {onClick()}
        itemView.tic_public_info_photo?.setOnClickListener  {onClick()}
        itemView.tic_view_profile_button?.setOnClickListener {onClick()}
        itemView.tic_accept_button?.setOnClickListener(View.OnClickListener {
            if (userId == 0 || feedItem == null) return@OnClickListener
            EntourageEvents.logEvent(EntourageEvents.EVENT_JOIN_REQUEST_ACCEPT)
            BusProvider.instance.post(
                    OnUserJoinRequestUpdateEvent(
                            userId,
                            FeedItem.JOIN_STATUS_ACCEPTED,
                            feedItem)
            )
        })
        itemView.tic_refuse_button?.setOnClickListener(View.OnClickListener {
            if (userId == 0 || feedItem == null) return@OnClickListener
            EntourageEvents.logEvent(EntourageEvents.EVENT_JOIN_REQUEST_REJECT)
            BusProvider.instance.post(
                    OnUserJoinRequestUpdateEvent(
                            userId,
                            FeedItem.JOIN_STATUS_REJECTED,
                            feedItem)
            )
        })
    }

    private fun onClick() {
        if (userId == 0) return
        BusProvider.instance.post(OnUserViewRequestedEvent(userId))
    }

    override fun populate(user: TimestampedObject) {
        if(user !is EntourageUser) return
        if (user.displayName == null || user.status == null) return
        userId = user.userId
        feedItem = user.feedItem
        if (user.status == FeedItem.JOIN_STATUS_PENDING) {
            populatePendingStatus(user)
        } else {
            populateJoinedStatus(user)
        }
    }

    private fun populatePendingStatus(user: EntourageUser) {
        itemView.tic_private_info_section?.visibility = View.VISIBLE
        itemView.tic_public_info_section?.visibility = View.GONE
        itemView.tic_private_username?.setText(user.displayName)
        //TODO Set the user role once they are sent from the server
        itemView.tic_private_username?.setRoles(null)
        val avatarURL = user.avatarURLAsString
        if (avatarURL != null  && itemView.tic_photo != null) {
            Picasso.get().load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(CropCircleTransformation())
                    .into(itemView.tic_photo!!)
        } else {
            itemView.tic_photo?.setImageResource(R.drawable.ic_user_photo_small)
        }

        // Partner logo
        val partner = user.partner
        if ( partner?.smallLogoUrl != null && itemView.tic_partner_logo != null) {
            Picasso.get()
                    .load(Uri.parse( partner.smallLogoUrl))
                    .placeholder(R.drawable.partner_placeholder)
                    .transform(CropCircleTransformation())
                    .into(itemView.tic_partner_logo!!)
        } else {
            itemView.tic_partner_logo?.setImageDrawable(null)
        }
        itemView.tic_join_description?.text = getJoinStatus(user.status, user.feedItem.type == TimestampedObject.TOUR_CARD)
        itemView.tic_join_message?.text = user.message

        // If we are not the creators of the entourage, hide the Accept and Refuse buttons
        val me = EntourageApplication.me(itemView.context)
        val isMyEntourage = if (me != null && feedItem != null && feedItem!!.author != null) {
            (me.id == feedItem!!.author!!.userID)
        } else false
        itemView.tic_accept_button?.visibility = if (isMyEntourage) View.VISIBLE else View.GONE
        itemView.tic_refuse_button?.visibility = if (isMyEntourage) View.VISIBLE else View.GONE
    }

    private fun populateJoinedStatus(user: EntourageUser) {
        itemView.tic_private_info_section?.visibility = View.GONE
        itemView.tic_public_info_section?.visibility = View.VISIBLE
        itemView.tic_public_info_username?.text = user.displayName
        val avatarURL = user.avatarURLAsString
        if (avatarURL != null && itemView.tic_public_info_photo !=null) {
            Picasso.get().load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(CropCircleTransformation())
                    .into(itemView.tic_public_info_photo!!)
        } else {
            itemView.tic_public_info_photo?.setImageResource(R.drawable.ic_user_photo_small)
        }

        // Partner logo
        val partner = user.partner
        if (partner != null && itemView.tic_public_info_partner_logo !=null) {
            val partnerLogoURL = partner.smallLogoUrl
            if (partnerLogoURL != null) {
                Picasso.get()
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(CropCircleTransformation())
                        .into(itemView.tic_public_info_partner_logo!!)
            } else {
                itemView.tic_public_info_partner_logo?.setImageDrawable(null)
            }
        } else {
            itemView.tic_public_info_partner_logo?.setImageDrawable(null)
        }
        val joinStatus = getJoinStatus(user.status, user.feedItem.type == TimestampedObject.TOUR_CARD)
        itemView.tic_join_status?.setText(Utils.fromHtml(itemView.context.getString(R.string.tour_info_text_join_html, user.displayName, joinStatus)), TextView.BufferType.SPANNABLE)
        when(user.status) {
            Tour.JOIN_STATUS_ACCEPTED, Tour.JOIN_STATUS_CANCELLED -> {
                val joinMessage = user.message
                if (joinMessage != null && joinMessage.isNotEmpty()) {
                    itemView.tic_public_info_message_layout?.visibility = View.VISIBLE
                    itemView.tic_public_join_message?.text = joinMessage
                    itemView.tic_public_info_timestamp?.text = DateFormat.format("H'h'm", user.timestamp)
                } else {
                    itemView.tic_public_info_message_layout?.visibility = View.GONE
                }
            }
            else -> {
                itemView.tic_public_info_message_layout?.visibility = View.GONE
            }
        }
    }

    private fun getJoinStatus(joinStatus: String, isTour: Boolean): String {
        return when (joinStatus) {
            Tour.JOIN_STATUS_ACCEPTED -> {
                val joinString = itemView.context.getString(if (isTour) R.string.tour_info_text_join_accepted else R.string.entourage_info_text_join_accepted)
                if (isTour && feedItem?.author != null) {
                    joinString + feedItem!!.author!!.userName
                } else
                    joinString
            }
            Tour.JOIN_STATUS_REJECTED -> itemView.context.getString(R.string.tour_info_text_join_rejected)
            Tour.JOIN_STATUS_PENDING -> itemView.context.getString(if (isTour) R.string.tour_join_request_received_message_short else R.string.entourage_join_request_received_message_short)
            Tour.JOIN_STATUS_CANCELLED -> itemView.context.getString(if (isTour) R.string.tour_info_text_join_cancelled_tour else R.string.tour_info_text_join_cancelled_entourage)
            Tour.JOIN_STATUS_QUITED -> itemView.context.getString(if (isTour) R.string.tour_info_text_join_quited_tour else R.string.tour_info_text_join_quited_entourage)
            else -> ""
        }
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.entourage_information_user_join_card_view
    }
}