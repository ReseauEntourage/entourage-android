package social.entourage.android.entourage.information.discussion

import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_entourage_information_chat_message_others_card_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.tape.Events
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.tools.EntBus

/**
 * Chat Message Card for Tour Information Screen
 */
open class ChatMessageCardViewHolder(val view: View) : BaseCardViewHolder(view) {
    private var userId = 0
    private var deeplinkURL:String? = null
    override fun bindFields() {
        itemView.tic_chat_user_photo?.setOnClickListener {
            if (userId != 0) EntBus.post(OnUserViewRequestedEvent(userId))
        }
    }

    override fun populate(data: TimestampedObject) {
        populate(data as ChatMessage)
    }

    private fun populate(chatMessage: ChatMessage) {
        val isPoi = chatMessage.metadata?.type?.equals("poi") ?: false
        val isEntourage = chatMessage.metadata?.type?.equals("entourage") ?: false
        var isMine = true
        // user avatar
        itemView.tic_chat_user_photo?.let { userPhotoView->
            isMine = false
            chatMessage.userAvatarURL?.let { avatarURL ->
                Glide.with(userPhotoView.context)
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(userPhotoView)
            } ?: run {
                Glide.with(userPhotoView.context)
                        .load(R.drawable.ic_user_photo_small)
                        .into(userPhotoView)
            }
        }

        // Partner logo
        itemView.tic_chat_user_partner_logo?.let { partnerLogoView ->
            chatMessage.partnerLogoSmall?.let { partnerLogoURL ->
                Glide.with(partnerLogoView.context)
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .circleCrop()
                        .into(partnerLogoView)
            } ?: run {
                partnerLogoView.setImageDrawable(null)
            }
        }

        // user name
        itemView.tic_chat_user_name?.text = chatMessage.userName

        // the actual chat
        val deeplink = DeepLinksManager.findFirstDeeplinkInText(chatMessage.content)
        if(deeplink?.isNotBlank() == true) {
            deeplinkURL = deeplink
            itemView.tic_chat_deeplink?.visibility = View.VISIBLE
            itemView.tic_chat_deeplink?.setOnClickListener { onDeeplinkClick(it) }
            itemView.setOnClickListener { onDeeplinkClick(it) }
        } else {
            deeplinkURL = null
            itemView.tic_chat_deeplink?.visibility = View.GONE
        }

        //linkify(it)
        itemView.tic_chat_message?.text = chatMessage.content
        //}
        // chat timestamp
        itemView.tic_chat_timestamp?.text = DateFormat.format("H'h'mm", chatMessage.timestamp)
        userId = chatMessage.userId

        if (isPoi) {
            setBubbleLink(isMine)

            itemView.layout_bubble?.setOnClickListener {
                chatMessage.metadata?.uuid?.let {showPoiDetail(it)}
            }
            itemView.tic_chat_deeplink?.setOnClickListener {
                chatMessage.metadata?.uuid?.let {showPoiDetail(it)}
            }
            itemView.tic_chat_message?.setOnClickListener {
                chatMessage.metadata?.uuid?.let {showPoiDetail(it)}
            }
        }
        if (isEntourage) {
            setBubbleLink(isMine)
        }
    }

    fun setBubbleLink(isMine:Boolean) {
        val colorLink = if (isMine) R.color.white else R.color.accent

        //itemView.layout_bubble?.background?.setColorFilter(ContextCompat.getColor(view.context, R.color.light_grey), android.graphics.PorterDuff.Mode.SRC_ATOP)
        itemView.tic_chat_message?.setTextColor(ContextCompat.getColor(view.context,colorLink))
        itemView.tic_chat_message?.setLinkTextColor(ContextCompat.getColor(view.context,colorLink))
        itemView.tic_chat_timestamp?.setTextColor(ContextCompat.getColor(view.context,colorLink))

        itemView.tic_chat_deeplink?.setBackgroundColor(ContextCompat.getColor(view.context,R.color.partner_logo_transparent))
        itemView.tic_chat_deeplink?.visibility = View.VISIBLE

        if (isMine) {
            itemView.tic_chat_deeplink?.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_open_in_new_white_24dp))
        }
        else {
            itemView.tic_chat_deeplink?.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_link))
        }
    }

    fun onDeeplinkClick(v:View) {
        if(deeplinkURL== null) {
            v.performClick()
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplinkURL))
        startActivity(v.context, intent, null)
    }

    fun showPoiDetail(poiId:String) {
        EntBus.post(Events.OnPoiViewDetail(poiId))
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_entourage_information_chat_message_others_card_view
    }
}