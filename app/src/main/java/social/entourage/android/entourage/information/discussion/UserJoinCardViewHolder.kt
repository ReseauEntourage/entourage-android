package social.entourage.android.entourage.information.discussion

import android.net.Uri
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.fragment.app.findFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_entourage_information_user_join_card_view.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.Utils
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * User Card View in entourage information screen
 */
class UserJoinCardViewHolder(view: View) : BaseCardViewHolder(view) {
    private var userId = 0
    private var feedItem: FeedItem? = null

    override fun bindFields() {
        itemView.tic_photo?.setOnClickListener {onClick()}
        itemView.tic_partner_logo?.setOnClickListener {onClick()}
        itemView.tic_public_info_photo?.setOnClickListener  {onClick()}
        itemView.tic_view_profile_button?.setOnClickListener {onClick()}
        itemView.tic_accept_button?.setOnClickListener(View.OnClickListener {
            if (userId == 0) return@OnClickListener
            feedItem?.let {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_JOIN_REQUEST_ACCEPT)
                (itemView.findFragment() as? FeedItemInformationFragment)?.onUserJoinRequestUpdateEvent(
                    userId,
                    FeedItem.JOIN_STATUS_ACCEPTED,
                    it)
            }
        })
        itemView.tic_refuse_button?.setOnClickListener(View.OnClickListener {
            if (userId == 0) return@OnClickListener
            feedItem?.let {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_JOIN_REQUEST_REJECT)
                (itemView.findFragment() as? FeedItemInformationFragment)?.onUserJoinRequestUpdateEvent(
                    userId,
                    FeedItem.JOIN_STATUS_REJECTED,
                    it)
            }
        })
    }

    private fun onClick() {
        if (userId == 0) return
        EntBus.post(OnUserViewRequestedEvent(userId))
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

        itemView.tic_photo?.let { photoView ->
            user.avatarURLAsString?.let {avatarURL ->
                Glide.with(photoView.context)
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(photoView)
            } ?: run {
                Glide.with(photoView.context)
                        .load(R.drawable.ic_user_photo_small)
                        .into(photoView)
            }
        }

        // Partner logo
        itemView.tic_partner_logo?.let { partnerLogoView ->
            user.partner?.smallLogoUrl?.let {url ->
                Glide.with(partnerLogoView.context)
                        .load(Uri.parse(url))
                        .placeholder(R.drawable.partner_placeholder)
                        .circleCrop()
                        .into(partnerLogoView)
            } ?: run {
                partnerLogoView.setImageDrawable(null)
            }
        }
        itemView.tic_join_description?.text = getJoinStatus(user.status ?: "")
        itemView.tic_join_message?.text = user.message

        // If we are not the creators of the entourage, hide the Accept and Refuse buttons
        val me = EntourageApplication.me(itemView.context)
        val author = feedItem?.author
        val isMyEntourage = (me != null && author != null && me.id == author.userID)
        itemView.tic_accept_button?.visibility = if (isMyEntourage) View.VISIBLE else View.GONE
        itemView.tic_refuse_button?.visibility = if (isMyEntourage) View.VISIBLE else View.GONE
    }

    private fun populateJoinedStatus(user: EntourageUser) {
        itemView.tic_private_info_section?.visibility = View.GONE
        itemView.tic_public_info_section?.visibility = View.VISIBLE
        itemView.tic_public_info_username?.text = user.displayName

        itemView.tic_public_info_photo?.let { photoView ->
            user.avatarURLAsString?.let { avatarURL ->
                Glide.with(photoView.context)
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(photoView)
            } ?: run {
                Glide.with(photoView.context)
                        .load(R.drawable.ic_user_photo_small)
                        .into(photoView)
            }
        }

        // Partner logo
        itemView.tic_public_info_partner_logo?.let { partnerLogoView ->
            user.partner?.smallLogoUrl?.let {partnerLogoURL ->
                    Glide.with(partnerLogoView.context)
                            .load(Uri.parse(partnerLogoURL))
                            .placeholder(R.drawable.partner_placeholder)
                            .circleCrop()
                            .into(partnerLogoView)
            } ?: run {
                partnerLogoView.setImageDrawable(null)
            }
        }

        val joinStatus = getJoinStatus(user.status ?: "")
        itemView.tic_join_status?.setText(Utils.fromHtml(itemView.context.getString(R.string.entourage_info_text_join_html, user.displayName, joinStatus)), TextView.BufferType.SPANNABLE)
        when(user.status) {
            FeedItem.JOIN_STATUS_ACCEPTED, FeedItem.JOIN_STATUS_CANCELLED -> {
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

    private fun getJoinStatus(joinStatus: String): String {
        return when (joinStatus) {
            FeedItem.JOIN_STATUS_ACCEPTED -> itemView.context.getString(R.string.entourage_info_text_join_accepted)
            FeedItem.JOIN_STATUS_REJECTED -> itemView.context.getString(R.string.entourage_info_text_join_rejected)
            FeedItem.JOIN_STATUS_PENDING -> itemView.context.getString(R.string.entourage_join_request_received_message_short)
            FeedItem.JOIN_STATUS_CANCELLED -> itemView.context.getString(R.string.entourage_info_text_join_cancelled_entourage)
            FeedItem.JOIN_STATUS_QUITED -> itemView.context.getString(R.string.entourage_info_text_join_quited_entourage)
            else -> ""
        }
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_entourage_information_user_join_card_view
    }
}