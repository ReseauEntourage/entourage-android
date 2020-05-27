package social.entourage.android.entourage.information.discussion

import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.entourage_information_chat_message_others_card_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.CropCircleTransformation

/**
 * Chat Message Card for Tour Information Screen
 */
open class ChatMessageCardViewHolder(view: View) : BaseCardViewHolder(view) {
    private var userId = 0
    private var deeplinkURL:String? = null
    override fun bindFields() {
        itemView.tic_chat_user_photo?.setOnClickListener {
            if (userId != 0) BusProvider.instance.post(OnUserViewRequestedEvent(userId))
        }
    }

    override fun populate(data: TimestampedObject) {
        populate(data as ChatMessage)
    }

    fun populate(chatMessage: ChatMessage) {
        // user avatar
        itemView.tic_chat_user_photo?.let { userPhotoView->
            chatMessage.userAvatarURL?.let { avatarURL ->
                Picasso.get().load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .transform(CropCircleTransformation())
                        .into(userPhotoView)
            } ?: run {
                userPhotoView.setImageResource(R.drawable.ic_user_photo_small)
            }
        }
        // Partner logo
        itemView.tic_chat_user_partner_logo?.let { partnerLogoView ->
            chatMessage.partnerLogoSmall?.let { partnerLogoURL ->
                Picasso.get().load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(CropCircleTransformation())
                        .into(partnerLogoView)
            } ?: run {
                partnerLogoView.setImageDrawable(null)
            }
        }

        // user name
        itemView.tic_chat_user_name?.text = chatMessage.userName ?: ""

        // the actual chat
        val deeplink = DeepLinksManager.findFirstDeeplinkInText(chatMessage.content)
        if(deeplink?.isNotBlank() ==true) {
            deeplinkURL = deeplink
            itemView.tic_chat_deeplink.visibility = View.VISIBLE
            itemView.tic_chat_deeplink.setOnClickListener {
                onDeeplinkClick(it)}
            itemView.setOnClickListener {onDeeplinkClick(it)}
        } else {
            deeplinkURL = null
            itemView.tic_chat_deeplink.visibility = View.GONE
        }

        //linkify(it)
        itemView.tic_chat_message?.text = chatMessage.content
        //}
        // chat timestamp
        itemView.tic_chat_timestamp?.text = DateFormat.format("H'h'mm", chatMessage.timestamp)
        userId = chatMessage.userId
    }

    fun onDeeplinkClick(v:View) {
        if(deeplinkURL== null) {
            v.performClick()
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplinkURL))
        startActivity(v.context, intent, null)
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.entourage_information_chat_message_others_card_view
    }
}