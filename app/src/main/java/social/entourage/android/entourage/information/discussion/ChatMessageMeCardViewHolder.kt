package social.entourage.android.entourage.information.discussion

import android.view.View
import social.entourage.android.R

/**
 * Created by mihaiionescu on 02/03/16.
 */
class ChatMessageMeCardViewHolder(view: View) : ChatMessageCardViewHolder(view) {
    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.entourage_information_chat_message_me_card_view
    }
}