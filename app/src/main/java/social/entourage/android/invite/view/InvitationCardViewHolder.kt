package social.entourage.android.invite.view

import android.view.View
import kotlinx.android.synthetic.main.layout_invitation_card.view.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.BusProvider.instance

/**
 * Created by mihaiionescu on 08/08/16.
 */
class InvitationCardViewHolder(view: View) : BaseCardViewHolder(view) {
    private var invitationId: Long = 0
    private lateinit var entourageUUID: String
    override fun bindFields() {
    }

    override fun populate(data: TimestampedObject) {
        val invitation = data as Invitation
        invitationId = invitation.id
        entourageUUID = invitation.entourageUUID
        itemView.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_BANNER_CLICK)
            if (entourageUUID.isBlank() || invitationId == 0L) return@setOnClickListener
            instance.post(OnFeedItemInfoViewRequestedEvent(entourageUUID, invitationId))
        }
        itemView.invitation_card_inviter?.text = itemView.resources.getString(R.string.invitation_card_inviter, invitation.inviterName)
        itemView.invitation_card_description?.text = invitation.entourage?.title ?: itemView.resources.getString(R.string.invitation_card_description)
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.layout_invitation_card
    }
}